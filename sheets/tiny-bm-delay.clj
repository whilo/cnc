;; gorilla-repl.fileformat = 1

;; **
;;; # Tiny BM with dendritic delay
;; **

;; @@
(ns tiny-bm-with-dendritic-delay
  (:require [gorilla-plot.core :as plot]
            [clojure.math.combinatorics :refer [cartesian-product]]
            [cnc.analytics :as a]
            [cnc.execute :as exe]
            [clojure.pprint :refer [pprint]]
            [hasch.core :refer [uuid]]
            [boltzmann.matrix :refer [full-matrix]]))
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
    (<!? (-get-in store [#uuid "19a5da17-6b4c-548c-8b6f-24faf06c089c" :output])))
  (def states (cartesian-product [0 1]))
  (def experiment 
    (<!? (-get-in store [#uuid "1e0eeb21-7ac3-5972-8a5c-d384a25445c4"])))
  (def blobs (:output experiment)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;tiny-bm-with-dendritic-delay/blobs</span>","value":"#'tiny-bm-with-dendritic-delay/blobs"}
;; <=

;; @@

;; @@
