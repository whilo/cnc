(ns cnc.exp.train
  (:require [cnc.execute :refer [run-experiment! slurp-bytes]]
            [cnc.eval-map :refer [find-fn]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [konserve.protocols :refer [-get-in]]
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
        blobs (map #(->> % (str base-directory) slurp-bytes)
                   blob-names)]
    {:output (into {} (map (fn [n b] [(keyword n)
                                     (uuid b)])
                           blob-names blobs))
     :new-blobs blobs}))


(def training-params {:epochs 2000,
                      :dt 0.01,
                      :burn_in_time 0.,
                      :phase_duration 100.0,
                      :learning_rate 1e-6,
                      :weight_recording_interval 100,
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
           '[konserve.protocols :refer [-get-in]]
           '[geschichte.platform :refer [<!?]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))

  (future
    (let [source-path (str (get-in @state [:config :source-base-path]) "model-nmsampling/code/ev_cd/stdp.py")]
      (def test-exp
        (run-experiment! (partial setup-training! store)
                         gather-training!
                         {:neuron-params neuron-params
                          :training-params training-params
                          :calibration-id #uuid "1070c715-96af-5a38-856f-0ef985bda116"
                          :data-id #uuid "12515c62-4bc6-5804-a870-84c7bee1b85f"
                          :source-path source-path
                          :args ["srun" "python" source-path]}))))

  (clojure.pprint/pprint (dissoc test-exp :process :new-blobs))


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
                                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches])))))

  (first (:transactions (second hist)))
  (uuid (-> hist second :transactions second first))




  (require '[clojure.core.matrix :as mat])

  (def db (hdf5/create "/tmp/test.h5"))
  (def db (hdf5/open "experiments/162719c0-b0c2-456f-a4e6-11cbe7ebff2d/weights_history.h5"))
  (def db (hdf5/open "experiments/162719c0-b0c2-456f-a4e6-11cbe7ebff2d/dist_joint_sim.h5"))

  (def group (hdf5/create-group db "spike-train"))

  (def dataset (hdf5/create-dataset group "neuron-1" "hello world!"))

  (def array-dataset (hdf5/create-dataset group "neuron-2" (int-array 10 (int 1023))))

  (get-in (mat/matrix (hdf5/read  (hdf5/get-dataset db "/dist_joint_sim"))) [0 1 1])
  (vec (first (hdf5/read  (hdf5/get-dataset db "/weights_history"))))

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
                   training-params))

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                   (find-fn 'data->datoms)
                   {:output digit-data
                    :name "5x5 digits 3,4,5"}))


  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train small rbms"}}}))


  (future
    (let [source-path (str (get-in @state [:config :source-base-path]) "model-nmsampling/code/ev_cd/stdp.py")]
      (def test-exp
        (run-experiment! (partial setup-training! store)
                         gather-training!
                         {:neuron-params neuron-params
                          :training-params training-params
                          :calibration-id #uuid "1070c715-96af-5a38-856f-0ef985bda116"
                          :data-id #uuid "04501ad6-45f4-5880-a3ee-ce91c748f933"
                          :source-path source-path
                          :args ["srun" "python" source-path]})))))
