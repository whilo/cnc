(ns cnc.exp.train
  (:require [cnc.execute :refer [run-experiment! slurp-bytes write-json git-commit gather-results! create-exp-dir]]
            [cnc.eval-map :refer [find-fn]]
            [geschichte.stage :as s]
            [geschichte.realize :as real]
            [geschichte.platform :refer [<!?]]
            [boltzmann.theoretical :as theo]
            [konserve.protocols :refer [-exists? -get-in -bget -update-in -assoc-in]]
            [hasch.core :refer [uuid]]
            [clojure.core.async :refer [>!!]]
            [clojure.java.io :as io]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn setup-training! [store base-dir {:keys [training-params
                                              data-id calibration-id
                                              init-rweights init-biases] :as exp-params}]
  (write-json base-dir "training_params.json" training-params)
  (write-json base-dir "init_rweights.json" init-rweights)
  (write-json base-dir "init_biases.json" init-biases)
  (write-json base-dir "calibration.json" (<!? (-get-in store [calibration-id :output])))
  (write-json base-dir "data.json" (<!? (-bget store data-id #(-> % :input-stream slurp read-string))))
  (assoc exp-params :base-directory base-dir))

(comment
  (do
    (require '[cnc.core :refer [state]]
             '[konserve.protocols :refer [-get-in -bget -exists?]]
             '[geschichte.platform :refer [<!?]])
    (def stage (get-in @state [:repo :stage]))
    (def store (get-in @state [:repo :store]))
    (def repo-id (get-in @state [:repo :id])))

  (def test-env
    (setup-training! store (create-exp-dir (java.util.Date.))
                     {:training-params {:h_count 5,
                                        :epochs 10 ;; TODO
                                        :dt 0.1,
                                        :burn_in_time 0.,
                                        :phase_duration 2000.0,
                                        :learning_rate 1e-7,
                                        ;:bias_learning_rate 0.0,
                                        :weight_recording_interval 100.0,
                                        :stdp_burnin 5.0,
                                        :sim_setup_kwargs {:grng_seed 42
                                                           :rng_seeds_seed 42}}
                      :calibration-id #uuid "22f685d0-ea7f-53b5-97d7-c6d6cadc67d3"
                      :data-id
                      #_#uuid "0bd17cd8-237b-5ecd-987a-033ca22ea6f1" ;; 2x2 bars
                      #uuid "0b619370-6716-5ae8-89b6-9c38b4e11e94" ;; 3x3 bars
                      #_#uuid "18127501-df3c-578d-8863-a3e17f2a61a7" ;; 5x5 digits 3,4,5
                      #_#uuid "0527b8c6-db13-5275-862e-22a6e940c7e9" ;; strong XOR
                      }))

  (require '[clojure.java.shell :refer [sh]])


  (sh "gnome-terminal"  "ipython"
      :dir (:base-directory test-env)
      :env (assoc (into {} (System/getenv))
             "DISPLAY" "localhost:20.0"))


  (def curr-exps
    (doall
     (map
      (fn [seed]
        (future
          (let [source-path (str (get-in @state [:config :source-base-path])
                                 "model-nmsampling/code/ev_cd/train.py")
                phase-duration 2000]
            (let [params {:training-params {:h_count 5,
                                            :epochs (int (/ (* 20000 100)
                                                            phase-duration)) ;; TODO
                                            :dt 0.1,
                                            :burn_in_time 0.,
                                            :phase_duration (float phase-duration)
                                            :learning_rate 1e-7,
                                        ;:bias_learning_rate 0.0,
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
      #_[1000 1500 2000 2500 3000 3500 4000 4500 5000]
      #_[1e-5 5e-5 1e-6 5e-6 1e-6 5e-7 1e-7 1e-8 5e-8]
      (range 50 60))))

  (println (clojure.java.shell/sh "ps"))

  (def rbm #boltzmann.theoretical.TheoreticalRBM{:restricted-weights [[-0.8841340318279167 2.251071635836021 3.5673896795687194 -3.420249147683104 -1.2819443536331754 0.9862760699510305 -3.4208225115014526 -1.2835930112086564 0.9873557565397155] [5.228078612942525 3.153110701050849 -1.6835081697846173 2.18766698353923 -1.575343145114616 -4.827752402673504 2.1873629031641753 -1.5766255413938415 -4.828597199783511] [-1.1414929820445003 2.0231880113442706 3.829782067690602 -3.5034730413120085 -1.2842516806749793 1.422658547637368 -3.502344192821967 -1.28558125705108 1.422741375870716] [-1.0225817906128736 2.081521313666241 3.6883277133438406 -3.4370661291171474 -1.2858671906995713 1.2297101281301448 -3.4375017775416783 -1.2845039114086085 1.2298397140916468] [4.467966121624177 3.087131915623917 -1.2084866875874103 1.4317318887144581 -1.5665440680582514 -4.347260768337335 1.4332610804749855 -1.566357645033309 -4.346089328590838]] :v-biases [-1.5341301556155136 -9.510913440211544 -2.0159380056943514 1.2062999975130388 -2.7633099136703025 0.8206392901391488 1.2050512968752354 -2.7620516045991153 0.8192053284323875] :h-biases [0.7027248426055048 1.2372358615489172 0.8801590566333531 0.8040422074136998 0.9519249106300683]})

  (def curr-exp
    (future
      (let [source-path (str (get-in @state [:config :source-base-path])
                             "model-nmsampling/code/ev_cd/train.py")
            seed 42
            phase-duration 2000]
        (let [params {:training-params {:h_count 5,
                                        :epochs (int (/ (* 10000 100)
                                                        phase-duration)) ;; TODO
                                        :dt 0.1,
                                        :burn_in_time 0.,
                                        :phase_duration (float phase-duration)
                                        :learning_rate 1e-8,
                                        :weight_recording_interval 100.0,
                                        ;:bias_learning_rate 0.0,
                                        :stdp_burnin 5.0,
                                        :sim_setup_kwargs {:grng_seed seed
                                                           :rng_seeds_seed seed}}
                      :calibration-id #uuid "22f685d0-ea7f-53b5-97d7-c6d6cadc67d3"
                      :init-rweights (:restricted-weights rbm)
                      #_(->> (repeat 9 0.00001)
                             (repeat 5)
                             (mapv vec))
                      :init-biases (vec (concat (:v-biases rbm) (:h-biases rbm)))

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
                        (filter #(.contains % "Fri Mar 20 21") (.list f)))))



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
                                           #_"dist_joint_sim.h5"
                                           #_"train.log"
                                           "srun.log"
                                           ])
            res (assoc res :topic "3x3 fixed fully pre-trained")
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
