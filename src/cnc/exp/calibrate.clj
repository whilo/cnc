(ns cnc.exp.calibrate
  (:require [cnc.execute :refer [run-experiment!]]
            [cnc.repo :refer [stage repo-id]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [hasch.core :refer [uuid]]
            [clojure.core.async :refer [>!!]]
            [clojure.java.io :as io]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn setup-calibration! [base-directory {:keys [neuron-params]}]
  (with-open [w (io/writer (str base-directory "neuron_params.json"))]
    (json/generate-stream neuron-params w)))

(defn gather-calibration! [base-directory _]
  (with-open [r (io/reader (str base-directory "calibration.json"))]
    (json/parse-stream r true)))

(def neuron-params {:cm         0.2,
                    :tau_m      1.,
                    :e_rev_E    0.,
                    :e_rev_I    -100.,
                    :v_thresh   -50.,
                    :tau_syn_E  10.,
                    :v_rest     -50.,
                    :tau_syn_I  10.,
                    :v_reset    -50.001,
                    :tau_refrac 10.,
                    :i_offset   0.})

(comment
  (future
    (let [source-path "/home/void/Dokumente/Studium/Informatik/brainz/stdp/model-nmsampling/code/ev_cd/calibrate.py"]
      (def test-exp
        (run-experiment! setup-calibration!
                         gather-calibration!
                         {:neuron-params neuron-params
                          :source (slurp source-path)
                          :args ["python" source-path]}))))

  (def calibration->datoms-val
    '(fn calibration->datoms [conn params]
       (let [id (uuid params)
             {{{v_rest_min :V_rest_min
                {alpha :alpha
                 v_p05 :v_p05} :fit} :calibration
                 neuron-params :neuron_parameters
                 source :source} :output} params]
         (db-transact conn [{:val/id (uuid (:output params))
                             :source/id (uuid source)
                             :calib/alpha alpha
                             :calib/v-p05 v_p05
                             :ref/neuron-params (uuid (dissoc neuron-params :_type :pynn_model))
                             :ref/trans-params id}])
         conn)))

  (<!? (s/transact stage ["whilo@dopamine.kip" repo-id "calibrate"]
                   neuron-params
                   '(fn add-neuron-params [conn params]
                      (let [namespaced (->> params
                                            (map (fn [[k v]]
                                                   [(keyword "neuron" (name k)) v]))
                                            (into {}))]
                        (db-transact conn [(assoc namespaced :val/id (uuid params))]))
                      conn)))



  (<!? (s/transact stage ["whilo@dopamine.kip" repo-id "calibrate"]
                   test-exp
                   calibration->datoms-val))

  (<!? (s/commit! stage {"whilo@dopamine.kip" {repo-id #{"calibrate"}}})))
