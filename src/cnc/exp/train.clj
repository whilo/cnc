(ns cnc.exp.train
  (:require [cnc.execute :refer [run-experiment! slurp-bytes write-json git-commit gather-results!]]
            [cnc.eval-map :refer [find-fn]]
            [geschichte.stage :as s]
            [geschichte.realize :as real]
            [geschichte.platform :refer [<!?]]
            [konserve.protocols :refer [-exists? -get-in -bget -update-in -assoc-in]]
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
  (write-json base-dir "data.json" (<!? (-bget store data-id #(-> % :input-stream slurp read-string)))))

(comment
  (do
    (require '[cnc.core :refer [state]]
             '[konserve.protocols :refer [-get-in -bget -exists?]]
             '[geschichte.platform :refer [<!?]])
    (def stage (get-in @state [:repo :stage]))
    (def store (get-in @state [:repo :store]))
    (def repo-id (get-in @state [:repo :id])))


  (def curr-exps
    (doall
     (map
      (fn [rate]
        (future
          (let [source-path (str (get-in @state [:config :source-base-path])
                                 "model-nmsampling/code/ev_cd/train.py")
                seed 42]
            (let [params {:training-params {:h_count 10,
                                            :epochs 20000, ;; TODO
                                            :dt 0.1,
                                            :burn_in_time 0.,
                                            :phase_duration 100.0,
                                            :learning_rate rate,
                                            :weight_recording_interval 100.0,
                                            :stdp_burnin 5.0,
                                            :sim_setup_kwargs {:grng_seed seed
                                                               :rng_seeds_seed seed}}
                          :calibration-id #uuid "22f685d0-ea7f-53b5-97d7-c6d6cadc67d3"
                          :data-id
                          #_#uuid "0bd17cd8-237b-5ecd-987a-033ca22ea6f1" ;; 2x2 bars
                          #uuid "0b619370-6716-5ae8-89b6-9c38b4e11e94" ;; 3x3 bars
                          #_#uuid "18127501-df3c-578d-8863-a3e17f2a61a7" ;; 5x5 digits 3,4,5
                          #_#uuid "0527b8c6-db13-5275-862e-22a6e940c7e9" ;; strong XOR
                          :source-path source-path
                          :args ["srun-log" "python" source-path]}]
              (try
                (run-experiment! (partial setup-training! store) params)
                (catch Exception e
                  (debug "experiment failed: " e)
                  e))))))
      #_[50 100 200 300 400 500 1000]
      [1e-5 5e-5 1e-6 5e-6 1e-6 5e-7 1e-7 1e-8 5e-8] #_(range 50 60))))




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



  (def base-dirs (map #(str "experiments/" % "/")
                      (let [f (io/file "experiments/")]
                        (filter #(.contains % "Fri Mar 13 11") (.list f)))))



  (clojure.pprint/pprint (mapv #(-> (gather-results! % [
                                                        "bias_theo_history.h5"
                                                        "weight_theo_history.h5"
                                                        ])
                                    (dissoc :new-blobs))
                               base-dirs))


  (mapv #(-> (gather-results! % [
                                 "bias_theo_history.h5"
                                 "weight_theo_history.h5"
                                 ])
             (dissoc :new-blobs))
        base-dirs)




  ;; TODO protect from committing dangling value uuids
  (doseq [base-dir base-dirs #_(map (comp :base-directory deref) curr-exps)]
    (when base-dir
      (let [res (gather-results! base-dir ["bias_bio_history.h5"
                                           "weight_bio_history.h5"
                                           "bias_theo_history.h5"
                                           "weight_theo_history.h5"
                                           "dist_joint_sim.h5"
                                           "train.log"
                                           #_"srun.log"
                                           ])
            res (assoc res :topic "sweep hidden-unit count")
            tparams (-> res :exp-params :training-params)]
        (doseq [b (:new-blobs res)]
          (<!? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train current rbms4"] b)))

        (when-not (<!? (-exists? store (uuid tparams)))
          (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms4"]
                           (find-fn 'add-training-params)
                           tparams)))

        (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms4"]
                         (find-fn 'train-ev-cd->datoms)
                         (dissoc res :new-blobs)))
        (println "transacted exp: " (uuid (dissoc res :new-blobs))))
      (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train current rbms4"}}}))))


  (clojure.pprint/pprint (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches]))
  (uuid (dissoc curr-exp :process :new-blobs :new-values))


  (swap! stage update-in ["weilbach@dopamine.kip" repo-id :transactions] assoc "train current rbms" [])
  (keys (get-in @stage ["weilbach@dopamine.kip" repo-id :state]))

  (<!? (-update-in store ["weilbach@dopamine.kip" repo-id] #(dissoc % :state)))
  (<!? (-assoc-in store ["weilbach@dopamine.kip" repo-id :branches "train current rbms2"] #{#uuid "3cf62813-8e88-5feb-b968-eae428a7f93e"} ))


  (<!? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train current rbms4"] digits))



  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train current rbms4"}}}))

  (clojure.pprint/pprint (-> curr-exp (dissoc :process)))

  (def hist
    (<!? (real/commit-history-values store
                                     (get-in @stage ["weilbach@dopamine.kip" repo-id :state :causal-order])
                                     (first (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches "train current rbms4"])))))

  (clojure.pprint/pprint hist)

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

  (spit "/tmp/digits" digit-data)
  (def digits (slurp-bytes "/tmp/digits"))



  (spit "/tmp/bars" (str [[1 0 0
                           1 0 0
                           1 0 0]
                          [0 0 1
                           0 0 1
                           0 0 1]
                          [1 1 1
                           0 0 0
                           0 0 0]]))
  (def bars (slurp-bytes "/tmp/bars"))

  )
