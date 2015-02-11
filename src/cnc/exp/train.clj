(ns cnc.exp.train
  (:require [cnc.execute :refer [run-experiment! slurp-bytes write-json git-commit]]
            [cnc.eval-map :refer [find-fn]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [konserve.protocols :refer [-exists? -get-in -bget]]
            [hasch.core :refer [uuid]]
            [clojure.core.async :refer [>!!]]
            [clojure.java.io :as io]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn setup-training! [store base-dir {:keys [training-params
                                              data-id calibration-id]}]
  (write-json base-dir "training_params.json" training-params)
  (write-json base-dir "calibration.json" (<!? (-get-in store [calibration-id :output])))
  (write-json base-dir "data.json" (<!? (-get-in store [data-id :output]))))

(defn gather-training! [base-directory _]
  (let [blob-names ["bias_history.h5"
                    "weight_history.h5"
                    "dist_joint_sim.h5"
                    "train.log"
                    "spike_trains.h5"]
        blobs (doall (map #(->> % (str base-directory) slurp-bytes)
                          blob-names))]
    {:output (into {} (map (fn [n b] [(keyword n)
                                     (uuid b)])
                           blob-names blobs))
     :new-blobs blobs}))


(comment
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in -bget]]
           '[geschichte.platform :refer [<!?]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))

  (future
    (let [source-path (str (get-in @state [:config :source-base-path])
                           "model-nmsampling/code/ev_cd/train.py")]
      (def curr-exp
        (let [params {:training-params {:h_count 10
                                        :epochs 1,
                                        :dt 0.1,
                                        :burn_in_time 0.,
                                        :phase_duration 100.0,
                                        :learning_rate 2e-5,
                                        :weight_recording_interval 100.0,
                                        :stdp_burnin 10.0,
                                        :sim_setup_kwargs {:grng_seed 43
                                                           :rng_seeds_seed 43}}
                      :calibration-id #uuid "22f685d0-ea7f-53b5-97d7-c6d6cadc67d3"
                      :data-id #uuid "3197da4c-3806-544f-a62b-2f48383691d4"
                      :source-path source-path
                      :args ["python" source-path]}]
          (try
            (run-experiment! (partial setup-training! store) gather-training! params)
            (catch Exception e
              (debug "experiment failed: " e)
              e))))))

  (:output curr-exp)

  (println (-> curr-exp ex-data :process :out))
  (println (-> curr-exp :process :out))

  (clojure.pprint/pprint (dissoc curr-exp :new-blobs :new-values :process))

  (<!? (-get-in store [(uuid (dissoc curr-exp :new-blobs :new-values :process))]))



  (<!? (-get-in store [#uuid "181516fb-226e-5f0b-81b8-09eadddc4f9a"]))

  (clojure.pprint/pprint #_(->> test-exp :process :err (take-last 1000) (apply str))
                         (dissoc curr-exp :process :new-blobs))

  (<!? (-get-in store [(uuid {:h_count 12
                              :epochs 1,
                              :dt 0.01,
                              :burn_in_time 0.,
                              :phase_duration 100.0,
                              :learning_rate 1e-6,
                              :weight_recording_interval 100.0,
                              :stdp_burnin 10.0,
                              :sampling_time 1e6})]))


  ;; TODO protect from committing dangling value uuids
  (let [exp curr-exp
        tparams (-> exp :exp-params :training-params)]
    (doseq [b (:new-blobs exp)]
      (<!? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train current rbms"] b)))

    (when-not (<!? (-exists? store (uuid tparams)))
      (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                       (find-fn 'add-training-params)
                       tparams)))

    (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                     (find-fn 'train-ev-cd->datoms)
                     (dissoc exp
                             :process
                             :new-blobs
                             :new-values))))

  (uuid (dissoc curr-exp :process :new-blobs :new-values))



  (swap! stage update-in ["weilbach@dopamine.kip" repo-id :transactions] assoc "train current rbms" [])
  (get-in @stage ["weilbach@dopamine.kip" repo-id :transactions "train current rbms"])


  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train current rbms"}}}))

  (clojure.pprint/pprint (-> curr-exp (dissoc :process)))

  (def hist
    (<!? (s/commit-history-values store
                                  (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :causal-order])
                                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "train current rbms"])))))

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

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
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

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                   (find-fn 'data->datoms)
                   {:output digit-data
                    :name "5x5 digits 3,4,5"}))

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                   (find-fn 'data->datoms)
                   {:output [[0] [1]]
                    :name "Minimal binary data [[0],[1]]."}))


  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                   (find-fn 'data->datoms)
                   {:output (read-string (slurp "/tmp/data.json"))
                    :name "Small unsymmetric distribution."}))


  (do
    (time (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train current rbms"}}})))
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


  (write-json  (<!? (-get-in store [#uuid "1070c715-96af-5a38-856f-0ef985bda116" :output]))))
