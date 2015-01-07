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
  (read-string (slurp (str base-directory "samples.edn"))))


(comment
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in]]
           '[geschichte.platform :refer [<!?]])
  (def stage2 (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))


  (let [source-path #_"/wang/users/weilbach/cluster_home/cnc/src/cnc/exp/sample.clj"
        "/usr/src/rbm-exps/src/rbm_exps/sample.clj"]
    (def test-exp
      (run-experiment! setup-sampling!
                       gather-sampling!
                       {:weights [[0.5 -0.2 0.3]
                                  [-0.2 -0.4 0.2]]
                        :v-bias [0.0 0.0 0.0]
                        :h-bias [0.0 0.0]
                        :seed 42
                        :source-path source-path
                        :args ["lein-exec" "-p" source-path]})))

  (<!? (-get-in store ["weilbach@dopamine.kip" repo-id :branches "train small rbms"]))

  (<!? (s/branch! stage
                  ["weilbach@dopamine.kip" repo-id]
                  "train small rbms"
                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "master"]))))

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                   (find-fn 'sampling->datoms)
                   test-exp))

  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"train small rbms"}}}))








  (println *command-line-args*)


  (def db (hdf5/create (io/file "samples.h5")))

  (def group (hdf5/create-group db "samples"))

  (def array-dataset (hdf5/create-dataset group "samples" (flatten samples)))

  (println (hdf5/read  (hdf5/get-dataset db "/samples/samples")))

  (hdf5/close db)

  )
