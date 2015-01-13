;; gorilla-repl.fileformat = 1

;; **
;;; # Slightly bigger RBM
;;; 
;;; 4 dimensional RBM training
;; **

;; @@
(ns natural-winter
  (:require [gorilla-plot.core :as plot]
            [clojure.math.combinatorics :refer [cartesian-product]]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
  (require '[cnc.core :refer [state]]
           '[cnc.execute :refer [slurp-bytes]]
           '[clojure.core.matrix :as mat]
           '[clj-hdf5.core :as hdf5]
           '[konserve.protocols :refer [-get-in -bget]]
           '[geschichte.platform :refer [<!?]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))

;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/repo-id</span>","value":"#'natural-winter/repo-id"}
;; <=

;; @@
(def samples (<!? (-get-in store [#uuid "2e354ab2-5629-53f0-88d6-79e405f61217" :output])))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/samples</span>","value":"#'natural-winter/samples"}
;; <=

;; @@
(def states (cartesian-product [0 1] [0 1] [0 1] [0 1]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/states</span>","value":"#'natural-winter/states"}
;; <=

;; @@
(defn sample-freqs [samples] 
  (let [c (count samples)] 
  (->> (frequencies samples)
       (map (fn [[k v]] [k (float (/ v c))]))
  	   (into {}))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/sample-freqs</span>","value":"#'natural-winter/sample-freqs"}
;; <=

;; @@
(plot/compose 
  #_(plot/bar-chart states (map (partial get-in joint-tensor) states) :color "red" :opacity 0.5) 
  (plot/bar-chart states (map (sample-freqs samples) states) :opacity 0.5 :plot-size 600))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"c03acf31-40f3-4fd5-a977-c52f4f7c95e1","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"c03acf31-40f3-4fd5-a977-c52f4f7c95e1","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"c03acf31-40f3-4fd5-a977-c52f4f7c95e1","values":[{"x":[0,0,0,0],"y":0.03659633919596672},{"x":[0,0,0,1],"y":0.042995698750019073},{"x":[0,0,1,0],"y":0.06619337946176529},{"x":[0,0,1,1],"y":0.08059193938970566},{"x":[0,1,0,0],"y":0.03179682046175003},{"x":[0,1,0,1],"y":0.03779622167348862},{"x":[0,1,1,0],"y":0.05639436095952988},{"x":[0,1,1,1],"y":0.0765923410654068},{"x":[1,0,0,0],"y":0.04279572144150734},{"x":[1,0,0,1],"y":0.05509449169039726},{"x":[1,0,1,0],"y":0.08199179917573929},{"x":[1,0,1,1],"y":0.1148885115981102},{"x":[1,1,0,0],"y":0.03969603031873703},{"x":[1,1,0,1],"y":0.05489451065659523},{"x":[1,1,1,0],"y":0.07429257035255432},{"x":[1,1,1,1],"y":0.10738926380872726}]}],"marks":[{"type":"rect","from":{"data":"c03acf31-40f3-4fd5-a977-c52f4f7c95e1"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"c03acf31-40f3-4fd5-a977-c52f4f7c95e1\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"c03acf31-40f3-4fd5-a977-c52f4f7c95e1\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"c03acf31-40f3-4fd5-a977-c52f4f7c95e1\", :values ({:x (0 0 0 0), :y 0.03659634} {:x (0 0 0 1), :y 0.0429957} {:x (0 0 1 0), :y 0.06619338} {:x (0 0 1 1), :y 0.08059194} {:x (0 1 0 0), :y 0.03179682} {:x (0 1 0 1), :y 0.03779622} {:x (0 1 1 0), :y 0.05639436} {:x (0 1 1 1), :y 0.07659234} {:x (1 0 0 0), :y 0.04279572} {:x (1 0 0 1), :y 0.05509449} {:x (1 0 1 0), :y 0.0819918} {:x (1 0 1 1), :y 0.11488851} {:x (1 1 0 0), :y 0.03969603} {:x (1 1 0 1), :y 0.05489451} {:x (1 1 1 0), :y 0.07429257} {:x (1 1 1 1), :y 0.107389264})}), :marks ({:type \"rect\", :from {:data \"c03acf31-40f3-4fd5-a977-c52f4f7c95e1\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; @@

;; @@
