(ns cnc.exp.calibrate
  (:require [cnc.eval-map :refer [find-fn]]
            [cnc.execute :refer [run-experiment!]]
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
  {:output (with-open [r (io/reader (str base-directory "calibration.json"))]
             (json/parse-stream r true))})

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
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in]]
           '[geschichte.platform :refer [<!?]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))
  (def source-base-path (get-in @state [:config :source-base-path]))

  (future
    (let [source-path (str source-base-path "model-nmsampling/code/ev_cd/calibrate.py")]
      (def test-exp
        (run-experiment! setup-calibration!
                         gather-calibration!
                         {:neuron-params neuron-params
                          :source-path source-path
                          :args ["python" source-path]}))))

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "calibrate"]
                   (find-fn 'add-neuron-params)
                   neuron-params))



  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "calibrate"]
                   (find-fn 'calibration->datoms)
                   test-exp))

  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"calibrate"}}})))
