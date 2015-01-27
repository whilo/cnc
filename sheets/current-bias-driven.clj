;; gorilla-repl.fileformat = 1

;; **
;;; # Current based bias-driven
;; **

;; @@
(ns mirthful-plateau
  (:require [gorilla-plot.core :as plot]
            [clojure.math.combinatorics :refer [cartesian-product]]
            [cnc.analytics :as a]
            [cnc.execute :as exe]
            [clojure.pprint :refer [pprint]]
            [hasch.core :refer [uuid]]
            [boltzmann.matrix :refer [full-matrix]]
            [clojure.java.shell :refer [sh]]))
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
    (<!? (-get-in store [#uuid "37107994-69aa-5a8f-9fd9-5616298b993b" :output])))
  (def states (cartesian-product [0 1] [0 1]))
  (def experiment 
    (<!? (-get-in store [#uuid "2d3ebd3b-ffc6-522e-84a1-967cc049d68b"])))
  (def blobs (:output experiment)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;mirthful-plateau/blobs</span>","value":"#'mirthful-plateau/blobs"}
;; <=

;; @@
(sh "xdg-open" (str (<!? (-bget store (:training_overview.png blobs) :file))))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:exit</span>","value":":exit"},{"type":"html","content":"<span class='clj-unkown'>0</span>","value":"0"}],"value":"[:exit 0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:out</span>","value":":out"},{"type":"html","content":"<span class='clj-string'>&quot;&quot;</span>","value":"\"\""}],"value":"[:out \"\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:err</span>","value":":err"},{"type":"html","content":"<span class='clj-string'>&quot;&quot;</span>","value":"\"\""}],"value":"[:err \"\"]"}],"value":"{:exit 0, :out \"\", :err \"\"}"}
;; <=

;; @@
(def joint-tensor 
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 blobs) "/dist_joint_sim"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;mirthful-plateau/joint-tensor</span>","value":"#'mirthful-plateau/joint-tensor"}
;; <=

;; @@
(plot/compose 
  (plot/bar-chart states (map (partial get-in joint-tensor) states)
                  :color "red" :opacity 0.5 :plot-size 600) 
  (plot/bar-chart states (map (a/sample-freqs samples) states) 
                  :opacity 0.5))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"610cc5d4-e58c-4a09-ba11-e8c86dd3b3a2","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"610cc5d4-e58c-4a09-ba11-e8c86dd3b3a2","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"610cc5d4-e58c-4a09-ba11-e8c86dd3b3a2","values":[{"x":[0,0],"y":0.972309},{"x":[0,1],"y":0.003441},{"x":[1,0],"y":0.024181},{"x":[1,1],"y":6.9E-5}]},{"name":"d6fe8a9d-3aac-490e-abe5-a33d9ec25106","values":[{"x":[0,0],"y":0.147185280919075},{"x":[0,1],"y":0.26907309889793396},{"x":[1,0],"y":0.4376562237739563},{"x":[1,1],"y":0.14608539640903473}]}],"marks":[{"type":"rect","from":{"data":"610cc5d4-e58c-4a09-ba11-e8c86dd3b3a2"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"d6fe8a9d-3aac-490e-abe5-a33d9ec25106"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"610cc5d4-e58c-4a09-ba11-e8c86dd3b3a2\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"610cc5d4-e58c-4a09-ba11-e8c86dd3b3a2\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"610cc5d4-e58c-4a09-ba11-e8c86dd3b3a2\", :values ({:x (0 0), :y 0.972309} {:x (0 1), :y 0.003441} {:x (1 0), :y 0.024181} {:x (1 1), :y 6.9E-5})} {:name \"d6fe8a9d-3aac-490e-abe5-a33d9ec25106\", :values ({:x (0 0), :y 0.14718528} {:x (0 1), :y 0.2690731} {:x (1 0), :y 0.43765622} {:x (1 1), :y 0.1460854})}), :marks ({:type \"rect\", :from {:data \"610cc5d4-e58c-4a09-ba11-e8c86dd3b3a2\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"d6fe8a9d-3aac-490e-abe5-a33d9ec25106\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; @@

;; @@
