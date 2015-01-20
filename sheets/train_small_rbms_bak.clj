(ns train-small-rbms-bak)

(ns affectionate-brook
  (:require [gorilla-plot.core :as plot]
            [gg4clj.core :as gg4clj]
            [clojure.math.combinatorics :refer [cartesian-product]]
            [clojure.pprint :refer [pprint]]))
  (require '[cnc.core :refer [state]]
           '[cnc.analytics :as a]
           '[konserve.protocols :refer [-get-in]]
           '[geschichte.platform :refer [<!?]])
(do
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))
  (def samples
    (<!? (-get-in store [#uuid "12515c62-4bc6-5804-a870-84c7bee1b85f" :output])))
  (def states (cartesian-product [0 1] [0 1] [0 1]))
  (def experiment
    (<!? (-get-in store [#uuid "17731511-7e26-5817-9242-85bf2e945a91"])))
  (def blobs (:output experiment)))



(pprint (get-in experiment [:exp-params :training-params]))


(def joint-tensor
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 blobs) "/dist_joint_sim"))


(plot/compose
  (plot/bar-chart states (map (partial get-in joint-tensor) states)
                  :color "red" :opacity 0.5 :plot-size 600)
  (plot/bar-chart states (map (a/sample-freqs samples) states) :opacity 0.5))



(def weights-history-tensor (a/get-hdf5-tensor store (:weights_history.h5 blobs) "/weights_history"))


(plot/histogram (apply concat (take-last 1000 weights-history-tensor))
                :bins 20 :plot-size 600)


(def bias-history-tensor
  (a/get-hdf5-tensor store (:bias_history.h5 blobs) "/bias_history"))


(plot/histogram (apply concat (take-last 1000 bias-history-tensor))
                :bins 20 :plot-size 600)


(def weights-history-avgs-tensor
  (a/get-hdf5-tensor store (:weight_avgs.h5 blobs) "/weight_avgs"))



(plot/compose
  (plot/list-plot (map first weights-history-avgs-tensor)
                  :joined true :plot-range [:all [-0.0004 0.0020]] :plot-size 600)
  (plot/list-plot (map second weights-history-avgs-tensor)
                  :joined true :color "green"))


![history](project-files/repo/store/%23uuid%20"26fc6a44-674b-5928-8e17-d3dfa539c92a")
