(ns cnc.core
  (:require [datomic.api :as d]
            [cognitect.transit :as tr]
            [clojure.core.async :refer [<!! >!!]]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [hasch.core :refer [uuid]]
            [konserve.store :refer [new-mem-store]]
            [geschichte.sync :refer [server-peer]]
            [geschichte.stage :as s]
            [geschichte.p2p.fetch :refer [fetch]]
            [geschichte.p2p.publish-on-request :refer [publish-on-request]]
            [geschichte.platform :refer [create-http-kit-handler!]]
            [cnc.log :refer [setup-logging!]]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)
(setup-logging!)

;; initial test: neuron calibration
;; call in random process subfolder
;; input is neuron-params as json
;; call process
;; output is calibration file as json
;; read files into datomic


(def uri "datomic:mem:///experiments")

(d/delete-database uri)
(d/create-database uri)

(def conn (d/connect uri))

(def schema (read-string (slurp "resources/schema.edn")))

(d/transact  conn schema)

(defn run-experiment! [pre-proc-fn post-proc-fn {:keys [program args] :as exp-params}]
  (let [base-directory (str "/tmp/" (uuid) "/")
        _ (println "base-dir:" base-directory)
        _ (.mkdir (io/file base-directory))
        exp-params (assoc exp-params :base-directory base-directory)
        _ (pre-proc-fn exp-params)
        proc (apply sh (concat [program] args [:dir base-directory]))
        output (post-proc-fn exp-params)]
    {:params exp-params
     :base-directory base-directory
     :process proc
     :output output}))


(defn setup-calibration! [{:keys [neuron-params base-directory]}]
  (with-open [w (io/writer (str base-directory "neuron_params.json"))]
    (json/generate-stream neuron-params w)))

(defn gather-calibration! [{:keys [base-directory]}]
  (with-open [r (io/reader (str base-directory "calibration.json"))]
    (json/parse-stream r true)))


(def test-exp
  (run-experiment! setup-calibration!
                   gather-calibration!
                   {:neuron-params {:cm         0.2,
                                    :tau_m      1.,
                                    :e_rev_E    0.,
                                    :e_rev_I    -100.,
                                    :v_thresh   -50.,
                                    :tau_syn_E  10.,
                                    :v_rest     -50.,
                                    :tau_syn_I  10.,
                                    :v_reset    -50.001,
                                    :tau_refrac 10.,
                                    :i_offset   0.}
                    :program "python"
                    :args ["/home/void/Dokumente/Studium/Informatik/brainz/stdp/model-nmsampling/code/ev_cd/calibrate.py"]}))

(def store (<!! (new-mem-store (atom {"whilo@dopamine.kip" {#uuid "46af3d95-a4e4-4809-b5da-1b95bdef8ebb" {:causal-order {#uuid "182bc393-29f3-5f46-9b28-d882aa851004" []}, :last-update #inst "2015-01-02T17:28:37.038-00:00", :id #uuid "46af3d95-a4e4-4809-b5da-1b95bdef8ebb", :description "ev-cd experiments.", :schema {:type "http://github.com/ghubber/geschichte", :version 1}, :head "calibrate", :branches {"calibrate" #{#uuid "182bc393-29f3-5f46-9b28-d882aa851004"}}, :public false, :pull-requests {}}}, #uuid "182bc393-29f3-5f46-9b28-d882aa851004" {:transactions [[#uuid "157a2813-60b0-597e-97fd-449153eb58a4" #uuid "123ed64b-1e25-59fc-8c5b-038636ae6c3d"]], :parents [], :ts #inst "2015-01-02T17:28:37.038-00:00", :author "whilo@dopamine.kip"}, #uuid "123ed64b-1e25-59fc-8c5b-038636ae6c3d" '(fn replace [old params] params), #uuid "157a2813-60b0-597e-97fd-449153eb58a4" nil, #uuid "1c366126-b11c-555b-9911-81c24c037630" '(fn store-blob-trans [old params] (if *custom-store-fn* (*custom-store-fn* old params) old))}))))

(def peer (server-peer (create-http-kit-handler! "ws://127.0.0.1:31744")
                       store
                       (comp (partial fetch store)
                             (partial publish-on-request store))))

(def stage (<!! (s/create-stage! "whilo@dopamine.kip" peer eval)))

(<!! (s/create-repo! stage "ev-cd experiments." nil "calibrate"))

(defn db-transact [conn txs]
  (d/transact conn (map #(assoc % :db/id (d/tempid :db.part/user))
                        txs)))

((fn calibration->datoms [old params]
   (let [id (uuid params)
         {{{v_rest_min :V_rest_min
            {alpha :alpha
             v_p05 :v_p05} :fit} :calibration
             neuron-params :neuron_parameters} :output} params]
     (db-transact old [{:calib/alpha alpha
                        :calib/v-p05 v_p05
                        :ref/neuron-params (uuid (dissoc neuron-params :_type :pynn_model))
                        :ref/trans-params id}]))) conn test-exp)

(<!! (s/transact stage ["whilo@dopamine.kip" #uuid "46af3d95-a4e4-4809-b5da-1b95bdef8ebb" "calibrate"]
                 test-exp
                 '(fn transact-calibration [old params]
                    (let [id (uuid params)
                          {{{} :calibration} :output}]))))


(let [id (d/tempid :db/user)]
  (d/transact conn [(assoc test-exp :db/id id)]))

(clojure.pprint/pprint test-exp)

(get-in test-exp [:output "calibration"])

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
