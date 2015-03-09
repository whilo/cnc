(ns cnc.exp.sample
  (:require [cnc.execute :refer [run-experiment! gather-results!]]
            [cnc.eval-map :refer [find-fn]]
            [geschichte.stage :as s]
            [clojure.java.io :as io]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn setup-sampling! [base-directory exp-params]
  #_(spit (str base-directory "exp-params.edn") exp-params))

(defn gather-sampling! [base-directory _]
  {:output (read-string (slurp (str base-directory "samples.edn")))})


(comment
  (do
    (require '[cnc.core :refer [state]]
             '[konserve.protocols :refer [-get-in]]
             '[geschichte.platform :refer [<!?]]
             '[hasch.core :refer [uuid]])
    (def stage (get-in @state [:repo :stage]))
    (def store (get-in @state [:repo :store]))
    (def repo-id (get-in @state [:repo :id])))


  (let [source-path (str (get-in @state [:config :source-base-path])
                         "rbm-exps/src/rbm_exps/sample.clj")]
    (def sample-small
      (run-experiment! setup-sampling!
                       {:weights [[4.0 -4.0]
                                  [-4.0 4.0]]
                        :v-bias [0.0 0.0]
                        :h-bias [0.0 0.0]
                        :seed 42
                        :source-path source-path
                        :args ["srun" "lein-exec" "-p" source-path]})))


  (frequencies (:output sample-small))


  (<!? (-get-in store ["weilbach@dopamine.kip" repo-id :branches "sample"]))

  (<!? (s/branch! stage
                  ["weilbach@dopamine.kip" repo-id]
                  "train small rbms"
                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "master"]))))




  (let [res (gather-results! (:base-directory sample-small)
                             ["samples.edn"])]
    (<!? (s/transact-binary stage ["weilbach@dopamine.kip" repo-id "sample"] (-> res :new-blobs first)))
    (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "sample"]
                     (find-fn 'sampling->datoms)
                     (dissoc res :new-blobs))))

  (s/abort-transactions stage ["weilbach@dopamine.kip" repo-id "sample"])

  (uuid (merge sample-small (gather-sampling! (:base-directory sample-small) nil)))


  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"sample"}}}))



  ;; sample from stdp rbm
  ;; - extract sequence of weights from rbm
  (<!? (-get-in store ["weilbach@dopamine.kip" repo-id :branches "sample"]))
  ;; - for each sample for some time in parallel
  ;; - calculate dist_join_sim over visible units







  (println *command-line-args*)

  )
