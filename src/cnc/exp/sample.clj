(ns cnc.exp.sample
  (:require [cnc.execute :refer [run-experiment!]]
            [boltzmann.core :refer [sample-gibbs]]
            [boltzmann.theoretical :refer [create-theoretical-rbm]]
            [geschichte.stage :as s]
            [clojure.core.matrix :as mat]
            [clj-hdf5.core :as hdf5]
            [clojure.java.io :as io]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn setup-sampling! [base-directory exp-params]
  (spit (str base-directory "exp-params.edn") exp-params))

(defn gather-sampling! [base-directory _]
  (read-string (slurp (str base-directory "samples.edn"))))

(def base-dir (last *command-line-args*))

(def exp-params (read-string (slurp (str base-dir "exp-params.edn"))))

(let [{:keys [weights v-bias h-bias seed]} exp-params]
  (def rbm (create-theoretical-rbm weights v-bias h-bias))

  (def samples (mapv #(vec (take (count v-bias) %))
                     (sample-gibbs rbm 10000 :seed seed))))

(spit (str base-dir "samples.edn") samples)


(comment
  (require '[cnc.repo :refer [stage repo-id]]
           '[geschichte.platform :refer [<!?]])

  (:out (:process test-exp))
  (:output test-exp)

  (let [source-path "/usr/src/cnc/src/cnc/exp/sample.clj"]
    (def test-exp
      (run-experiment! setup-sampling!
                       gather-sampling!
                       {:weights [[0.5 -0.2 0.3]
                                  [-0.2 -0.4 0.2]]
                        :v-bias [0.0 0.0 0.0]
                        :h-bias [0.0 0.0]
                        :seed 42
                        :source (slurp source-path)
                        :args ["lein-exec" "-p" source-path]})))


  (def sampling->datoms-val
    '(fn sampling->datoms [conn params]
       (let [id (uuid params)
             {samples :output
              {:keys [weights v-bias h-bias seed]} :exp-params}  params]
         (db-transact conn [{:val/id (uuid samples)
                             :sampling/count (count samples)
                             :sampling/seed seed
                             :ref/rbm-weights (uuid weights)
                             :ref/rbm-v-bias (uuid v-bias)
                             :ref/rbm-h-bias (uuid h-bias)
                             :source/id (uuid source)
                             :ref/trans-params id}])
         conn)))


  (<!? (s/transact stage ["whilo@dopamine.kip" repo-id "calibrate"]
                   test-exp
                   sampling->datoms-val))

  (<!? (s/commit! stage {"whilo@dopamine.kip" {repo-id #{"calibrate"}}}))








  (println *command-line-args*)


  (def db (hdf5/create (io/file "samples.h5")))

  (def group (hdf5/create-group db "samples"))

  (def array-dataset (hdf5/create-dataset group "samples" (flatten samples)))

  (println (hdf5/read  (hdf5/get-dataset db "/samples/samples")))

  (hdf5/close db)

  )
