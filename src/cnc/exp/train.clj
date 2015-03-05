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
  (let [blob-names ["bias_bio_history.h5"
                    "weight_bio_history.h5"
                    "bias_theo_history.h5"
                    "bias_theo_history.pdf"
                    "weight_theo_history.h5"
                    "weight_theo_history.pdf"
                    "dist_joint_sim.h5"
                    "train.log"]
        blobs (doall (map #(->> % (str base-directory) slurp-bytes)
                          blob-names))]
    {:output (into {} (map (fn [n b] [(keyword n)
                                     (uuid b)])
                           blob-names blobs))
     :new-blobs blobs}))


(comment
  (do
    (require '[cnc.core :refer [state]]
             '[konserve.protocols :refer [-get-in -bget]]
             '[geschichte.platform :refer [<!?]])
    (def stage (get-in @state [:repo :stage]))
    (def store (get-in @state [:repo :store]))
    (def repo-id (get-in @state [:repo :id])))

  (def curr-exps
    (doall
     (map
      (fn [seed]
        (future
          (let [source-path (str (get-in @state [:config :source-base-path])
                                 "model-nmsampling/code/ev_cd/train.py")]
            (let [params {:training-params {:h_count 6,
                                            :epochs 4,
                                            :dt 0.1,
                                            :burn_in_time 0.,
                                            :phase_duration 100.0,
                                            :learning_rate 5e-8,
                                            :weight_recording_interval 100.0,
                                            :stdp_burnin 10.0,
                                            :sim_setup_kwargs {:grng_seed seed
                                                               :rng_seeds_seed seed}}
                          :calibration-id #uuid "22f685d0-ea7f-53b5-97d7-c6d6cadc67d3"
                          :data-id #uuid "00fb4927-1ef8-549d-b41f-afbac6463014" ;; strong sym XOR
                          #_#uuid "37107994-69aa-5a8f-9fd9-5616298b993b" ;; strong XOR
                          #_#uuid "29175e59-4df7-5229-9257-757b74f7af1b" ;; weaker XOR
                          #_#uuid "3197da4c-3806-544f-a62b-2f48383691d4"
                          :source-path source-path
                          :args ["srun-log" "python" source-path]}]
              (try
                (run-experiment! (partial setup-training! store) params)
                (catch Exception e
                  (debug "experiment failed: " e)
                  e))))))
      (range 50 55))))

  (println (-> curr-exp ex-data :process :err))
  (println (-> curr-exp :process :out))
  (-> curr-exp :output :dist_joint_sim.h5)


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
  (doseq [exp (map deref curr-exps)]
    (let [tparams (-> exp :exp-params :training-params)
          res (gather-training! (:base-directory exp) nil)]
      (doseq [b (:new-blobs res)]
        (<!? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train current rbms"] b)))

      (when-not (<!? (-exists? store (uuid tparams)))
        (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                         (find-fn 'add-training-params)
                         tparams)))

      (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                       (find-fn 'train-ev-cd->datoms)
                       (dissoc exp :process))))
    (println "transacted exp: " (uuid (dissoc exp :process)))
    (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train current rbms"}}})))


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
