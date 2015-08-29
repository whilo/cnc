(ns cnc.exp.train
  (:require [cnc.execute :refer [run-experiment! slurp-bytes write-json git-commit gather-results! create-exp-dir]]
            [cnc.eval-map :refer [find-fn]]
            [replikativ.crdt.repo.stage :as s]
            [replikativ.crdt.repo.realize :as real]
            [full.async :refer [<??]]
            [boltzmann.theoretical :as theo]
            [boltzmann.matrix :refer [full-matrix]]
            [incanter.stats :refer [sample-normal]]
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
                                              weights biases] :as exp-params}]
  (write-json base-dir "training_params.json" training-params)
  (write-json base-dir "init_weights.json" weights)
  (write-json base-dir "init_biases.json" biases)
  (write-json base-dir "calibration.json" (<?? (-get-in store [calibration-id :output])))
  (write-json base-dir "data.json" (<?? (-bget store data-id #(-> % :input-stream slurp read-string)))))



(defn theo-lr [H tau-stdp T tau-ref w-trans] ;; TODO add tau-br
  (float (* H 2 tau-stdp T (/ 1 (Math/pow tau-ref 2)) w-trans)))

(defn theo-lr->bio [lr tau-stdp T tau-ref w-trans]
  (float (/ lr (theo-lr 1 tau-stdp T tau-ref w-trans))))

(comment

  (theo-lr 1e-7 10 1000 10 (/ 1 0.00317))

  (do
    (require '[cnc.core :refer [state]]
             '[konserve.protocols :refer [-get-in -bget -exists?]])
    (def stage (get-in @state [:repo :stage]))
    (def store (get-in @state [:repo :store]))
    (def repo-id (get-in @state [:repo :id])))

  (full-matrix (vec (take 20 #(repeatedly (vec (sample-normal 9 :mean 0 :sd 0.01))))))

  (<?? (-get-in store [#uuid "3dff229e-363d-5c21-947d-e363fddfa399"]))

  (def test-env
    (future
      (let [source-path (str (get-in @state [:config :source-base-path])
                             "model-nmsampling/code/ev_cd/train.py")]
        (run-experiment! (partial setup-training! store)
                         {:source-path source-path
                          :args ["srun-log" "python" source-path]
                          :training-params {:h_count 10,
                                            :epochs 200 ;; TODO
                                            :dt 0.1,
                                            :burn_in_time 0.,
                                            :phase_duration 100.0,
                                            :learning_rate_init 2e-6,
                                        ;:bias_learning_rate 0.0,
                                            :sampling_time 1e6
                                            :weight_recording_interval 100.0,
                                            :stdp_burnin 20.0,
                                            :sim_setup_kwargs {:grng_seed 42
                                                               :rng_seeds_seed 42}}

                          :weights (full-matrix (vec (take 10 (repeatedly #(vec (sample-normal 154 :mean 0 :sd 0.01))))))
                          :biases (repeat 164 0)
                          :exp-name "digits"
                          :calibration-id #uuid "2033334b-05b8-540f-9b9e-dc936801a6f8"
                          :data-id
                          #_#uuid "0bd17cd8-237b-5ecd-987a-033ca22ea6f1" ;; 2x2 bars
                          #_#uuid "0b619370-6716-5ae8-89b6-9c38b4e11e94" ;; 3x3 bars
                          #_#uuid "37a2a128-ae09-5e59-9559-ebbc6ef3e597" ;; 5x5 digits 4,5,3
                          #uuid "121ab1db-a1e0-5bed-9933-6330f63fba65" ;; 12x12 + label small mnist digits 3,4,5
                          #_#uuid "18127501-df3c-578d-8863-a3e17f2a61a7" ;; 5x5 digits 3,4,5
                          #_#uuid "0527b8c6-db13-5275-862e-22a6e940c7e9" ;; strong XOR
                          #_#uuid "0185f4e0-a88f-5843-b7bb-bc9a5c928218" ;; soft XOR
                          #_#uuid "30b23c43-7b14-5250-8f87-52f1eed0c7a4" ;; 3x3 bars symmetric
                          }))))

  (<?? (-get-in store [#uuid "18127501-df3c-578d-8863-a3e17f2a61a7" ]))

  (require '[clojure.java.shell :refer [sh]])
  (sh "gnome-terminal"  "ipython"
      :dir (:base-directory test-env)
      :env (assoc (into {} (System/getenv))
                  "DISPLAY" "localhost:37.0"))


  (def curr-exps
    (doall
     (for [phase-duration [1000]
           seed (range 50 60)]
       (future
         (let [source-path (str (get-in @state [:config :source-base-path])
                                "model-nmsampling/code/ev_cd/train.py")
               h-count 20
               v-count 9]
           (let [params {:training-params {:h_count h-count,
                                           :epochs (int (/ 2e6 phase-duration)) ;; TODO
                                           :dt 0.1,
                                           :burn_in_time 0.,
                                           :phase_duration (float phase-duration)
                                           :learning_rate (float (/ 1e-4 phase-duration)) ;; ~ lr 0.01
                                           :weight_recording_interval 100.0,
                                           :stdp_burnin 20.0,
                                           :sim_setup_kwargs {:grng_seed seed
                                                              :rng_seeds_seed seed}}
                         #_(vec (take 20 #(repeatedly (vec (sample-normal 9 :mean 0 :sd 0.01)))))
                         :weights (full-matrix [[-7.664036749380962E-4 -2.562386754141901E-4 0.013822004245474178 -0.0022818383448701862 -7.811137329310021E-4 -0.011715832217955502 -7.745592147699734E-4 0.0159238467655298 -0.0019558637264695763] [-0.004923278931593456 -0.006902785941740222 0.0022506240271483596 0.005123704622273227 0.007844374897920918 -0.005083509117575359 -0.003524235608095418 6.520950585379455E-4 0.01854859832605527] [-0.0063255037951736315 0.006540827706271029 -6.540271747245754E-4 -0.004455272924723231 0.0034296266812934625 -0.004800641409812772 0.0021214480624468404 0.004035201242696172 -0.008389038961947242] [0.006618948965641044 -0.0010880475857611878 0.0032369370874010387 0.005723718605549148 -0.014681420237670618 0.008102145304072269 0.011776046590860987 4.646395082070941E-4 -0.014905396819613224] [-0.008109913482898147 -0.005687314004782421 0.002427338809176914 -3.97412154268436E-5 -0.010361947767676991 -0.017523353315214835 -0.011586908037746216 0.007072112268216339 -0.008651219090300503] [-0.0026907436393912377 0.007184312107520426 -0.006831314044273269 0.0198069301478893 0.007602529623651352 0.010263681981734912 0.007236576934243795 1.8556291696205965E-4 0.015037729240728073] [0.011355684813942975 -0.0038773935735100487 -0.00862015250267014 0.0090002077611994 0.001131471198587196 -0.011624992229704052 -0.015565728348029188 0.0038436689684173764 1.6586170039840708E-4] [0.007319453641886518 0.010972962335211117 -0.017234723533899175 -0.0025973816745891633 0.0026147485096136495 -8.03580195584755E-4 9.506790683245019E-4 -0.005852222663889197 -0.024776615610478985] [-0.004596604723055763 0.009381429897770582 -0.008938680663721405 0.006323298390942373 0.0027396777789669684 0.007505464451761501 0.007361912089290902 0.00863029445023163 -0.00564585571628202] [0.016117559870164874 0.009272457763763554 -0.006521570903359428 0.0026487481240348152 -0.008600961885012881 0.0196591123365055 -0.019625148686841298 0.013640951120723232 -0.006888059930886866] [0.0010194423887498487 0.0056481058410672745 -0.009749010586571077 -0.009150982840213748 0.0016940146251809038 -0.011779967212505888 -0.006280124334383436 0.004056898195430901 0.007492359813519893] [5.401383752402477E-4 0.017517167793427265 -2.748261972265998E-4 0.011919173075989657 0.003943825348446874 0.018306153455558163 -0.003593340430168061 0.03097430057491113 -0.012588128450700101] [-0.0015735585460383964 -0.012318393620452154 0.006083327087058154 0.020411661147666193 -0.01342425861772773 0.006080779515740005 -0.0026572476974946295 -0.006943294646392541 0.0031352670966969106] [-0.002365209996495664 0.018980259471602142 0.007099637872926429 0.005164470836572873 0.00978681115786032 0.017824938425784007 0.009192928954774702 0.01258134043096833 -1.6668825099392428E-4] [-0.010049329614324897 -0.013976437811905 0.013928833978803689 0.010300555990359027 0.009966551338015715 -0.013057339809639737 0.009663313276927222 0.016339319448533764 1.5138979620489234E-4] [-0.002707323164608901 0.0017398777584710667 -0.001435782868064688 -0.007179710186370515 0.004257284970540099 -0.010186409663534639 0.008124780401927765 -0.003041170370695345 0.0031202677754470606] [0.017313502459600248 0.004891617454207757 0.02003451591120121 0.018579132252156693 -0.003306167641076789 -0.020336690733887313 0.012813930878150618 0.01728175267218138 -0.002307716293241962] [0.011976898799858453 -0.014562192560805806 -9.814267664808513E-4 -0.002756473823195137 0.003146514093045516 -0.006518237660103966 -0.010404869814239711 -0.009948998498916336 0.00586174911861045] [-0.00471155350784076 0.00689682615390127 -0.0019031021505873194 0.014822565900591908 -0.013610758430274726 0.0018016472325935182 0.006757924057358311 0.0031065100926015027 0.005102134405331422] [-0.010686515370945173 -0.006301877814784609 0.011595184906934425 -0.0030182756608344487 0.014635781296334758 -0.009135486088587351 0.003263737418515076 0.007399731025787994 -0.017682232010800086]])
                         :biases (vec (repeat (+ h-count v-count) 0))

                         :calibration-id #uuid "22f685d0-ea7f-53b5-97d7-c6d6cadc67d3"
                         :data-id
                         #_#uuid "0bd17cd8-237b-5ecd-987a-033ca22ea6f1" ;; 2x2 bars
                         #_#uuid "0b619370-6716-5ae8-89b6-9c38b4e11e94" ;; 3x3 bars
                         #_#uuid "18127501-df3c-578d-8863-a3e17f2a61a7" ;; 5x5 digits 3,4,5
                         #_#uuid "0527b8c6-db13-5275-862e-22a6e940c7e9" ;; strong XOR
                         #uuid "30b23c43-7b14-5250-8f87-52f1eed0c7a4" ;; 3x3 bars symmetric
                         :source-path source-path
                         :args ["srun-log" "python" source-path]}]
             (try
               (run-experiment! (partial setup-training! store) params)
               (catch Exception e
                 (debug "experiment failed: " e)
                 e))))))))

  #_[1e-5 5e-5 1e-6 5e-6 1e-6 5e-7 1e-7 1e-8 5e-8]


  (println (clojure.java.shell/sh "ps"))

  (def rbm #boltzmann.theoretical.TheoreticalRBM{:restricted-weights [[-0.8841340318279167 2.251071635836021 3.5673896795687194 -3.420249147683104 -1.2819443536331754 0.9862760699510305 -3.4208225115014526 -1.2835930112086564 0.9873557565397155] [5.228078612942525 3.153110701050849 -1.6835081697846173 2.18766698353923 -1.575343145114616 -4.827752402673504 2.1873629031641753 -1.5766255413938415 -4.828597199783511] [-1.1414929820445003 2.0231880113442706 3.829782067690602 -3.5034730413120085 -1.2842516806749793 1.422658547637368 -3.502344192821967 -1.28558125705108 1.422741375870716] [-1.0225817906128736 2.081521313666241 3.6883277133438406 -3.4370661291171474 -1.2858671906995713 1.2297101281301448 -3.4375017775416783 -1.2845039114086085 1.2298397140916468] [4.467966121624177 3.087131915623917 -1.2084866875874103 1.4317318887144581 -1.5665440680582514 -4.347260768337335 1.4332610804749855 -1.566357645033309 -4.346089328590838]] :v-biases [-1.5341301556155136 -9.510913440211544 -2.0159380056943514 1.2062999975130388 -2.7633099136703025 0.8206392901391488 1.2050512968752354 -2.7620516045991153 0.8192053284323875] :h-biases [0.7027248426055048 1.2372358615489172 0.8801590566333531 0.8040422074136998 0.9519249106300683]})

  (def curr-exp
    (future
      (let [source-path (str (get-in @state [:config :source-base-path])
                             "model-nmsampling/code/ev_cd/train.py")
            seed 42
            phase-duration 2000]
        (let [params {:training-params {:h_count 20,
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

  (<?? (-get-in store [(uuid (dissoc curr-exp :new-blobs :new-values :process))]))


  (<?? (-get-in store [#uuid "181516fb-226e-5f0b-81b8-09eadddc4f9a"]))

  (clojure.pprint/pprint #_(->> test-exp :process :err (take-last 1000) (apply str))
                         (dissoc curr-exp :process :new-blobs))

  (<?? (-get-in store [(uuid {:h_count 12
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
                        (filter #(.contains % "digits_Thu_Aug_27_17:40") (.list f)))))



  (clojure.pprint/pprint (mapv #(-> (gather-results! % [
                                                        "spike_trains.h5"
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
      (let [res (gather-results! base-dir ["bias_theo_history.h5"
                                           "weight_theo_history.h5"
                                           "spike_trains.h5"
                                           #_"dist_joint_sim.h5"
                                           #_"train.log"
                                           "srun.log"
                                           ])
            res (assoc res :topic "5x5 3 digits 3,4,5")
            tparams (-> res :exp-params :training-params)]
        (doseq [b (:new-blobs res)]
          (<?? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train current rbms"] b)))

        (when-not (<?? (-exists? store (uuid tparams)))
          (<?? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                           (find-fn 'add-training-params)
                           tparams)))

        (<?? (s/transact stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                         (find-fn 'train-ev-cd->datoms)
                         (dissoc res :new-blobs)))
        (println "transacted exp: " (uuid (dissoc res :new-blobs))))
      (<?? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train current rbms"}}}))))


  (clojure.pprint/pprint (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches]))
  (uuid (dissoc curr-exp :process :new-blobs :new-values))


  (swap! stage update-in ["weilbach@dopamine.kip" repo-id :transactions] assoc "train current rbms" [])
  (keys (get-in @stage ["weilbach@dopamine.kip" repo-id :state]))

  (<?? (-update-in store ["weilbach@dopamine.kip" repo-id] #(dissoc % :state)))
  (<?? (-assoc-in store ["weilbach@dopamine.kip" repo-id :branches "train current rbms2"] #{#uuid "3cf62813-8e88-5feb-b968-eae428a7f93e"} ))


  (<?? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train current rbms"] xor))



  (<?? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train current rbms"}}}))

  (clojure.pprint/pprint (-> curr-exp (dissoc :process)))

  (def hist
    (<?? (real/commit-history-values store
                                     (get-in @stage ["weilbach@dopamine.kip" repo-id :state :causal-order])
                                     (first (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches "train current rbms"])))))

  (clojure.pprint/pprint hist)

  (def digit-data [
                   [1 0 0 0 0
                    1 0 1 0 0
                    1 1 1 1 0
                    0 0 1 0 0
                    0 0 1 0 0]
                   [1 1 1 1 0
                    1 0 0 0 0
                    1 1 1 0 0
                    0 0 0 1 0
                    1 1 1 0 0]
                   [0 1 1 1 0
                    0 0 0 0 1
                    0 0 1 1 0
                    0 0 0 0 1
                    0 1 1 1 0]])

  (spit "/tmp/digits" digit-data)
  (def digits (slurp-bytes "/tmp/digits"))

  (uuid digits)


  (spit "/tmp/bars" (str [[1 0 0
                           1 0 0
                           1 0 0]
                          [0 0 1
                           0 0 1
                           0 0 1]
                          [1 1 1
                           0 0 0
                           0 0 0]
                          [0 0 0
                           0 0 0
                           1 1 1]]))
  (def bars (slurp-bytes "/tmp/bars"))

  (def xor (slurp-bytes "/tmp/xor"))
  (spit "/tmp/xor" (str [[0 0]
                         [1 0]
                         [1 0]
                         [1 0]
                         [1 0]
                         [1 0]
                         [0 1]
                         [0 1]
                         [0 1]
                         [0 1]
                         [0 1]
                         [1 1]]))


  (def three-digits-mnist-small-raw [[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.33039215686274509, 0.89803921568627454, 0.68431372549019609, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0098039215686274508, 0.33137254901960783, 0.96568627450980393, 0.92647058823529416, 0.80490196078431375, 0.42941176470588238, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.44313725490196076, 0.99019607843137258, 0.79313725490196085, 0.96568627450980393, 0.40294117647058825, 0.82647058823529407, 0.0, 0.0, 0.0, 0.0, 0.0, 0.24607843137254903, 0.95686274509803915, 0.70882352941176463, 0.085294117647058826, 0.13921568627450981, 0.0, 0.98137254901960791, 0.2107843137254902, 0.0, 0.0, 0.0, 0.062745098039215685, 0.915686274509804, 0.3666666666666667, 0.046078431372549022, 0.0, 0.0, 0.0, 0.99019607843137258, 0.38235294117647056, 0.0, 0.0, 0.0, 0.50980392156862742, 0.79117647058823537, 0.0, 0.0, 0.0, 0.0, 0.0, 0.99313725490196081, 0.33725490196078434, 0.0, 0.0, 0.0, 0.66078431372549018, 0.46862745098039216, 0.0, 0.0, 0.0, 0.0068627450980392156, 0.51470588235294112, 0.72058823529411764, 0.011764705882352941, 0.0, 0.0, 0.0, 0.66274509803921577, 0.36274509803921567, 0.0, 0.0, 0.11176470588235295, 0.69019607843137254, 0.57549019607843133, 0.0, 0.0, 0.0, 0.0, 0.0, 0.66078431372549018, 0.88137254901960782, 0.51078431372549016, 0.75196078431372548, 0.90784313725490196, 0.50980392156862742, 0.054901960784313725, 0.0, 0.0, 0.0, 0.0, 0.0, 0.24705882352941175, 0.8666666666666667, 0.99019607843137258, 0.65000000000000002, 0.14215686274509803, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0], [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.037254901960784313, 0.14509803921568626, 0.49803921568627452, 0.49607843137254903, 0.49607843137254903, 0.17647058823529413, 0.0, 0.0, 0.0, 0.0, 0.0, 0.21666666666666667, 0.84999999999999998, 0.96274509803921571, 0.99019607843137258, 0.9882352941176471, 0.9882352941176471, 0.89607843137254906, 0.071568627450980388, 0.0, 0.0, 0.0, 0.0, 0.11078431372549019, 0.55098039215686279, 0.3784313725490196, 0.25980392156862747, 0.32843137254901961, 0.96274509803921571, 0.90980392156862755, 0.064705882352941183, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.35490196078431374, 0.97941176470588243, 0.64901960784313728, 0.0, 0.0, 0.0, 0.0, 0.0, 0.031372549019607843, 0.31176470588235294, 0.3784313725490196, 0.79215686274509811, 0.98333333333333339, 0.82745098039215692, 0.23627450980392156, 0.0, 0.0, 0.0, 0.0, 0.0, 0.52450980392156865, 0.99019607843137258, 0.99019607843137258, 0.99313725490196081, 0.99019607843137258, 0.49411764705882355, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.030392156862745098, 0.17156862745098039, 0.086274509803921567, 0.086274509803921567, 0.64901960784313728, 0.63921568627450986, 0.0, 0.0, 0.0, 0.0, 0.0, 0.078431372549019607, 0.0088235294117647058, 0.0, 0.0, 0.096078431372549025, 0.81568627450980391, 0.63921568627450986, 0.0, 0.0, 0.0, 0.0, 0.26372549019607844, 0.92058823529411771, 0.30392156862745101, 0.2627450980392157, 0.34803921568627449, 0.81960784313725488, 0.92549019607843142, 0.3833333333333333, 0.0, 0.0, 0.0, 0.0, 0.25196078431372548, 0.89509803921568631, 0.9882352941176471, 0.9882352941176471, 0.90980392156862744, 0.66078431372549018, 0.15784313725490196, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.10784313725490197, 0.47745098039215683, 0.26960784313725494, 0.023529411764705882, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0], [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.012745098039215686, 0.034313725490196081, 0.0, 0.0, 0.0, 0.0, 0.0, 0.14705882352941177, 0.58039215686274515, 0.0039215686274509803, 0.0, 0.0, 0.25882352941176473, 0.84019607843137256, 0.077450980392156865, 0.0, 0.0, 0.0, 0.0, 0.14215686274509803, 0.9882352941176471, 0.070588235294117646, 0.0, 0.0, 0.22647058823529415, 0.9882352941176471, 0.70882352941176474, 0.016666666666666666, 0.0, 0.0, 0.0, 0.50098039215686274, 0.9882352941176471, 0.23921568627450981, 0.0, 0.0, 0.0029411764705882353, 0.59607843137254901, 0.9882352941176471, 0.31960784313725488, 0.0, 0.0, 0.0, 0.53431372549019607, 0.9882352941176471, 0.070588235294117646, 0.0, 0.0, 0.0, 0.25196078431372548, 0.9882352941176471, 0.64411764705882346, 0.056862745098039215, 0.24509803921568626, 0.707843137254902, 0.93137254901960786, 0.94901960784313732, 0.053921568627450983, 0.0, 0.0, 0.0, 0.0, 0.70392156862745092, 0.99019607843137258, 0.99019607843137258, 0.99313725490196081, 0.99019607843137258, 0.99019607843137258, 0.99019607843137258, 0.24901960784313726, 0.0, 0.0, 0.0, 0.0, 0.59019607843137256, 0.9882352941176471, 0.92352941176470593, 0.54705882352941182, 0.34999999999999998, 0.665686274509804, 0.9882352941176471, 0.30392156862745096, 0.0, 0.0, 0.0, 0.0, 0.16862745098039217, 0.3362745098039216, 0.044117647058823532, 0.0, 0.0, 0.59019607843137256, 0.9882352941176471, 0.50686274509803919, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.30980392156862746, 0.96568627450980393, 0.89509803921568631, 0.014705882352941176, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.36764705882352944, 0.82450980392156858, 0.072549019607843143, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0]])
  (spit "/tmp/three-digits-mnist-small.edn" three-digits-mnist-small-raw)

  (def three-digits-small-mnist (slurp-bytes "/tmp/three-digits-mnist-small.edn"))
  )
