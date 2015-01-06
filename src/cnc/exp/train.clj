(ns cnc.exp.train
  (:require [cnc.execute :refer [run-experiment!]]
            [cnc.repo :refer [stage repo-id store]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [konserve.protocols :refer [-get-in]]
            [hasch.core :refer [uuid]]
            [clojure.core.async :refer [>!!]]
            [clojure.java.io :as io]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn write-json [base-dir name coll]
  (with-open [w (io/writer (str base-dir name))]
    (json/generate-stream coll w)))

(defn setup-training! [base-dir {:keys [neuron-params training-params
                                        data-id calibration-id]}]
  (write-json base-dir "neuron_params.json" neuron-params)
  (write-json base-dir "training_params.json" training-params)
  (write-json base-dir "calibration.json" (<!? (-get-in store [calibration-id :output])))
  (write-json base-dir "data.json" (<!? (-get-in store [data-id :output]))))

(defn gather-training! [base-directory _]
  #_(with-open [r (io/reader (str base-directory "calibration.json"))]
    (json/parse-stream r true)))


(def training-params {"epochs" 1,
                      "dt" 0.01,
                      "burn_in_time" 0.,
                      "phase_duration" 100.0,
                      "learning_rate" 1e-6,
                      "stdp_burnin" 10.0
                      "sampling_time" 1e6})

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
    (let [source-path "/home/void/Dokumente/Studium/Informatik/brainz/stdp/model-nmsampling/code/ev_cd/stdp.py"]
      (def test-exp
        (run-experiment! setup-training!
                         gather-training!
                         {:neuron-params neuron-params
                          :training-params training-params
                          :calibration-id #uuid "050da733-dff1-5f53-b6d4-11aedfbbada2"
                          :data-id #uuid "00dc768e-cc2d-5e7c-8a39-50011f5ecf38"
                          :source-path source-path
                          :args ["python" source-path]}))))

  (clojure.pprint/pprint test-exp)

  (def hist
    (<!? (s/commit-history-values store
                                  (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :causal-order])
                                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "calibrate"])))))

  (first (:transactions (second hist)))
  (uuid (-> hist second :transactions second first))





  (def db (hdf5/create "/tmp/test.h5"))
  (def db (hdf5/open "experiments/06363154-dae4-4b95-9892-921671d91000/weights_history.h5"))

  (def group (hdf5/create-group db "spike-train"))

  (def dataset (hdf5/create-dataset group "neuron-1" "hello world!"))

  (def array-dataset (hdf5/create-dataset group "neuron-2" (int-array 10 (int 1023))))

  (count (hdf5/read  (hdf5/get-dataset db "/weight_history")))
  (vec (first (hdf5/read  (hdf5/get-dataset db "/weight_history"))))

  (hdf5/members db)

  (hdf5/close db))
