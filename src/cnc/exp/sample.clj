(ns cnc.exp.sample
  (:require [cnc.execute :refer [run-experiment!]]
            [cnc.eval-map :refer [find-fn]]
            [geschichte.stage :as s]
            [clojure.java.io :as io]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn setup-sampling! [base-directory exp-params]
  (spit (str base-directory "exp-params.edn") exp-params))

(defn gather-sampling! [base-directory _]
  {:output (read-string (slurp (str base-directory "samples.edn")))})


(comment
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in]]
           '[geschichte.platform :refer [<!?]]
           '[hasch.core :refer [uuid]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))


  (let [source-path (str (get-in @state [:config :source-base-path])
                         "rbm-exps/src/rbm_exps/sample.clj")]
    (def sample-small
      (run-experiment! setup-sampling!
                       gather-sampling!
                       {:weights [[1.5 -0.2 1.3 0.3]
                                  [-0.2 -1.4 1.2 -0.1]
                                  [0.1 -2.3 1.6 0.2]]
                        :v-bias [-2.1 0.0 0.3 0.8]
                        :h-bias [0.8 -1.2 0.3]
                        :seed 42
                        :source-path source-path
                        :args ["srun" "lein-exec" "-p" source-path]})))


  (<!? (-get-in store ["weilbach@dopamine.kip" repo-id :branches "sample"]))

  (<!? (s/branch! stage
                  ["weilbach@dopamine.kip" repo-id]
                  "train small rbms"
                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "master"]))))



  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "sample"]
                   (find-fn 'sampling->datoms)
                   sample-small))

  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"sample"}}}))



  ;; sample from stdp rbm
  ;; - extract sequence of weights from rbm
  (<!? (-bget store ["weilbach@dopamine.kip" repo-id :branches "sample"]))
  ;; - for each sample for some time in parallel
  ;; - calculate dist_join_sim over visible units







  (println *command-line-args*)

  )
