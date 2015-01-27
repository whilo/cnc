;; gorilla-repl.fileformat = 1

;; **
;;; # Bias-driven investigation
;; **

;; @@
(ns moonlit-curve
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
    (<!? (-get-in store [#uuid "37107994-69aa-5a8f-9fd9-5616298b993b" :output])))
  (def states (cartesian-product [0 1] [0 1]))
  (def experiment 
    (<!? (-get-in store [#uuid "14d3da99-9a58-5684-b871-84b025c896be"])))
  (def blobs (:output experiment)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/blobs</span>","value":"#'moonlit-curve/blobs"}
;; <=

;; @@
(def joint-tensor 
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 blobs) "/dist_joint_sim"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/joint-tensor</span>","value":"#'moonlit-curve/joint-tensor"}
;; <=

;; @@
(plot/compose 
  (plot/bar-chart states (map (partial get-in joint-tensor) states) 
                  :color "red" :opacity 0.5 :plot-size 600) 
  (plot/bar-chart states (map (a/sample-freqs samples) states) 
                  :opacity 0.5))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"6a6e1d6c-b98a-47a0-a4d5-9eed0c9c36ec","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"6a6e1d6c-b98a-47a0-a4d5-9eed0c9c36ec","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"6a6e1d6c-b98a-47a0-a4d5-9eed0c9c36ec","values":[{"x":[0,0],"y":0.218757199999952},{"x":[0,1],"y":0.17091220000004792},{"x":[1,0],"y":0.340542800000048},{"x":[1,1],"y":0.26978779999995206}]},{"name":"3f217cb7-7746-42e1-9de4-b171eef8b3bc","values":[{"x":[0,0],"y":0.147185280919075},{"x":[0,1],"y":0.26907309889793396},{"x":[1,0],"y":0.4376562237739563},{"x":[1,1],"y":0.14608539640903473}]}],"marks":[{"type":"rect","from":{"data":"6a6e1d6c-b98a-47a0-a4d5-9eed0c9c36ec"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"3f217cb7-7746-42e1-9de4-b171eef8b3bc"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"6a6e1d6c-b98a-47a0-a4d5-9eed0c9c36ec\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"6a6e1d6c-b98a-47a0-a4d5-9eed0c9c36ec\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"6a6e1d6c-b98a-47a0-a4d5-9eed0c9c36ec\", :values ({:x (0 0), :y 0.218757199999952} {:x (0 1), :y 0.17091220000004792} {:x (1 0), :y 0.340542800000048} {:x (1 1), :y 0.26978779999995206})} {:name \"3f217cb7-7746-42e1-9de4-b171eef8b3bc\", :values ({:x (0 0), :y 0.14718528} {:x (0 1), :y 0.2690731} {:x (1 0), :y 0.43765622} {:x (1 1), :y 0.1460854})}), :marks ({:type \"rect\", :from {:data \"6a6e1d6c-b98a-47a0-a4d5-9eed0c9c36ec\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"3f217cb7-7746-42e1-9de4-b171eef8b3bc\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; @@
(<!? (-get-in store [#uuid "14d3da99-9a58-5684-b871-84b025c896be" :output]))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:spike_trains.h5</span>","value":":spike_trains.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;1ceee65a-1c93-5d75-8f8f-56ace0a9aecc&quot;</span>","value":"#uuid \"1ceee65a-1c93-5d75-8f8f-56ace0a9aecc\""}],"value":"[:spike_trains.h5 #uuid \"1ceee65a-1c93-5d75-8f8f-56ace0a9aecc\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:weight_avgs.h5</span>","value":":weight_avgs.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;3641f690-d675-533c-a6ad-c22f25a9b796&quot;</span>","value":"#uuid \"3641f690-d675-533c-a6ad-c22f25a9b796\""}],"value":"[:weight_avgs.h5 #uuid \"3641f690-d675-533c-a6ad-c22f25a9b796\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:dist_joint_sim.h5</span>","value":":dist_joint_sim.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;16dfd0a8-0f0d-587b-8618-729fd1ba1e4e&quot;</span>","value":"#uuid \"16dfd0a8-0f0d-587b-8618-729fd1ba1e4e\""}],"value":"[:dist_joint_sim.h5 #uuid \"16dfd0a8-0f0d-587b-8618-729fd1ba1e4e\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:weights_history.h5</span>","value":":weights_history.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;1bfb84c1-4bad-5bd7-8f05-1a32b384bab1&quot;</span>","value":"#uuid \"1bfb84c1-4bad-5bd7-8f05-1a32b384bab1\""}],"value":"[:weights_history.h5 #uuid \"1bfb84c1-4bad-5bd7-8f05-1a32b384bab1\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:dist_joint.pdf</span>","value":":dist_joint.pdf"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;2303baed-abdd-566d-bc93-cf6469ad784c&quot;</span>","value":"#uuid \"2303baed-abdd-566d-bc93-cf6469ad784c\""}],"value":"[:dist_joint.pdf #uuid \"2303baed-abdd-566d-bc93-cf6469ad784c\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:dist_joint.png</span>","value":":dist_joint.png"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;149e26a0-ca95-51b8-ab51-a2b6b3127268&quot;</span>","value":"#uuid \"149e26a0-ca95-51b8-ab51-a2b6b3127268\""}],"value":"[:dist_joint.png #uuid \"149e26a0-ca95-51b8-ab51-a2b6b3127268\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:training_overview.png</span>","value":":training_overview.png"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;25711115-4a98-5d3a-b6f2-f14e13f7e00b&quot;</span>","value":"#uuid \"25711115-4a98-5d3a-b6f2-f14e13f7e00b\""}],"value":"[:training_overview.png #uuid \"25711115-4a98-5d3a-b6f2-f14e13f7e00b\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:bias_history.h5</span>","value":":bias_history.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;30188c5a-9cae-57a8-a186-c2850f0dc31f&quot;</span>","value":"#uuid \"30188c5a-9cae-57a8-a186-c2850f0dc31f\""}],"value":"[:bias_history.h5 #uuid \"30188c5a-9cae-57a8-a186-c2850f0dc31f\"]"}],"value":"{:spike_trains.h5 #uuid \"1ceee65a-1c93-5d75-8f8f-56ace0a9aecc\", :weight_avgs.h5 #uuid \"3641f690-d675-533c-a6ad-c22f25a9b796\", :dist_joint_sim.h5 #uuid \"16dfd0a8-0f0d-587b-8618-729fd1ba1e4e\", :weights_history.h5 #uuid \"1bfb84c1-4bad-5bd7-8f05-1a32b384bab1\", :dist_joint.pdf #uuid \"2303baed-abdd-566d-bc93-cf6469ad784c\", :dist_joint.png #uuid \"149e26a0-ca95-51b8-ab51-a2b6b3127268\", :training_overview.png #uuid \"25711115-4a98-5d3a-b6f2-f14e13f7e00b\", :bias_history.h5 #uuid \"30188c5a-9cae-57a8-a186-c2850f0dc31f\"}"}
;; <=

;; @@
(def weights-history-tensor 
  (a/get-hdf5-tensor store (:weights_history.h5 blobs) "/weights_history"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/weights-history-tensor</span>","value":"#'moonlit-curve/weights-history-tensor"}
;; <=

;; @@
(plot/histogram (apply concat (take-last 1000 weights-history-tensor)) 
                :bins 40 :plot-size 600)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"3ff2bfe4-f933-4003-81b0-6c92b73f852b","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"3ff2bfe4-f933-4003-81b0-6c92b73f852b","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"3ff2bfe4-f933-4003-81b0-6c92b73f852b"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"3ff2bfe4-f933-4003-81b0-6c92b73f852b","values":[{"x":-0.0181610140999075,"y":0},{"x":-0.01752225387402688,"y":2000.0},{"x":-0.016883493648146262,"y":0.0},{"x":-0.016244733422265643,"y":0.0},{"x":-0.015605973196385023,"y":0.0},{"x":-0.014967212970504403,"y":0.0},{"x":-0.014328452744623783,"y":0.0},{"x":-0.013689692518743163,"y":0.0},{"x":-0.013050932292862543,"y":0.0},{"x":-0.012412172066981923,"y":0.0},{"x":-0.011773411841101302,"y":0.0},{"x":-0.011134651615220682,"y":0.0},{"x":-0.010495891389340062,"y":2000.0},{"x":-0.009857131163459442,"y":0.0},{"x":-0.009218370937578822,"y":0.0},{"x":-0.008579610711698202,"y":0.0},{"x":-0.007940850485817582,"y":0.0},{"x":-0.007302090259936962,"y":0.0},{"x":-0.006663330034056343,"y":0.0},{"x":-0.006024569808175724,"y":0.0},{"x":-0.005385809582295105,"y":0.0},{"x":-0.004747049356414485,"y":0.0},{"x":-0.004108289130533866,"y":1000.0},{"x":-0.0034695289046532468,"y":0.0},{"x":-0.0028307686787726275,"y":0.0},{"x":-0.0021920084528920082,"y":1000.0},{"x":-0.0015532482270113888,"y":0.0},{"x":-9.144880011307693E-4,"y":0.0},{"x":-2.757277752501498E-4,"y":1000.0},{"x":3.630324506304697E-4,"y":4000.0},{"x":0.0010017926765110892,"y":1000.0},{"x":0.0016405529023917086,"y":1000.0},{"x":0.0022793131282723283,"y":0.0},{"x":0.0029180733541529476,"y":1000.0},{"x":0.003556833580033567,"y":1000.0},{"x":0.004195593805914186,"y":4000.0},{"x":0.004834354031794805,"y":0.0},{"x":0.005473114257675425,"y":0.0},{"x":0.006111874483556044,"y":0.0},{"x":0.006750634709436663,"y":0.0},{"x":0.0073893949353172825,"y":1000.0},{"x":0.008028155161197903,"y":0}]}],"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"3ff2bfe4-f933-4003-81b0-6c92b73f852b\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"3ff2bfe4-f933-4003-81b0-6c92b73f852b\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"3ff2bfe4-f933-4003-81b0-6c92b73f852b\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"3ff2bfe4-f933-4003-81b0-6c92b73f852b\", :values ({:x -0.0181610140999075, :y 0} {:x -0.01752225387402688, :y 2000.0} {:x -0.016883493648146262, :y 0.0} {:x -0.016244733422265643, :y 0.0} {:x -0.015605973196385023, :y 0.0} {:x -0.014967212970504403, :y 0.0} {:x -0.014328452744623783, :y 0.0} {:x -0.013689692518743163, :y 0.0} {:x -0.013050932292862543, :y 0.0} {:x -0.012412172066981923, :y 0.0} {:x -0.011773411841101302, :y 0.0} {:x -0.011134651615220682, :y 0.0} {:x -0.010495891389340062, :y 2000.0} {:x -0.009857131163459442, :y 0.0} {:x -0.009218370937578822, :y 0.0} {:x -0.008579610711698202, :y 0.0} {:x -0.007940850485817582, :y 0.0} {:x -0.007302090259936962, :y 0.0} {:x -0.006663330034056343, :y 0.0} {:x -0.006024569808175724, :y 0.0} {:x -0.005385809582295105, :y 0.0} {:x -0.004747049356414485, :y 0.0} {:x -0.004108289130533866, :y 1000.0} {:x -0.0034695289046532468, :y 0.0} {:x -0.0028307686787726275, :y 0.0} {:x -0.0021920084528920082, :y 1000.0} {:x -0.0015532482270113888, :y 0.0} {:x -9.144880011307693E-4, :y 0.0} {:x -2.757277752501498E-4, :y 1000.0} {:x 3.630324506304697E-4, :y 4000.0} {:x 0.0010017926765110892, :y 1000.0} {:x 0.0016405529023917086, :y 1000.0} {:x 0.0022793131282723283, :y 0.0} {:x 0.0029180733541529476, :y 1000.0} {:x 0.003556833580033567, :y 1000.0} {:x 0.004195593805914186, :y 4000.0} {:x 0.004834354031794805, :y 0.0} {:x 0.005473114257675425, :y 0.0} {:x 0.006111874483556044, :y 0.0} {:x 0.006750634709436663, :y 0.0} {:x 0.0073893949353172825, :y 1000.0} {:x 0.008028155161197903, :y 0})}], :width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; @@
(def bias-history-tensor 
  (a/get-hdf5-tensor store (:bias_history.h5 blobs) "/bias_history"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/bias-history-tensor</span>","value":"#'moonlit-curve/bias-history-tensor"}
;; <=

;; @@
(plot/histogram (apply concat (take-last 1000 bias-history-tensor)) 
                :bins 30 :plot-size 600)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"c20ae07b-3609-4838-938d-e9e67b8a895e","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"c20ae07b-3609-4838-938d-e9e67b8a895e","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"c20ae07b-3609-4838-938d-e9e67b8a895e"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"c20ae07b-3609-4838-938d-e9e67b8a895e","values":[{"x":-0.007045275833854129,"y":0},{"x":-0.006785453730562431,"y":1000.0},{"x":-0.006525631627270733,"y":0.0},{"x":-0.006265809523979035,"y":0.0},{"x":-0.006005987420687337,"y":0.0},{"x":-0.005746165317395639,"y":0.0},{"x":-0.0054863432141039415,"y":0.0},{"x":-0.0052265211108122435,"y":0.0},{"x":-0.004966699007520546,"y":1000.0},{"x":-0.004706876904228848,"y":1000.0},{"x":-0.00444705480093715,"y":0.0},{"x":-0.004187232697645452,"y":0.0},{"x":-0.003927410594353754,"y":0.0},{"x":-0.0036675884910620563,"y":0.0},{"x":-0.003407766387770359,"y":0.0},{"x":-0.0031479442844786613,"y":0.0},{"x":-0.0028881221811869638,"y":0.0},{"x":-0.0026283000778952663,"y":0.0},{"x":-0.0023684779746035688,"y":0.0},{"x":-0.0021086558713118713,"y":0.0},{"x":-0.0018488337680201735,"y":0.0},{"x":-0.0015890116647284758,"y":1000.0},{"x":-0.001329189561436778,"y":0.0},{"x":-0.0010693674581450804,"y":1000.0},{"x":-8.095453548533826E-4,"y":0.0},{"x":-5.497232515616849E-4,"y":0.0},{"x":-2.8990114826998723E-4,"y":1000.0},{"x":-3.007904497828956E-5,"y":0.0},{"x":2.297430583134081E-4,"y":0.0},{"x":4.895651616051058E-4,"y":0.0},{"x":7.493872648968035E-4,"y":1000.0},{"x":0.0010092093681885012,"y":0}]}],"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"c20ae07b-3609-4838-938d-e9e67b8a895e\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"c20ae07b-3609-4838-938d-e9e67b8a895e\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"c20ae07b-3609-4838-938d-e9e67b8a895e\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"c20ae07b-3609-4838-938d-e9e67b8a895e\", :values ({:x -0.007045275833854129, :y 0} {:x -0.006785453730562431, :y 1000.0} {:x -0.006525631627270733, :y 0.0} {:x -0.006265809523979035, :y 0.0} {:x -0.006005987420687337, :y 0.0} {:x -0.005746165317395639, :y 0.0} {:x -0.0054863432141039415, :y 0.0} {:x -0.0052265211108122435, :y 0.0} {:x -0.004966699007520546, :y 1000.0} {:x -0.004706876904228848, :y 1000.0} {:x -0.00444705480093715, :y 0.0} {:x -0.004187232697645452, :y 0.0} {:x -0.003927410594353754, :y 0.0} {:x -0.0036675884910620563, :y 0.0} {:x -0.003407766387770359, :y 0.0} {:x -0.0031479442844786613, :y 0.0} {:x -0.0028881221811869638, :y 0.0} {:x -0.0026283000778952663, :y 0.0} {:x -0.0023684779746035688, :y 0.0} {:x -0.0021086558713118713, :y 0.0} {:x -0.0018488337680201735, :y 0.0} {:x -0.0015890116647284758, :y 1000.0} {:x -0.001329189561436778, :y 0.0} {:x -0.0010693674581450804, :y 1000.0} {:x -8.095453548533826E-4, :y 0.0} {:x -5.497232515616849E-4, :y 0.0} {:x -2.8990114826998723E-4, :y 1000.0} {:x -3.007904497828956E-5, :y 0.0} {:x 2.297430583134081E-4, :y 0.0} {:x 4.895651616051058E-4, :y 0.0} {:x 7.493872648968035E-4, :y 1000.0} {:x 0.0010092093681885012, :y 0})}], :width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; @@
(def weights-history-avgs-tensor 
  (a/get-hdf5-tensor store (:weight_avgs.h5 blobs) "/weight_avgs"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/weights-history-avgs-tensor</span>","value":"#'moonlit-curve/weights-history-avgs-tensor"}
;; <=

;; @@
#_(plot/compose
  (plot/list-plot (map first weights-history-avgs-tensor) 
                  :joined true :plot-range [:all [-0.004 0.020]]  :plot-size 600)
  (plot/list-plot (map second weights-history-avgs-tensor) 
                  :joined true :color "green"))
;; @@

;; **
;;; ![history](project-files/repo/store/%23uuid%20"25711115-4a98-5d3a-b6f2-f14e13f7e00b")
;; **

;; **
;;; ## Ideas
;;; - deactivate weights/bias, resample to find out what is causing behaviour
;;; - symmetry: try current-based neurons
;; **

;; **
;;; ## Sample with bias only
;; **

;; @@
(def bias-blobs {:dist_joint_sim.h5 #uuid "281c42ab-bf3d-5177-a77d-942e21b48bfd", 
                 :spike_trains.h5 #uuid "16e50562-4dc2-548c-aaf1-a41f77c09b5c"})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/bias-blobs</span>","value":"#'moonlit-curve/bias-blobs"}
;; <=

;; @@
(def bias-only-dist-joint-tensor 
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 bias-blobs) "/dist_joint_sim"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/bias-only-dist-joint-tensor</span>","value":"#'moonlit-curve/bias-only-dist-joint-tensor"}
;; <=

;; @@
(plot/compose 
  (plot/bar-chart states (map (partial get-in joint-tensor) states) 
                  :color "red" :opacity 0.2 :plot-size 600) 
  (plot/bar-chart states (map (partial get-in bias-only-dist-joint-tensor) states) 
                  :color "blue" :opacity 0.2 :plot-size 600))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"52d800c0-4235-4f2d-8a0f-2122493a7d30","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"52d800c0-4235-4f2d-8a0f-2122493a7d30","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"52d800c0-4235-4f2d-8a0f-2122493a7d30","values":[{"x":[0,0],"y":0.218757199999952},{"x":[0,1],"y":0.17091220000004792},{"x":[1,0],"y":0.340542800000048},{"x":[1,1],"y":0.26978779999995206}]},{"name":"99ea7450-6fd1-4200-8ab0-4702e238d734","values":[{"x":[0,0],"y":0.24104209999999573},{"x":[0,1],"y":0.24794790000000427},{"x":[1,0],"y":0.2519579000000043},{"x":[1,1],"y":0.25905209999999573}]}],"marks":[{"type":"rect","from":{"data":"52d800c0-4235-4f2d-8a0f-2122493a7d30"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.2}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"99ea7450-6fd1-4200-8ab0-4702e238d734"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"blue"},"opacity":{"value":0.2}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"52d800c0-4235-4f2d-8a0f-2122493a7d30\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"52d800c0-4235-4f2d-8a0f-2122493a7d30\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"52d800c0-4235-4f2d-8a0f-2122493a7d30\", :values ({:x (0 0), :y 0.218757199999952} {:x (0 1), :y 0.17091220000004792} {:x (1 0), :y 0.340542800000048} {:x (1 1), :y 0.26978779999995206})} {:name \"99ea7450-6fd1-4200-8ab0-4702e238d734\", :values ({:x (0 0), :y 0.24104209999999573} {:x (0 1), :y 0.24794790000000427} {:x (1 0), :y 0.2519579000000043} {:x (1 1), :y 0.25905209999999573})}), :marks ({:type \"rect\", :from {:data \"52d800c0-4235-4f2d-8a0f-2122493a7d30\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.2}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"99ea7450-6fd1-4200-8ab0-4702e238d734\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"blue\"}, :opacity {:value 0.2}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; **
;;; ## Sample with weights only
;; **

;; @@
(def weights (partition 5 (last weights-history-tensor)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/weights</span>","value":"#'moonlit-curve/weights"}
;; <=

;; @@
(def hidden-weights (mat/join-along 0 (mat/zero-matrix 5 5) (take 2 weights)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/hidden-weights</span>","value":"#'moonlit-curve/hidden-weights"}
;; <=

;; @@
(def visi-weights (mat/transpose (mat/join-along 1 (take 2 (drop 2 weights)) (mat/zero-matrix 2 2))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/visi-weights</span>","value":"#'moonlit-curve/visi-weights"}
;; <=

;; @@
(mat/join-along 1 hidden-weights visi-weights)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>2.056875957797295E-4</span>","value":"2.056875957797295E-4"},{"type":"html","content":"<span class='clj-double'>0.0013669105912842605</span>","value":"0.0013669105912842605"}],"value":"[0.0 0.0 0.0 0.0 0.0 2.056875957797295E-4 0.0013669105912842605]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>-0.0027931538164196745</span>","value":"-0.0027931538164196745"},{"type":"html","content":"<span class='clj-double'>-0.004409988990267686</span>","value":"-0.004409988990267686"}],"value":"[0.0 0.0 0.0 0.0 0.0 -0.0027931538164196745 -0.004409988990267686]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0035847379363490857</span>","value":"0.0035847379363490857"},{"type":"html","content":"<span class='clj-double'>0.003572532798821243</span>","value":"0.003572532798821243"}],"value":"[0.0 0.0 0.0 0.0 0.0 0.0035847379363490857 0.003572532798821243]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.00738939493531728</span>","value":"0.00738939493531728"},{"type":"html","content":"<span class='clj-double'>0.003874658825819731</span>","value":"0.003874658825819731"}],"value":"[0.0 0.0 0.0 0.0 0.0 0.00738939493531728 0.003874658825819731]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0032644557584475043</span>","value":"0.0032644557584475043"},{"type":"html","content":"<span class='clj-double'>0.0035927028983775873</span>","value":"0.0035927028983775873"}],"value":"[0.0 0.0 0.0 0.0 0.0 0.0032644557584475043 0.0035927028983775873]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>-0.01754933012833558</span>","value":"-0.01754933012833558"},{"type":"html","content":"<span class='clj-double'>-0.0181610140999075</span>","value":"-0.0181610140999075"},{"type":"html","content":"<span class='clj-double'>-0.010553584555954195</span>","value":"-0.010553584555954195"},{"type":"html","content":"<span class='clj-double'>-0.01066898478193935</span>","value":"-0.01066898478193935"},{"type":"html","content":"<span class='clj-double'>-1.1550209797454866E-4</span>","value":"-1.1550209797454866E-4"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[-0.01754933012833558 -0.0181610140999075 -0.010553584555954195 -0.01066898478193935 -1.1550209797454866E-4 0.0 0.0]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>5.972623899694243E-6</span>","value":"5.972623899694243E-6"},{"type":"html","content":"<span class='clj-double'>0.0025956373673705715</span>","value":"0.0025956373673705715"},{"type":"html","content":"<span class='clj-double'>1.92010726884624E-4</span>","value":"1.92010726884624E-4"},{"type":"html","content":"<span class='clj-double'>-4.2930275828171493E-4</span>","value":"-4.2930275828171493E-4"},{"type":"html","content":"<span class='clj-double'>5.211212727172467E-4</span>","value":"5.211212727172467E-4"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[5.972623899694243E-6 0.0025956373673705715 1.92010726884624E-4 -4.2930275828171493E-4 5.211212727172467E-4 0.0 0.0]"}],"value":"[[0.0 0.0 0.0 0.0 0.0 2.056875957797295E-4 0.0013669105912842605] [0.0 0.0 0.0 0.0 0.0 -0.0027931538164196745 -0.004409988990267686] [0.0 0.0 0.0 0.0 0.0 0.0035847379363490857 0.003572532798821243] [0.0 0.0 0.0 0.0 0.0 0.00738939493531728 0.003874658825819731] [0.0 0.0 0.0 0.0 0.0 0.0032644557584475043 0.0035927028983775873] [-0.01754933012833558 -0.0181610140999075 -0.010553584555954195 -0.01066898478193935 -1.1550209797454866E-4 0.0 0.0] [5.972623899694243E-6 0.0025956373673705715 1.92010726884624E-4 -4.2930275828171493E-4 5.211212727172467E-4 0.0 0.0]]"}
;; <=

;; @@
(mat/transpose [[0.0 0.0 0.0 0.0 0.0 2.056875957797295E-4 0.0013669105912842605] 
 [0.0 0.0 0.0 0.0 0.0 -0.0027931538164196745 -0.004409988990267686] 
 [0.0 0.0 0.0 0.0 0.0 0.0035847379363490857 0.003572532798821243] 
 [0.0 0.0 0.0 0.0 0.0 0.00738939493531728 0.003874658825819731] 
 [0.0 0.0 0.0 0.0 0.0 0.0032644557584475043 0.0035927028983775873] 
 [-0.01754933012833558 -0.0181610140999075 -0.010553584555954195 -0.01066898478193935 -1.1550209797454866E-4 0.0 0.0] 
 [5.972623899694243E-6 0.0025956373673705715 1.92010726884624E-4 -4.2930275828171493E-4 5.211212727172467E-4 0.0 0.0]])
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>-0.01754933012833558</span>","value":"-0.01754933012833558"},{"type":"html","content":"<span class='clj-double'>5.972623899694243E-6</span>","value":"5.972623899694243E-6"}],"value":"[0.0 0.0 0.0 0.0 0.0 -0.01754933012833558 5.972623899694243E-6]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>-0.0181610140999075</span>","value":"-0.0181610140999075"},{"type":"html","content":"<span class='clj-double'>0.0025956373673705715</span>","value":"0.0025956373673705715"}],"value":"[0.0 0.0 0.0 0.0 0.0 -0.0181610140999075 0.0025956373673705715]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>-0.010553584555954195</span>","value":"-0.010553584555954195"},{"type":"html","content":"<span class='clj-double'>1.92010726884624E-4</span>","value":"1.92010726884624E-4"}],"value":"[0.0 0.0 0.0 0.0 0.0 -0.010553584555954195 1.92010726884624E-4]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>-0.01066898478193935</span>","value":"-0.01066898478193935"},{"type":"html","content":"<span class='clj-double'>-4.2930275828171493E-4</span>","value":"-4.2930275828171493E-4"}],"value":"[0.0 0.0 0.0 0.0 0.0 -0.01066898478193935 -4.2930275828171493E-4]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>-1.1550209797454866E-4</span>","value":"-1.1550209797454866E-4"},{"type":"html","content":"<span class='clj-double'>5.211212727172467E-4</span>","value":"5.211212727172467E-4"}],"value":"[0.0 0.0 0.0 0.0 0.0 -1.1550209797454866E-4 5.211212727172467E-4]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>2.056875957797295E-4</span>","value":"2.056875957797295E-4"},{"type":"html","content":"<span class='clj-double'>-0.0027931538164196745</span>","value":"-0.0027931538164196745"},{"type":"html","content":"<span class='clj-double'>0.0035847379363490857</span>","value":"0.0035847379363490857"},{"type":"html","content":"<span class='clj-double'>0.00738939493531728</span>","value":"0.00738939493531728"},{"type":"html","content":"<span class='clj-double'>0.0032644557584475043</span>","value":"0.0032644557584475043"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[2.056875957797295E-4 -0.0027931538164196745 0.0035847379363490857 0.00738939493531728 0.0032644557584475043 0.0 0.0]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>0.0013669105912842605</span>","value":"0.0013669105912842605"},{"type":"html","content":"<span class='clj-double'>-0.004409988990267686</span>","value":"-0.004409988990267686"},{"type":"html","content":"<span class='clj-double'>0.003572532798821243</span>","value":"0.003572532798821243"},{"type":"html","content":"<span class='clj-double'>0.003874658825819731</span>","value":"0.003874658825819731"},{"type":"html","content":"<span class='clj-double'>0.0035927028983775873</span>","value":"0.0035927028983775873"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[0.0013669105912842605 -0.004409988990267686 0.003572532798821243 0.003874658825819731 0.0035927028983775873 0.0 0.0]"}],"value":"[[0.0 0.0 0.0 0.0 0.0 -0.01754933012833558 5.972623899694243E-6] [0.0 0.0 0.0 0.0 0.0 -0.0181610140999075 0.0025956373673705715] [0.0 0.0 0.0 0.0 0.0 -0.010553584555954195 1.92010726884624E-4] [0.0 0.0 0.0 0.0 0.0 -0.01066898478193935 -4.2930275828171493E-4] [0.0 0.0 0.0 0.0 0.0 -1.1550209797454866E-4 5.211212727172467E-4] [2.056875957797295E-4 -0.0027931538164196745 0.0035847379363490857 0.00738939493531728 0.0032644557584475043 0.0 0.0] [0.0013669105912842605 -0.004409988990267686 0.003572532798821243 0.003874658825819731 0.0035927028983775873 0.0 0.0]]"}
;; <=

;; @@
(def weight-blobs {:dist_joint_sim.h5 #uuid "2b0ba197-4492-5484-be5e-55fd6a4ff649",
                   :spike_trains.h5 #uuid "27ec7222-3a09-51e9-8e66-8a8b0a0f83e4"})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/weight-blobs</span>","value":"#'moonlit-curve/weight-blobs"}
;; <=

;; @@
(def weights-only-dist-joint-tensor 
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 weight-blobs) "/dist_joint_sim"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/weights-only-dist-joint-tensor</span>","value":"#'moonlit-curve/weights-only-dist-joint-tensor"}
;; <=

;; @@
#_(plot/compose 
  (plot/bar-chart states (map (partial get-in joint-tensor) states) 
                  :color "red" :opacity 0.2 :plot-size 600) 
  (plot/bar-chart states (map (partial get-in weights-only-dist-joint-tensor) states) 
                  :color "blue" :opacity 0.2 :plot-size 600))
;; @@

;; @@
(def trans-weight-blobs {:dist_joint_sim.h5 #uuid "3e9e0f32-0cfd-5b19-98e9-373ad99c1fc7", 
                         :spike_trains.h5 #uuid "00e1df01-52fc-5125-8885-8217ec993de8"})
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/trans-weight-blobs</span>","value":"#'moonlit-curve/trans-weight-blobs"}
;; <=

;; @@
(def trans-weights-only-dist-joint-tensor 
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 trans-weight-blobs) "/dist_joint_sim"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/trans-weights-only-dist-joint-tensor</span>","value":"#'moonlit-curve/trans-weights-only-dist-joint-tensor"}
;; <=

;; @@
(plot/compose 
  (plot/bar-chart states (map (partial get-in trans-weights-only-dist-joint-tensor) states) 
                  :color "blue" :opacity 0.2 :plot-size 600)
  (plot/bar-chart states (map (partial get-in joint-tensor) states) 
                  :color "red" :opacity 0.2 :plot-size 600))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"ff459c86-6975-4782-a7bf-dbf826487872","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"ff459c86-6975-4782-a7bf-dbf826487872","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"ff459c86-6975-4782-a7bf-dbf826487872","values":[{"x":[0,0],"y":0.28008009999999933},{"x":[0,1],"y":0.09543990000000065},{"x":[1,0],"y":0.47184990000000065},{"x":[1,1],"y":0.15263009999999932}]},{"name":"545b35c5-7561-4d24-8f29-561698d1b843","values":[{"x":[0,0],"y":0.218757199999952},{"x":[0,1],"y":0.17091220000004792},{"x":[1,0],"y":0.340542800000048},{"x":[1,1],"y":0.26978779999995206}]}],"marks":[{"type":"rect","from":{"data":"ff459c86-6975-4782-a7bf-dbf826487872"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"blue"},"opacity":{"value":0.2}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"545b35c5-7561-4d24-8f29-561698d1b843"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.2}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"ff459c86-6975-4782-a7bf-dbf826487872\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"ff459c86-6975-4782-a7bf-dbf826487872\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"ff459c86-6975-4782-a7bf-dbf826487872\", :values ({:x (0 0), :y 0.28008009999999933} {:x (0 1), :y 0.09543990000000065} {:x (1 0), :y 0.47184990000000065} {:x (1 1), :y 0.15263009999999932})} {:name \"545b35c5-7561-4d24-8f29-561698d1b843\", :values ({:x (0 0), :y 0.218757199999952} {:x (0 1), :y 0.17091220000004792} {:x (1 0), :y 0.340542800000048} {:x (1 1), :y 0.26978779999995206})}), :marks ({:type \"rect\", :from {:data \"ff459c86-6975-4782-a7bf-dbf826487872\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"blue\"}, :opacity {:value 0.2}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"545b35c5-7561-4d24-8f29-561698d1b843\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.2}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; @@
(def spike-trains
  (mapv #(a/get-hdf5-tensor store (:spike_trains.h5 trans-weight-blobs) (str "/spike_trains/" %)) (range 7)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;moonlit-curve/spike-trains</span>","value":"#'moonlit-curve/spike-trains"}
;; <=

;; @@
(take 10 (first spike-trains))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>12.6</span>","value":"12.6"},{"type":"html","content":"<span class='clj-double'>22.7</span>","value":"22.7"},{"type":"html","content":"<span class='clj-double'>35.1</span>","value":"35.1"},{"type":"html","content":"<span class='clj-double'>45.2</span>","value":"45.2"},{"type":"html","content":"<span class='clj-double'>69.7</span>","value":"69.7"},{"type":"html","content":"<span class='clj-double'>80.2</span>","value":"80.2"},{"type":"html","content":"<span class='clj-double'>108.2</span>","value":"108.2"},{"type":"html","content":"<span class='clj-double'>118.3</span>","value":"118.3"},{"type":"html","content":"<span class='clj-double'>138.3</span>","value":"138.3"},{"type":"html","content":"<span class='clj-double'>148.4</span>","value":"148.4"}],"value":"(12.6 22.7 35.1 45.2 69.7 80.2 108.2 118.3 138.3 148.4)"}
;; <=

;; @@
#_(apply plot/compose (map #(plot/list-plot (map (fn [t] [t %]) (spike-trains %)) :plot-range [[0 2e3] [0 7]] :plot-size 1200 :symbol-size 20) (range 7)))
;; @@

;; @@

;; @@
