(ns slightly-bigger)

(ns natural-winter
  (:require [gorilla-plot.core :as plot]
            [clojure.math.combinatorics :refer [cartesian-product]]
            [cnc.analytics :as a]
            [clojure.pprint :refer [pprint]]))
  (require '[cnc.core :refer [state]]
           '[cnc.execute :refer [slurp-bytes]]
           '[clojure.core.matrix :as mat]
           '[clj-hdf5.core :as hdf5]
           '[konserve.protocols :refer [-get-in -bget]]
           '[geschichte.platform :refer [<!?]])
(do
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))
  (def samples
    (<!? (-get-in store [#uuid "2e354ab2-5629-53f0-88d6-79e405f61217" :output])))
  (def states (cartesian-product [0 1] [0 1] [0 1] [0 1]))
  (def experiment
    (<!? (-get-in store [#uuid "0556b31a-f3a5-5605-85c3-2ba9fa1e23b0"])))
  (def blobs (:output experiment)))


;; ....

![history](project-files/repo/store/%23uuid%20"0b58eba0-bb60-5666-8ef1-7c5f93380ff5")
