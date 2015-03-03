(ns cnc.exp.lif-sample
  (:require [cnc.execute :refer [run-experiment! write-json slurp-bytes]]
            [cnc.eval-map :refer [find-fn]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [hasch.core :refer [uuid]]
            [konserve.protocols :refer [-exists? -get-in -bget]]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [clj-hdf5.core :as hdf5]
            [cnc.analytics :refer [get-hdf5-tensor]]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)
(defn setup-lif-sampling! [store base-dir {:keys [weights v-bias h-bias sampling-params calibration-id]}]
  (write-json base-dir "weights.json" weights)
  (write-json base-dir "v_bias.json" v-bias)
  (write-json base-dir "h_bias.json" h-bias)
  (write-json base-dir "sampling_params.json" sampling-params)
  (write-json base-dir "calibration.json" (<!? (-get-in store [calibration-id :output]))))



(defn gather-lif-sampling! [base-directory _]
  (let [blob-names ["dist_joint_sim.h5"
                    "lif_sample.log"
                    "spiketrains.h5"]
        blobs (doall (map #(->> % (str base-directory) slurp-bytes)
                          blob-names))]
    {:output (into {} (map (fn [n b] [(keyword n)
                                     (uuid b)])
                           blob-names blobs))
     :new-blobs blobs}))


(comment
  (require '[cnc.core :refer [state]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))


  (require '[boltzmann.matrix :refer [full-matrix]])

  (def weights (get-hdf5-tensor store #uuid "3760e2a2-7804-576f-8566-5c3d7fdde0a0" "/weight"))
  (def biases (get-hdf5-tensor store #uuid "28a97297-2f9b-57e0-9564-70d24e93fdff" "/weight"))

  (full-matrix (partition 6 (take 12 (drop 12 (last weights)))))

  (clojure.pprint/pprint (partition 10 (last weights)))
  (count (last biases))
  (count weights)

  (let [source-path (str (get-in @state [:config :source-base-path])
                         "model-nmsampling/code/ev_cd/lif_sample.py")
        train (<!? (-get-in store [#uuid "0898b806-aade-5130-9c65-e5b2cac96071" :output]))
        weights (last (get-hdf5-tensor store (:weight_theo_history.h5 train) "/weight"))
        biases (last (get-hdf5-tensor store (:bias_theo_history.h5 train) "/weight"))]
    (def lif-sample-small
      (run-experiment! (partial setup-lif-sampling! store)
                       gather-lif-sampling!
                       {:weights (partition 2 (take 4 (drop 4 weights)))
                        :v-bias (vec (take 2 biases))
                        :h-bias (vec (drop 2 biases))
                        :sampling-params {:sim_setup_kwargs {:grng_seed 43
                                                             :rng_seeds_seed 43}
                                          :duration 1e6}
                        :calibration-id #uuid "22f685d0-ea7f-53b5-97d7-c6d6cadc67d3"
                        :source-path source-path
                        :args ["srun" "python" source-path]})))


  (println (lif-sample-small :process))

  (get-hdf5-tensor store #uuid "3c4bd40f-d8c5-5273-aca0-9d36944ce9a6" "/dist_joint_sim")


  (let [exp lif-sample-small]
    (doseq [b (:new-blobs exp)]
      (<!? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train current rbms"] b)))

    (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                     (find-fn 'lif-sampling->datoms)
                     (dissoc exp
                             :process
                             :new-blobs))))


  (<!? (-get-in store ["weilbach@dopamine.kip" repo-id :branches "sample"]))

  (<!? (s/branch! stage
                  ["weilbach@dopamine.kip" repo-id]
                  "train small rbms"
                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "master"]))))



  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "sample"]
                   (find-fn 'sampling->datoms)
                   sample-small))

  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train current rbms"}}}))



  ;; sample from stdp rbm
  ;; - extract sequence of weights from rbm
  (<!? (-bget store ["weilbach@dopamine.kip" repo-id :branches "sample"]))
  ;; - for each sample for some time in parallel
  ;; - calculate dist_join_sim over visible units







  (println *command-line-args*))
