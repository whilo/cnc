(ns cnc.exp.train
  (:require [cnc.execute :refer [run-experiment! slurp-bytes]]
            [cnc.eval-map :refer [find-fn]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [konserve.protocols :refer [-get-in -bget]]
            [hasch.core :refer [uuid]]
            [clojure.core.async :refer [>!!]]
            [clojure.java.io :as io]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn write-json [base-dir name coll]
  (with-open [w (io/writer (str base-dir name))]
    (json/generate-stream coll w)))

(defn setup-training! [store base-dir {:keys [neuron-params training-params
                                              data-id calibration-id]}]
  (write-json base-dir "neuron_params.json" neuron-params)
  (write-json base-dir "training_params.json" training-params)
  (write-json base-dir "calibration.json" (<!? (-get-in store [calibration-id :output])))
  (write-json base-dir "data.json" (<!? (-get-in store [data-id :output]))))

(defn gather-training! [base-directory _]
  (let [blob-names ["bias_history.h5"
                    "dist_joint_sim.h5"
                    "dist_joint.pdf"
                    "dist_joint.png"
                    "spike_trains.h5"
                    "training_overview.png"
                    "weight_avgs.h5"
                    "weights_history.h5"]
        blobs (doall (map #(->> % (str base-directory) slurp-bytes)
                          blob-names))]
    {:output (into {} (map (fn [n b] [(keyword n)
                                     (uuid b)])
                           blob-names blobs))
     :new-blobs blobs}))


(def training-params {:h_count 9
                      :epochs 1,
                      :dt 0.01,
                      :burn_in_time 0.,
                      :phase_duration 100.0,
                      :learning_rate 1e-6,
                      :weight_recording_interval 100.0,
                      :stdp_burnin 10.0,
                      :sampling_time 1e6})

(def neuron-params {:cm         0.2,
                    :tau_m      1.,
                    :e_rev_E    0.,
                    :e_rev_I    -100.,
                    :v_thresh   -50.,
                    :tau_syn_E  10.,
                    :v_rest     -50.,
                    :tau_syn_I  10.,
                    :v_reset    -50.001,
                    :tau_refrac 10.,
                    :i_offset   0.})



(comment
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in -bget]]
           '[geschichte.platform :refer [<!?]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))
  (future
    (let [source-path (str (get-in @state [:config :source-base-path]) "model-nmsampling/code/ev_cd/stdp.py")]
      (def test-exp
        (let [params {:neuron-params neuron-params
                      :training-params {:h_count 12
                                        :epochs 1,
                                        :dt 0.01,
                                        :burn_in_time 0.,
                                        :phase_duration 100.0,
                                        :learning_rate 1e-6,
                                        :weight_recording_interval 100.0,
                                        :stdp_burnin 10.0,
                                        :sampling_time 1e6}
                      :calibration-id #uuid "1070c715-96af-5a38-856f-0ef985bda116"
                      :data-id #uuid "2e354ab2-5629-53f0-88d6-79e405f61217"
                      :source-path source-path
                      :args ["srun" "python" source-path]}]
          (merge (gather-training! "experiments/Mon Jan 12 22:34:47 CET 2015_9a8e8f96/" nil)
                 {:exp-params params
                  :base-directory "experiments/Mon Jan 12 22:34:47 CET 2015_9a8e8f96/"
                  :git-commit-id "f9a3077b68b43a0a269d922de18d7f411dbed751"})
          #_(try
              (run-experiment! (partial setup-training! store) gather-training! params)
              (catch Exception e
                e))))))

  (future
    (let [source-path (str (get-in @state [:config :source-base-path]) "model-nmsampling/code/ev_cd/stdp.py")]
      (def unsymmetric-exp
        (let [params {:neuron-params neuron-params
                      :training-params {:h_count 5
                                        :epochs 1,
                                        :dt 0.01,
                                        :burn_in_time 0.,
                                        :phase_duration 100.0,
                                        :learning_rate 1e-5,
                                        :weight_recording_interval 100.0,
                                        :stdp_burnin 10.0,
                                        :sampling_time 1e6}
                      :calibration-id #uuid "1070c715-96af-5a38-856f-0ef985bda116"
                      :data-id #uuid "25d4dfa7-3c11-559a-9aa3-618a5258a911"
                      :source-path source-path
                      :args ["srun" "python" source-path]}]
          (try
            (run-experiment! (partial setup-training! store) gather-training! params)
            (catch Exception e
              e))))))


  (<!? (-get-in store [(uuid (dissoc test-exp
                                     :new-blobs
                                     :new-values))]))


  {:bias_history.h5 #uuid "10ecc026-1c3d-527c-ba6b-7f9dd69da60c",
   :dist_joint_sim.h5 #uuid "31ad948e-a13d-55bd-9e68-1294f8eb0efa",
   :dist_joint.pdf #uuid "3056c6c5-0b9d-578f-b7a8-3b62d1a240d4",
   :dist_joint.png #uuid "1bb7ce42-6ea4-534d-92d3-c7c957faaa78",
   :spike_trains.h5 #uuid "1aa5a022-b1bc-59fb-bbad-ad61f18ac68b",
   :training_overview.png #uuid "0b58eba0-bb60-5666-8ef1-7c5f93380ff5",
   :weight_avgs.h5 #uuid "0b5d005f-c2b9-54c8-ade1-4c9c817e2083",
   :weights_history.h5 #uuid "2db03bec-762d-5731-a747-613de8028200"}


  (clojure.pprint/pprint #_(->> test-exp :process :err (take-last 1000) (apply str))
                         (dissoc unsymmetric-exp :process :new-blobs))

  (<!? (-get-in store [(uuid {:h_count 12
                              :epochs 1,
                              :dt 0.01,
                              :burn_in_time 0.,
                              :phase_duration 100.0,
                              :learning_rate 1e-6,
                              :weight_recording_interval 100.0,
                              :stdp_burnin 10.0,
                              :sampling_time 1e6})]))

  (doseq [b (:new-blobs test-exp)]
    (<!? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train small rbms"] b)))

  (swap! stage update-in ["weilbach@dopamine.kip" repo-id :transactions] assoc "train small rbms" [])
  (get-in @stage ["weilbach@dopamine.kip" repo-id :transactions "train small rbms"])

  ;; protect from committing dangling value uuids
  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                   (find-fn 'train-ev-cd->datoms)
                   (dissoc test-exp
                           :new-blobs
                           :new-values)))

  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train small rbms"}}}))

  (clojure.pprint/pprint (-> test-exp (dissoc :process)))

  (def hist
    (<!? (s/commit-history-values store
                                  (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :causal-order])
                                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "train small rbms"])))))

  (first (:transactions (second hist)))
  (uuid (-> hist second :transactions second first))
  (clojure.pprint/pprint hist)

  (uuid (<!? (-bget store #uuid "08a569bd-7797-5678-b134-70c2764b3dea" (comp slurp-bytes :input-stream))))

  (require '[clojure.core.matrix :as mat])

  (def db (hdf5/create "/tmp/test.h5"))
  (def db (hdf5/open "experiments/162719c0-b0c2-456f-a4e6-11cbe7ebff2d/weights_history.h5"))
  (def db (hdf5/open "experiments/162719c0-b0c2-456f-a4e6-11cbe7ebff2d/dist_joint_sim.h5"))

  (def db (hdf5/open (<!? (-bget store #uuid "0ce9da38-9a10-51eb-ae96-4768b2fa78d6" :file))))

  (def group (hdf5/create-group db "spike-train"))

  (def dataset (hdf5/create-dataset group "neuron-1" "hello world!"))

  (def array-dataset (hdf5/create-dataset group "neuron-2" (int-array 10 (int 1023))))

  (get-in (mat/matrix (hdf5/read  (hdf5/get-dataset db "/dist_joint_sim"))) [0 1 1])

  (def weights-history (hdf5/read  (hdf5/get-dataset db "/weights_history")))
  (count weights-history)
  (spit "/tmp/weights_history.json" (vec (get weights-history 110010)))

  (hdf5/members db)

  (hdf5/close db)

  (def digit-data [[0 1 1 1 0
                    0 0 0 0 1
                    0 0 1 1 0
                    0 0 0 0 1
                    0 1 1 1 0]
                   [1 0 0 0 0
                    1 0 1 0 0
                    1 1 1 1 0
                    0 0 1 0 0
                    0 0 1 0 0]
                   [1 1 1 1 0
                    1 0 0 0 0
                    1 1 1 0 0
                    0 0 0 1 0
                    1 1 1 0 0]])

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                   (find-fn 'add-training-params)
                   {:h_count 5
                    :epochs 10,
                    :dt 0.01,
                    :burn_in_time 0.,
                    :phase_duration 100.0,
                    :learning_rate 1e-6,
                    :weight_recording_interval 100.0,
                    :stdp_burnin 10.0,
                    :sampling_time 1e6}))

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                   (find-fn 'data->datoms)
                   {:output digit-data
                    :name "5x5 digits 3,4,5"}))


  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                   (find-fn 'data->datoms)
                   {:output (read-string (slurp "/tmp/data.json"))
                    :name "Small unsymmetric distribution."}))


  (do
    (time (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train small rbms"}}})))
    nil)


  (future
    (let [source-path (str (get-in @state [:config :source-base-path]) "model-nmsampling/code/ev_cd/stdp.py")
          training-params {:h_count 30
                           :epochs 2000,
                           :dt 0.1,
                           :burn_in_time 0.,
                           :phase_duration 100.0,
                           :learning_rate 1e-6,
                           :weight_recording_interval 1e4,
                           :stdp_burnin 10.0,
                           :sampling_time 1e6}]
      (def digit-exp
        (try
          (run-experiment! (partial setup-training! store)
                           gather-training!
                           {:neuron-params neuron-params
                            :training-params training-params
                            :calibration-id #uuid "1070c715-96af-5a38-856f-0ef985bda116"
                            :data-id #uuid "04501ad6-45f4-5880-a3ee-ce91c748f933"
                            :source-path source-path
                            :args ["srun" "python" source-path]})
          (catch Exception e
            e)))))

  (<!? (-get-in store [#uuid "04501ad6-45f4-5880-a3ee-ce91c748f933"]))

  (clojure.pprint/pprint (-> digit-exp #_(dissoc :process)))




  )
