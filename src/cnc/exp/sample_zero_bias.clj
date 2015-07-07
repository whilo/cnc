(ns cnc.exp.sample-zero-bias
  (:require [cnc.execute :refer [run-experiment! slurp-bytes write-json]]
            [cnc.eval-map :refer [find-fn]]
            [replikativ.stage :as s]
            [replikativ.platform :refer [<!?]]
            [konserve.protocols :refer [-exists? -get-in -bget]]
            [hasch.core :refer [uuid]]
            [clojure.core.async :refer [>!!]]
            [clojure.java.io :as io]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn setup-sampling! [store base-dir {:keys [calibration-id v-bias h-bias weights]}]
  (write-json base-dir "calibration.json" (<!? (-get-in store [calibration-id :output])))
  (write-json base-dir "weights.json" weights)
  (write-json base-dir "v_bias.json" v-bias)
  (write-json base-dir "h_bias.json" h-bias))

(defn gather-sampling! [base-dir _]
  (let [blob-names ["dist_joint_sim.h5"
                    "spike_trains.h5"]
        blobs (doall (map #(->> % (str base-dir) slurp-bytes)
                          blob-names))]
    {:output (into {} (map (fn [n b] [(keyword n)
                                     (uuid b)])
                           blob-names blobs))
     :new-blobs blobs}))


(comment
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in -bget]]
           '[replikativ.platform :refer [<!?]]
           '[boltzmann.matrix :refer [full-matrix]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))
  (future
    (let [source-path (str (get-in @state [:config :source-base-path])
                           "model-nmsampling/code/ev_cd/lif_sample.py")]
      (def zero-weights-exp
        (let [params {:weights (full-matrix (repeat 5 [0.0 0.0]))
                      :v-bias [7.493872648968013E-4 -4.777858590763243E-4]
                      :h-bias [-0.0048876059032700895 -0.007045275833854129
                               -0.0052037882113369575 -0.0016307435849696509 -0.0012139195273703854]
                      :calibration-id #uuid "35aaea32-02c3-550a-849d-a6e27832f9fa"
                      :source-path source-path
                      :args ["srun" "python" source-path]}]
          (try
            (run-experiment! (partial setup-sampling! store) gather-sampling! params)
            (catch Exception e
              (warn "Experiment failed: " (:source-path params))
              e))))))

  (:output zero-weights-exp)

  (future
    (let [source-path (str (get-in @state [:config :source-base-path])
                           "model-nmsampling/code/ev_cd/lif_sample.py")]
      (def zero-bias-exp
        (let [params {:weights ;; asymmetric
                      ;; transposed
                      [[0.0 0.0 0.0 0.0 0.0 -0.01754933012833558 5.972623899694243E-6] [0.0 0.0 0.0 0.0 0.0 -0.0181610140999075 0.0025956373673705715] [0.0 0.0 0.0 0.0 0.0 -0.010553584555954195 1.92010726884624E-4] [0.0 0.0 0.0 0.0 0.0 -0.01066898478193935 -4.2930275828171493E-4] [0.0 0.0 0.0 0.0 0.0 -1.1550209797454866E-4 5.211212727172467E-4] [2.056875957797295E-4 -0.0027931538164196745 0.0035847379363490857 0.00738939493531728 0.0032644557584475043 0.0 0.0] [0.0013669105912842605 -0.004409988990267686 0.003572532798821243 0.003874658825819731 0.0035927028983775873 0.0 0.0]]
                      :v-bias [0.0 0.0]
                      :h-bias (vec (repeat 5 0.0))
                      :calibration-id #uuid "35aaea32-02c3-550a-849d-a6e27832f9fa"
                      :source-path source-path
                      :args ["srun" "python" source-path]}]
          (try
            (run-experiment! (partial setup-sampling! store) gather-sampling! params)
            (catch Exception e
              (warn "Experiment failed: " (:source-path params))
              e))))))

  (:output zero-bias-exp)
  {:dist_joint_sim.h5 #uuid "3e9e0f32-0cfd-5b19-98e9-373ad99c1fc7", :spike_trains.h5 #uuid "00e1df01-52fc-5125-8885-8217ec993de8"}

  (let [exp zero-bias-exp]
    (doseq [b (:new-blobs exp)]
      (<!? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "train small rbms"] b)))

    (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                     (find-fn 'lif-sampling->datoms)
                     (dissoc exp
                             :new-blobs
                             :new-values))))
  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train small rbms"}}}))



  (count (full-matrix (full-matrix (repeat 5 [0.0 0.0]))))
  )
