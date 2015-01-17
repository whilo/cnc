;; gorilla-repl.fileformat = 1

;; **
;;; # Slightly bigger RBM (4 visible, 12 hidden)
;; **

;; @@
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
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/blobs</span>","value":"#'natural-winter/blobs"}
;; <=

;; **
;;; ## Parameters
;; **

;; @@
(pprint (get-in experiment [:exp-params :training-params]))
;; @@
;; ->
;;; {:h_count 12,
;;;  :dt 0.01,
;;;  :epochs 1,
;;;  :burn_in_time 0.0,
;;;  :learning_rate 1.0E-6,
;;;  :sampling_time 1000000.0,
;;;  :phase_duration 100.0,
;;;  :weight_recording_interval 100.0,
;;;  :stdp_burnin 10.0}
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; **
;;; ## Trained distribution vs. data distribution
;; **

;; @@
(def joint-tensor 
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 blobs) "/dist_joint_sim"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/joint-tensor</span>","value":"#'natural-winter/joint-tensor"}
;; <=

;; @@
(plot/compose 
  (plot/bar-chart states (map (partial get-in joint-tensor) states) 
                  :color "red" :opacity 0.5 :plot-size 600) 
  (plot/bar-chart states (map (sample-freqs samples) states) 
                  :opacity 0.5))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"a746969e-2fc1-4c9b-8148-f1392565a15e","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"a746969e-2fc1-4c9b-8148-f1392565a15e","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"a746969e-2fc1-4c9b-8148-f1392565a15e","values":[{"x":[0,0,0,0],"y":0.02478780000003078},{"x":[0,0,0,1],"y":0.040264200000013455},{"x":[0,0,1,0],"y":0.05168179999998724},{"x":[0,0,1,1],"y":0.07973429999997816},{"x":[0,1,0,0],"y":0.024794400000009452},{"x":[0,1,0,1],"y":0.04056299999996228},{"x":[0,1,1,0],"y":0.04961339999997616},{"x":[0,1,1,1],"y":0.08059110000004246},{"x":[1,0,0,0],"y":0.039417799999995624},{"x":[1,0,0,1],"y":0.06127639999998757},{"x":[1,0,1,0],"y":0.07742149999998114},{"x":[1,0,1,1],"y":0.12047840000002621},{"x":[1,1,0,0],"y":0.0399851000000115},{"x":[1,1,0,1],"y":0.06501129999998934},{"x":[1,1,1,0],"y":0.0773582000000081},{"x":[1,1,1,1],"y":0.1270213000000005}]},{"name":"27919ef5-9dc3-4b63-be25-f26bab471895","values":[{"x":[0,0,0,0],"y":0.03659633919596672},{"x":[0,0,0,1],"y":0.042995698750019073},{"x":[0,0,1,0],"y":0.06619337946176529},{"x":[0,0,1,1],"y":0.08059193938970566},{"x":[0,1,0,0],"y":0.03179682046175003},{"x":[0,1,0,1],"y":0.03779622167348862},{"x":[0,1,1,0],"y":0.05639436095952988},{"x":[0,1,1,1],"y":0.0765923410654068},{"x":[1,0,0,0],"y":0.04279572144150734},{"x":[1,0,0,1],"y":0.05509449169039726},{"x":[1,0,1,0],"y":0.08199179917573929},{"x":[1,0,1,1],"y":0.1148885115981102},{"x":[1,1,0,0],"y":0.03969603031873703},{"x":[1,1,0,1],"y":0.05489451065659523},{"x":[1,1,1,0],"y":0.07429257035255432},{"x":[1,1,1,1],"y":0.10738926380872726}]}],"marks":[{"type":"rect","from":{"data":"a746969e-2fc1-4c9b-8148-f1392565a15e"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"27919ef5-9dc3-4b63-be25-f26bab471895"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"a746969e-2fc1-4c9b-8148-f1392565a15e\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"a746969e-2fc1-4c9b-8148-f1392565a15e\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"a746969e-2fc1-4c9b-8148-f1392565a15e\", :values ({:x (0 0 0 0), :y 0.02478780000003078} {:x (0 0 0 1), :y 0.040264200000013455} {:x (0 0 1 0), :y 0.05168179999998724} {:x (0 0 1 1), :y 0.07973429999997816} {:x (0 1 0 0), :y 0.024794400000009452} {:x (0 1 0 1), :y 0.04056299999996228} {:x (0 1 1 0), :y 0.04961339999997616} {:x (0 1 1 1), :y 0.08059110000004246} {:x (1 0 0 0), :y 0.039417799999995624} {:x (1 0 0 1), :y 0.06127639999998757} {:x (1 0 1 0), :y 0.07742149999998114} {:x (1 0 1 1), :y 0.12047840000002621} {:x (1 1 0 0), :y 0.0399851000000115} {:x (1 1 0 1), :y 0.06501129999998934} {:x (1 1 1 0), :y 0.0773582000000081} {:x (1 1 1 1), :y 0.1270213000000005})} {:name \"27919ef5-9dc3-4b63-be25-f26bab471895\", :values ({:x (0 0 0 0), :y 0.03659634} {:x (0 0 0 1), :y 0.0429957} {:x (0 0 1 0), :y 0.06619338} {:x (0 0 1 1), :y 0.08059194} {:x (0 1 0 0), :y 0.03179682} {:x (0 1 0 1), :y 0.03779622} {:x (0 1 1 0), :y 0.05639436} {:x (0 1 1 1), :y 0.07659234} {:x (1 0 0 0), :y 0.04279572} {:x (1 0 0 1), :y 0.05509449} {:x (1 0 1 0), :y 0.0819918} {:x (1 0 1 1), :y 0.11488851} {:x (1 1 0 0), :y 0.03969603} {:x (1 1 0 1), :y 0.05489451} {:x (1 1 1 0), :y 0.07429257} {:x (1 1 1 1), :y 0.107389264})}), :marks ({:type \"rect\", :from {:data \"a746969e-2fc1-4c9b-8148-f1392565a15e\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"27919ef5-9dc3-4b63-be25-f26bab471895\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; **
;;; ## Final weight distribution (last 1000 simulation steps)
;; **

;; @@
(def weights-history-tensor 
  (a/get-hdf5-tensor store (:weights_history.h5 blobs) "/weights_history"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/weights-history-tensor</span>","value":"#'natural-winter/weights-history-tensor"}
;; <=

;; @@
(plot/histogram (apply concat (take-last 1000 weights-history-tensor)) 
                :bins 40 :plot-size 600)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"3ea15fcb-4314-4b1f-afc9-dfb51a82ed74","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"3ea15fcb-4314-4b1f-afc9-dfb51a82ed74","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"3ea15fcb-4314-4b1f-afc9-dfb51a82ed74"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"3ea15fcb-4314-4b1f-afc9-dfb51a82ed74","values":[{"x":-0.004089876414684562,"y":0},{"x":-0.003918789507438139,"y":2000.0},{"x":-0.003747702600191716,"y":1000.0},{"x":-0.003576615692945293,"y":2000.0},{"x":-0.0034055287856988702,"y":1000.0},{"x":-0.0032344418784524473,"y":2000.0},{"x":-0.0030633549712060244,"y":1000.0},{"x":-0.0028922680639596014,"y":2000.0},{"x":-0.0027211811567131785,"y":0.0},{"x":-0.0025500942494667556,"y":1000.0},{"x":-0.0023790073422203326,"y":0.0},{"x":-0.0022079204349739097,"y":1000.0},{"x":-0.0020368335277274868,"y":2000.0},{"x":-0.001865746620481064,"y":0.0},{"x":-0.0016946597132346413,"y":1000.0},{"x":-0.0015235728059882186,"y":0.0},{"x":-0.0013524858987417959,"y":0.0},{"x":-0.0011813989914953732,"y":0.0},{"x":-0.0010103120842489505,"y":0.0},{"x":-8.392251770025276E-4,"y":1000.0},{"x":-6.681382697561048E-4,"y":1000.0},{"x":-4.97051362509682E-4,"y":1000.0},{"x":-3.2596445526325915E-4,"y":3000.0},{"x":-1.5487754801683635E-4,"y":6000.0},{"x":1.6209359229586446E-5,"y":6000.0},{"x":1.8729626647600924E-4,"y":4000.0},{"x":3.5838317372243204E-4,"y":7000.0},{"x":5.294700809688549E-4,"y":12000.0},{"x":7.005569882152777E-4,"y":8000.0},{"x":8.716438954617005E-4,"y":3000.0},{"x":0.0010427308027081232,"y":6000.0},{"x":0.001213817709954546,"y":2000.0},{"x":0.0013849046172009687,"y":1000.0},{"x":0.0015559915244473914,"y":3000.0},{"x":0.001727078431693814,"y":3000.0},{"x":0.0018981653389402368,"y":5000.0},{"x":0.0020692522461866598,"y":1000.0},{"x":0.0022403391534330827,"y":1000.0},{"x":0.0024114260606795056,"y":3000.0},{"x":0.0025825129679259286,"y":1000.0},{"x":0.0027535998751723515,"y":2000.0},{"x":0.0029246867824187744,"y":0}]}],"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"3ea15fcb-4314-4b1f-afc9-dfb51a82ed74\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"3ea15fcb-4314-4b1f-afc9-dfb51a82ed74\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"3ea15fcb-4314-4b1f-afc9-dfb51a82ed74\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"3ea15fcb-4314-4b1f-afc9-dfb51a82ed74\", :values ({:x -0.004089876414684562, :y 0} {:x -0.003918789507438139, :y 2000.0} {:x -0.003747702600191716, :y 1000.0} {:x -0.003576615692945293, :y 2000.0} {:x -0.0034055287856988702, :y 1000.0} {:x -0.0032344418784524473, :y 2000.0} {:x -0.0030633549712060244, :y 1000.0} {:x -0.0028922680639596014, :y 2000.0} {:x -0.0027211811567131785, :y 0.0} {:x -0.0025500942494667556, :y 1000.0} {:x -0.0023790073422203326, :y 0.0} {:x -0.0022079204349739097, :y 1000.0} {:x -0.0020368335277274868, :y 2000.0} {:x -0.001865746620481064, :y 0.0} {:x -0.0016946597132346413, :y 1000.0} {:x -0.0015235728059882186, :y 0.0} {:x -0.0013524858987417959, :y 0.0} {:x -0.0011813989914953732, :y 0.0} {:x -0.0010103120842489505, :y 0.0} {:x -8.392251770025276E-4, :y 1000.0} {:x -6.681382697561048E-4, :y 1000.0} {:x -4.97051362509682E-4, :y 1000.0} {:x -3.2596445526325915E-4, :y 3000.0} {:x -1.5487754801683635E-4, :y 6000.0} {:x 1.6209359229586446E-5, :y 6000.0} {:x 1.8729626647600924E-4, :y 4000.0} {:x 3.5838317372243204E-4, :y 7000.0} {:x 5.294700809688549E-4, :y 12000.0} {:x 7.005569882152777E-4, :y 8000.0} {:x 8.716438954617005E-4, :y 3000.0} {:x 0.0010427308027081232, :y 6000.0} {:x 0.001213817709954546, :y 2000.0} {:x 0.0013849046172009687, :y 1000.0} {:x 0.0015559915244473914, :y 3000.0} {:x 0.001727078431693814, :y 3000.0} {:x 0.0018981653389402368, :y 5000.0} {:x 0.0020692522461866598, :y 1000.0} {:x 0.0022403391534330827, :y 1000.0} {:x 0.0024114260606795056, :y 3000.0} {:x 0.0025825129679259286, :y 1000.0} {:x 0.0027535998751723515, :y 2000.0} {:x 0.0029246867824187744, :y 0})}], :width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; @@
(def bias-history-tensor 
  (a/get-hdf5-tensor store (:bias_history.h5 blobs) "/bias_history"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/bias-history-tensor</span>","value":"#'natural-winter/bias-history-tensor"}
;; <=

;; **
;;; ## Final bias distribution
;; **

;; @@
(plot/histogram (apply concat (take-last 1000 bias-history-tensor)) 
                :bins 30 :plot-size 600)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"a66a3428-8b8a-4771-a3c4-ef8c8fa6ae95","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"a66a3428-8b8a-4771-a3c4-ef8c8fa6ae95","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"a66a3428-8b8a-4771-a3c4-ef8c8fa6ae95"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"a66a3428-8b8a-4771-a3c4-ef8c8fa6ae95","values":[{"x":-0.0013071599196780067,"y":0},{"x":-0.0012303488362137719,"y":1000.0},{"x":-0.001153537752749537,"y":0.0},{"x":-0.0010767266692853022,"y":0.0},{"x":-9.999155858210673E-4,"y":0.0},{"x":-9.231045023568324E-4,"y":0.0},{"x":-8.462934188925974E-4,"y":0.0},{"x":-7.694823354283624E-4,"y":0.0},{"x":-6.926712519641275E-4,"y":2000.0},{"x":-6.158601684998925E-4,"y":1000.0},{"x":-5.390490850356575E-4,"y":0.0},{"x":-4.6223800157142264E-4,"y":1000.0},{"x":-3.8542691810718773E-4,"y":0.0},{"x":-3.0861583464295283E-4,"y":0.0},{"x":-2.3180475117871792E-4,"y":1000.0},{"x":-1.5499366771448301E-4,"y":1000.0},{"x":-7.81825842502481E-5,"y":1000.0},{"x":-1.3715007860131735E-6,"y":1000.0},{"x":7.543958267822175E-5,"y":0.0},{"x":1.5225066614245667E-4,"y":0.0},{"x":2.2906174960669157E-4,"y":1000.0},{"x":3.058728330709265E-4,"y":1000.0},{"x":3.826839165351614E-4,"y":0.0},{"x":4.594949999993963E-4,"y":2000.0},{"x":5.363060834636313E-4,"y":0.0},{"x":6.131171669278662E-4,"y":0.0},{"x":6.899282503921012E-4,"y":0.0},{"x":7.667393338563361E-4,"y":1000.0},{"x":8.435504173205711E-4,"y":0.0},{"x":9.203615007848061E-4,"y":1000.0},{"x":9.97172584249041E-4,"y":1000.0},{"x":0.0010739836677132759,"y":0}]}],"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"a66a3428-8b8a-4771-a3c4-ef8c8fa6ae95\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"a66a3428-8b8a-4771-a3c4-ef8c8fa6ae95\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"a66a3428-8b8a-4771-a3c4-ef8c8fa6ae95\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"a66a3428-8b8a-4771-a3c4-ef8c8fa6ae95\", :values ({:x -0.0013071599196780067, :y 0} {:x -0.0012303488362137719, :y 1000.0} {:x -0.001153537752749537, :y 0.0} {:x -0.0010767266692853022, :y 0.0} {:x -9.999155858210673E-4, :y 0.0} {:x -9.231045023568324E-4, :y 0.0} {:x -8.462934188925974E-4, :y 0.0} {:x -7.694823354283624E-4, :y 0.0} {:x -6.926712519641275E-4, :y 2000.0} {:x -6.158601684998925E-4, :y 1000.0} {:x -5.390490850356575E-4, :y 0.0} {:x -4.6223800157142264E-4, :y 1000.0} {:x -3.8542691810718773E-4, :y 0.0} {:x -3.0861583464295283E-4, :y 0.0} {:x -2.3180475117871792E-4, :y 1000.0} {:x -1.5499366771448301E-4, :y 1000.0} {:x -7.81825842502481E-5, :y 1000.0} {:x -1.3715007860131735E-6, :y 1000.0} {:x 7.543958267822175E-5, :y 0.0} {:x 1.5225066614245667E-4, :y 0.0} {:x 2.2906174960669157E-4, :y 1000.0} {:x 3.058728330709265E-4, :y 1000.0} {:x 3.826839165351614E-4, :y 0.0} {:x 4.594949999993963E-4, :y 2000.0} {:x 5.363060834636313E-4, :y 0.0} {:x 6.131171669278662E-4, :y 0.0} {:x 6.899282503921012E-4, :y 0.0} {:x 7.667393338563361E-4, :y 1000.0} {:x 8.435504173205711E-4, :y 0.0} {:x 9.203615007848061E-4, :y 1000.0} {:x 9.97172584249041E-4, :y 1000.0} {:x 0.0010739836677132759, :y 0})}], :width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; **
;;; ## History of weights in training (avg pink, variance green)
;; **

;; @@
(def weights-history-avgs-tensor 
  (a/get-hdf5-tensor store (:weight_avgs.h5 blobs) "/weight_avgs"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;natural-winter/weights-history-avgs-tensor</span>","value":"#'natural-winter/weights-history-avgs-tensor"}
;; <=

;; @@
#_(plot/compose
  (plot/list-plot (map first weights-history-avgs-tensor) 
                  :joined true :plot-range [:all [-0.0004 0.0020]]  :plot-size 600)
  (plot/list-plot (map second weights-history-avgs-tensor) 
                  :joined true :color "green"))
;; @@

;; **
;;; ![history](project-files/repo/store/%23uuid%20"0b58eba0-bb60-5666-8ef1-7c5f93380ff5")
;; **

;; @@

;; @@
