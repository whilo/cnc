;; gorilla-repl.fileformat = 1

;; **
;;; # Current based small RBMs
;; **

;; @@
(ns mirthful-plateau
  (:require [gorilla-plot.core :as plot]
            [gg4clj.core :as gg4clj]
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
    (<!? (-get-in store [#uuid "25419627-cce7-598c-92bd-5e925193d030"])))
  (def blobs (:output experiment)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;mirthful-plateau/blobs</span>","value":"#'mirthful-plateau/blobs"}
;; <=

;; @@
(future (sh "okular" (str (<!? (-bget store (:training_overview.pdf blobs) :file)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>#&lt;core$future_call$reify__6320@43f69f06: :pending&gt;</span>","value":"#<core$future_call$reify__6320@43f69f06: :pending>"}
;; <=

;; @@
(future (sh "kate" (str (<!? (-bget store (:stdp.log blobs) :file)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>#&lt;core$future_call$reify__6320@7e5c6b8f: :pending&gt;</span>","value":"#<core$future_call$reify__6320@7e5c6b8f: :pending>"}
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
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"37a7e6e3-c40b-485f-a2c5-1bd1ea72bf11","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"37a7e6e3-c40b-485f-a2c5-1bd1ea72bf11","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"37a7e6e3-c40b-485f-a2c5-1bd1ea72bf11","values":[{"x":[0,0],"y":0.1956072000000216},{"x":[0,1],"y":0.24338709999997868},{"x":[1,0],"y":0.3670517999999784},{"x":[1,1],"y":0.19395390000002133}]},{"name":"e3b9a3f8-683e-4a7d-811a-2bf2001f36f7","values":[{"x":[0,0],"y":0.147185280919075},{"x":[0,1],"y":0.26907309889793396},{"x":[1,0],"y":0.4376562237739563},{"x":[1,1],"y":0.14608539640903473}]}],"marks":[{"type":"rect","from":{"data":"37a7e6e3-c40b-485f-a2c5-1bd1ea72bf11"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"e3b9a3f8-683e-4a7d-811a-2bf2001f36f7"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"37a7e6e3-c40b-485f-a2c5-1bd1ea72bf11\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"37a7e6e3-c40b-485f-a2c5-1bd1ea72bf11\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"37a7e6e3-c40b-485f-a2c5-1bd1ea72bf11\", :values ({:x (0 0), :y 0.1956072000000216} {:x (0 1), :y 0.24338709999997868} {:x (1 0), :y 0.3670517999999784} {:x (1 1), :y 0.19395390000002133})} {:name \"e3b9a3f8-683e-4a7d-811a-2bf2001f36f7\", :values ({:x (0 0), :y 0.14718528} {:x (0 1), :y 0.2690731} {:x (1 0), :y 0.43765622} {:x (1 1), :y 0.1460854})}), :marks ({:type \"rect\", :from {:data \"37a7e6e3-c40b-485f-a2c5-1bd1ea72bf11\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"e3b9a3f8-683e-4a7d-811a-2bf2001f36f7\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; @@
(gg4clj/view (gg4clj/r+ (gg4clj/data-frame {:samples (map (a/sample-freqs samples) states)
                                            :simulated (map (partial get-in joint-tensor) states)})
                        [:geom_bar]))
;; @@
;; ->
;;; Lade nötiges Paket: methods
;;; Fehler in as.vector(x, mode) : 
;;;   cannot coerce type &#x27;environment&#x27; to vector of type &#x27;any&#x27;
;;; Calls: Ops.data.frame -&gt; split -&gt; rep_len -&gt; as.vector
;;; Ausführung angehalten
;;; 
;;; 
;; <-
;; =>
;;; {"type":"html","content":"","value":"#gg4clj.core.GGView{:plot-command [:+ [:data.frame {:samples [:c 0.14718528 0.2690731 0.43765622 0.1460854], :simulated [:c 0.1956072000000216 0.24338709999997868 0.3670517999999784 0.19395390000002133]}] [:geom_bar]], :options {}}"}
;; <=

;; @@
(def longer-experiment (<!? (-get-in store [#uuid "1b1dada1-3e79-5a5b-b212-11a5b6aa55eb"])))
(def longer-blobs (:output longer-experiment))
(require '[clojure.data :refer [diff]])
#_(clojure.data/diff longer-experiment experiment)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(future (sh "okular" (str (<!? (-bget store (:training_overview.pdf longer-blobs) :file)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>#&lt;core$future_call$reify__6320@194fa0f2: :pending&gt;</span>","value":"#<core$future_call$reify__6320@194fa0f2: :pending>"}
;; <=

;; @@
 (def joint-tensor-longer
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 longer-blobs) "/dist_joint_sim"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;mirthful-plateau/joint-tensor-longer</span>","value":"#'mirthful-plateau/joint-tensor-longer"}
;; <=

;; @@
(plot/compose 
  (plot/bar-chart states (map (partial get-in joint-tensor-longer) states)
                  :color "red" :opacity 0.5 :plot-size 600) 
  (plot/bar-chart states (map (a/sample-freqs samples) states) 
                  :opacity 0.5))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"1b9a2df2-448b-4310-8def-00586ccee0e2","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"1b9a2df2-448b-4310-8def-00586ccee0e2","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"1b9a2df2-448b-4310-8def-00586ccee0e2","values":[{"x":[0,0],"y":0.00979329999992624},{"x":[0,1],"y":7.877000000737608E-4},{"x":[1,0],"y":0.9450767000000737},{"x":[1,1],"y":0.04434229999992624}]},{"name":"acb7d657-c214-4daa-a220-b8a4df364a4f","values":[{"x":[0,0],"y":0.147185280919075},{"x":[0,1],"y":0.26907309889793396},{"x":[1,0],"y":0.4376562237739563},{"x":[1,1],"y":0.14608539640903473}]}],"marks":[{"type":"rect","from":{"data":"1b9a2df2-448b-4310-8def-00586ccee0e2"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"acb7d657-c214-4daa-a220-b8a4df364a4f"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"1b9a2df2-448b-4310-8def-00586ccee0e2\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"1b9a2df2-448b-4310-8def-00586ccee0e2\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"1b9a2df2-448b-4310-8def-00586ccee0e2\", :values ({:x (0 0), :y 0.00979329999992624} {:x (0 1), :y 7.877000000737608E-4} {:x (1 0), :y 0.9450767000000737} {:x (1 1), :y 0.04434229999992624})} {:name \"acb7d657-c214-4daa-a220-b8a4df364a4f\", :values ({:x (0 0), :y 0.14718528} {:x (0 1), :y 0.2690731} {:x (1 0), :y 0.43765622} {:x (1 1), :y 0.1460854})}), :marks ({:type \"rect\", :from {:data \"1b9a2df2-448b-4310-8def-00586ccee0e2\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"acb7d657-c214-4daa-a220-b8a4df364a4f\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; @@
(def bigger-rbm-experiment (<!? (-get-in store [#uuid "0f380350-713a-5d99-a033-079b9d414159"])))
(def bigger-rbm-blobs (:output bigger-rbm-experiment))
(def bigger-samples (<!? (-get-in store [#uuid "3197da4c-3806-544f-a62b-2f48383691d4" :output])))
(-> bigger-rbm-experiment :exp-params :training-params)

;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:h_count</span>","value":":h_count"},{"type":"html","content":"<span class='clj-long'>6</span>","value":"6"}],"value":"[:h_count 6]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:dt</span>","value":":dt"},{"type":"html","content":"<span class='clj-double'>0.1</span>","value":"0.1"}],"value":"[:dt 0.1]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:epochs</span>","value":":epochs"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[:epochs 2]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:burn_in_time</span>","value":":burn_in_time"},{"type":"html","content":"<span class='clj-double'>0.0</span>","value":"0.0"}],"value":"[:burn_in_time 0.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:learning_rate</span>","value":":learning_rate"},{"type":"html","content":"<span class='clj-double'>2.0E-5</span>","value":"2.0E-5"}],"value":"[:learning_rate 2.0E-5]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:sampling_time</span>","value":":sampling_time"},{"type":"html","content":"<span class='clj-double'>1000000.0</span>","value":"1000000.0"}],"value":"[:sampling_time 1000000.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:phase_duration</span>","value":":phase_duration"},{"type":"html","content":"<span class='clj-double'>100.0</span>","value":"100.0"}],"value":"[:phase_duration 100.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:weight_recording_interval</span>","value":":weight_recording_interval"},{"type":"html","content":"<span class='clj-double'>100.0</span>","value":"100.0"}],"value":"[:weight_recording_interval 100.0]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:sim_setup_kwargs</span>","value":":sim_setup_kwargs"},{"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:grng_seed</span>","value":":grng_seed"},{"type":"html","content":"<span class='clj-long'>43</span>","value":"43"}],"value":"[:grng_seed 43]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:rng_seeds_seed</span>","value":":rng_seeds_seed"},{"type":"html","content":"<span class='clj-long'>43</span>","value":"43"}],"value":"[:rng_seeds_seed 43]"}],"value":"{:grng_seed 43, :rng_seeds_seed 43}"}],"value":"[:sim_setup_kwargs {:grng_seed 43, :rng_seeds_seed 43}]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:stdp_burnin</span>","value":":stdp_burnin"},{"type":"html","content":"<span class='clj-double'>10.0</span>","value":"10.0"}],"value":"[:stdp_burnin 10.0]"}],"value":"{:h_count 6, :dt 0.1, :epochs 2, :burn_in_time 0.0, :learning_rate 2.0E-5, :sampling_time 1000000.0, :phase_duration 100.0, :weight_recording_interval 100.0, :sim_setup_kwargs {:grng_seed 43, :rng_seeds_seed 43}, :stdp_burnin 10.0}"}
;; <=

;; @@
(future (sh "okular" (str (<!? (-bget store (:training_overview.pdf bigger-rbm-blobs) :file)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>#&lt;core$future_call$reify__6320@7b491de9: :pending&gt;</span>","value":"#<core$future_call$reify__6320@7b491de9: :pending>"}
;; <=

;; @@
 (def joint-tensor-bigger
  (a/get-hdf5-tensor store (:dist_joint_sim.h5 bigger-rbm-blobs) "/dist_joint_sim"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;mirthful-plateau/joint-tensor-bigger</span>","value":"#'mirthful-plateau/joint-tensor-bigger"}
;; <=

;; @@
(let [states (cartesian-product [0 1] [0 1] [0 1] [0 1])]
(plot/compose 
  (plot/bar-chart states (map (partial get-in joint-tensor-bigger) states)
                  :color "red" :opacity 0.5 :plot-size 600) 
  (plot/bar-chart states (map (a/sample-freqs bigger-samples) states) 
                  :opacity 0.5)))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"ec0213f6-31ad-418f-9007-2dae3eb121ff","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"ec0213f6-31ad-418f-9007-2dae3eb121ff","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"ec0213f6-31ad-418f-9007-2dae3eb121ff","values":[{"x":[0,0,0,0],"y":7.620999999945052E-4},{"x":[0,0,0,1],"y":0.0033533999999617228},{"x":[0,0,1,0],"y":0.010135099999986588},{"x":[0,0,1,1],"y":0.04103680000003334},{"x":[0,1,0,0],"y":3.281000000122003E-4},{"x":[0,1,0,1],"y":0.0011245999999847263},{"x":[0,1,1,0],"y":0.002614799999971874},{"x":[0,1,1,1],"y":0.0097762000000556},{"x":[1,0,0,0],"y":0.009207300000017044},{"x":[1,0,0,1],"y":0.03738470000004815},{"x":[1,0,1,0],"y":0.11562939999997476},{"x":[1,0,1,1],"y":0.4526811999999839},{"x":[1,1,0,0],"y":0.006496000000033062},{"x":[1,1,0,1],"y":0.026175199999948964},{"x":[1,1,1,0],"y":0.05828010000001034},{"x":[1,1,1,1],"y":0.22501499999998323}]},{"name":"702c0a63-f818-46b9-b3ec-38d02a55c04e","values":[{"x":[0,0,0,0],"y":0.006499350070953369},{"x":[0,0,0,1],"y":0.01859813928604126},{"x":[0,0,1,0],"y":0.1337866187095642},{"x":[0,0,1,1],"y":0.43745625019073486},{"x":[0,1,0,0],"y":0.001799819990992546},{"x":[0,1,0,1],"y":0.006099389865994453},{"x":[0,1,1,0],"y":0.017398260533809662},{"x":[0,1,1,1],"y":0.04579542204737663},{"x":[1,0,0,0],"y":0.003099689958617091},{"x":[1,0,0,1],"y":0.01029897015541792},{"x":[1,0,1,0],"y":0.058394160121679306},{"x":[1,0,1,1],"y":0.2260773926973343},{"x":[1,1,0,0],"y":0.001099889981560409},{"x":[1,1,0,1],"y":0.003399660112336278},{"x":[1,1,1,0],"y":0.006599340122193098},{"x":[1,1,1,1],"y":0.02359764091670513}]}],"marks":[{"type":"rect","from":{"data":"ec0213f6-31ad-418f-9007-2dae3eb121ff"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"702c0a63-f818-46b9-b3ec-38d02a55c04e"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"ec0213f6-31ad-418f-9007-2dae3eb121ff\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"ec0213f6-31ad-418f-9007-2dae3eb121ff\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"ec0213f6-31ad-418f-9007-2dae3eb121ff\", :values ({:x (0 0 0 0), :y 7.620999999945052E-4} {:x (0 0 0 1), :y 0.0033533999999617228} {:x (0 0 1 0), :y 0.010135099999986588} {:x (0 0 1 1), :y 0.04103680000003334} {:x (0 1 0 0), :y 3.281000000122003E-4} {:x (0 1 0 1), :y 0.0011245999999847263} {:x (0 1 1 0), :y 0.002614799999971874} {:x (0 1 1 1), :y 0.0097762000000556} {:x (1 0 0 0), :y 0.009207300000017044} {:x (1 0 0 1), :y 0.03738470000004815} {:x (1 0 1 0), :y 0.11562939999997476} {:x (1 0 1 1), :y 0.4526811999999839} {:x (1 1 0 0), :y 0.006496000000033062} {:x (1 1 0 1), :y 0.026175199999948964} {:x (1 1 1 0), :y 0.05828010000001034} {:x (1 1 1 1), :y 0.22501499999998323})} {:name \"702c0a63-f818-46b9-b3ec-38d02a55c04e\", :values ({:x (0 0 0 0), :y 0.00649935} {:x (0 0 0 1), :y 0.01859814} {:x (0 0 1 0), :y 0.13378662} {:x (0 0 1 1), :y 0.43745625} {:x (0 1 0 0), :y 0.00179982} {:x (0 1 0 1), :y 0.00609939} {:x (0 1 1 0), :y 0.01739826} {:x (0 1 1 1), :y 0.045795422} {:x (1 0 0 0), :y 0.00309969} {:x (1 0 0 1), :y 0.01029897} {:x (1 0 1 0), :y 0.05839416} {:x (1 0 1 1), :y 0.2260774} {:x (1 1 0 0), :y 0.00109989} {:x (1 1 0 1), :y 0.00339966} {:x (1 1 1 0), :y 0.00659934} {:x (1 1 1 1), :y 0.02359764})}), :marks ({:type \"rect\", :from {:data \"ec0213f6-31ad-418f-9007-2dae3eb121ff\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"702c0a63-f818-46b9-b3ec-38d02a55c04e\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; **
;;; # To discuss
;;; ## learning-rate vs. duration
;;; - plot DKL between data
;;; - compare to traditional CD
;; **

;; @@

;; @@
