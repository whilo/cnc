(ns cnc.core
  (:require [datomic.api :as d]
            [cognitect.transit :as tr]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clj-hdf5.core :as hdf5]
            [clojure.data.json :as json]))

(def uri "datomic:mem:///experiments")

(d/delete-database uri)
(d/create-database uri)

(def conn (d/connect uri))

(def schema (read-string (slurp "resources/schema.edn")))


(d/transact  conn schema)


(def neuron-params {"cm"         0.2,
                    "tau_m"      1.,
                    "e_rev_E"    0.,
                    "e_rev_I"    -100.,
                    "v_thresh"   -50.,
                    "tau_syn_E"  10.,
                    "v_rest"     -50.,
                    "tau_syn_I"  10.,
                    "v_reset"    -50.001,
                    "tau_refrac" 10.,
                    "i_offset"   0.})

(def training-params {"epochs" 2000
                      "dt" 0.01
                      "burn_in_time" 0.0
                      "phase_duration" 100.0
                      "learning_rate" 1e-6
                      "stdp_burnin" 10.0
                      "data.h5" (str (java.util.UUID/randomUUID) ".h5")})


(def file-writer (io/output-stream (io/file (str "../data/" (java.util.UUID/randomUUID) "-datoms.json"))))

(def tr-writer (tr/writer file-writer :json))

(tr/write tr-writer test-experiment)


(sh "sbatch")

(sh "srun" "python" "../neural-sampling/simple_STDP.py" "--plot-figure" "nest")

(sh "srun" "env")

(seq (.list (io/file "../neural-sampling")))

(defn add-id [conn datoms-map]
  (let [id (d/entid (d/db conn) :db.part/user)]
    (assoc datoms-map :db/id id)))

(d/transact conn (map (partial add-id conn) [test-experiment]))

(d/q '[:find ?cm ?v-rest
       :where
       [?n :cm ?cm]
       [?n :v_rest ?v-rest]]
     (d/db conn))

(add-id conn test-experiment)


(comment
  "datomic:free://localhost:4334/experiments"
  (d/create-database uri)

  (sh "srun" "python" "-" :in "print 'hello'")

  (sh "srun" "python" "-" :in "import pyNN")
  )
