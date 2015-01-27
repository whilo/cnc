;; gorilla-repl.fileformat = 1

;; **
;;; # First experiment with small RBM (3 visible, 9 hidden)
;; **

;; @@
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

;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;affectionate-brook/blobs</span>","value":"#'affectionate-brook/blobs"}
;; <=

;; **
;;; ## Parameters
;; **

;; @@
(pprint (get-in experiment [:exp-params :training-params]))
;; @@
;; ->
;;; {:h_count 9,
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
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;affectionate-brook/joint-tensor</span>","value":"#'affectionate-brook/joint-tensor"}
;; <=

;; @@
(plot/compose 
  (plot/bar-chart states (map (partial get-in joint-tensor) states) 
                  :color "red" :opacity 0.5 :plot-size 600) 
  (plot/bar-chart states (map (a/sample-freqs samples) states) :opacity 0.5))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"6c7f2180-917e-4078-b5c8-0918fea01a4f","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"6c7f2180-917e-4078-b5c8-0918fea01a4f","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"6c7f2180-917e-4078-b5c8-0918fea01a4f","values":[{"x":[0,0,0],"y":0.09432770000002604},{"x":[0,0,1],"y":0.13279769999995408},{"x":[0,1,0],"y":0.07511409999999451},{"x":[0,1,1],"y":0.11825480000002565},{"x":[1,0,0],"y":0.11948009999999823},{"x":[1,0,1],"y":0.18178150000002166},{"x":[1,1,0],"y":0.10767649999998113},{"x":[1,1,1],"y":0.1705675999999987}]},{"name":"08783f0b-888e-44b7-ba53-353cd8955c2b","values":[{"x":[0,0,0],"y":0.11178882420063019},{"x":[0,0,1],"y":0.14848515391349792},{"x":[0,1,0],"y":0.08699130266904831},{"x":[0,1,1],"y":0.10408958792686462},{"x":[1,0,0],"y":0.13398660719394684},{"x":[1,0,1],"y":0.178782120347023},{"x":[1,1,0],"y":0.09899009764194489},{"x":[1,1,1],"y":0.13688631355762482}]}],"marks":[{"type":"rect","from":{"data":"6c7f2180-917e-4078-b5c8-0918fea01a4f"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"08783f0b-888e-44b7-ba53-353cd8955c2b"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"6c7f2180-917e-4078-b5c8-0918fea01a4f\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"6c7f2180-917e-4078-b5c8-0918fea01a4f\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"6c7f2180-917e-4078-b5c8-0918fea01a4f\", :values ({:x (0 0 0), :y 0.09432770000002604} {:x (0 0 1), :y 0.13279769999995408} {:x (0 1 0), :y 0.07511409999999451} {:x (0 1 1), :y 0.11825480000002565} {:x (1 0 0), :y 0.11948009999999823} {:x (1 0 1), :y 0.18178150000002166} {:x (1 1 0), :y 0.10767649999998113} {:x (1 1 1), :y 0.1705675999999987})} {:name \"08783f0b-888e-44b7-ba53-353cd8955c2b\", :values ({:x (0 0 0), :y 0.111788824} {:x (0 0 1), :y 0.14848515} {:x (0 1 0), :y 0.0869913} {:x (0 1 1), :y 0.10408959} {:x (1 0 0), :y 0.1339866} {:x (1 0 1), :y 0.17878212} {:x (1 1 0), :y 0.0989901} {:x (1 1 1), :y 0.13688631})}), :marks ({:type \"rect\", :from {:data \"6c7f2180-917e-4078-b5c8-0918fea01a4f\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"08783f0b-888e-44b7-ba53-353cd8955c2b\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; **
;;; ## Final weight distribution (last 1000 simulation steps)
;; **

;; @@
(def weights-history-tensor (a/get-hdf5-tensor store (:weights_history.h5 blobs) "/weights_history"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;affectionate-brook/weights-history-tensor</span>","value":"#'affectionate-brook/weights-history-tensor"}
;; <=

;; @@
(plot/histogram (apply concat (take-last 1000 weights-history-tensor)) 
                :bins 20 :plot-size 600)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"d0b834a1-6ca8-47ee-ad23-8c1bcba12799","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"d0b834a1-6ca8-47ee-ad23-8c1bcba12799","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"d0b834a1-6ca8-47ee-ad23-8c1bcba12799"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"d0b834a1-6ca8-47ee-ad23-8c1bcba12799","values":[{"x":-0.005244078772498443,"y":0},{"x":-0.004887444800115166,"y":1000.0},{"x":-0.00453081082773189,"y":2000.0},{"x":-0.004174176855348613,"y":1000.0},{"x":-0.003817542882965336,"y":1000.0},{"x":-0.003460908910582059,"y":3000.0},{"x":-0.003104274938198782,"y":0.0},{"x":-0.002747640965815505,"y":1000.0},{"x":-0.0023910069934322277,"y":0.0},{"x":-0.0020343730210489507,"y":0.0},{"x":-0.0016777390486656736,"y":0.0},{"x":-0.0013211050762823966,"y":0.0},{"x":-9.644711038991196E-4,"y":2000.0},{"x":-6.078371315158427E-4,"y":2000.0},{"x":-2.512031591325657E-4,"y":5000.0},{"x":1.0543081325071124E-4,"y":7000.0},{"x":4.620647856339882E-4,"y":8000.0},{"x":8.186987580172651E-4,"y":4000.0},{"x":0.001175332730400542,"y":3000.0},{"x":0.001531966702783819,"y":7000.0},{"x":0.001888600675167096,"y":6000.0},{"x":0.002245234647550373,"y":1000.0},{"x":0.0026018686199336502,"y":0}]}],"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"d0b834a1-6ca8-47ee-ad23-8c1bcba12799\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"d0b834a1-6ca8-47ee-ad23-8c1bcba12799\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"d0b834a1-6ca8-47ee-ad23-8c1bcba12799\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"d0b834a1-6ca8-47ee-ad23-8c1bcba12799\", :values ({:x -0.005244078772498443, :y 0} {:x -0.004887444800115166, :y 1000.0} {:x -0.00453081082773189, :y 2000.0} {:x -0.004174176855348613, :y 1000.0} {:x -0.003817542882965336, :y 1000.0} {:x -0.003460908910582059, :y 3000.0} {:x -0.003104274938198782, :y 0.0} {:x -0.002747640965815505, :y 1000.0} {:x -0.0023910069934322277, :y 0.0} {:x -0.0020343730210489507, :y 0.0} {:x -0.0016777390486656736, :y 0.0} {:x -0.0013211050762823966, :y 0.0} {:x -9.644711038991196E-4, :y 2000.0} {:x -6.078371315158427E-4, :y 2000.0} {:x -2.512031591325657E-4, :y 5000.0} {:x 1.0543081325071124E-4, :y 7000.0} {:x 4.620647856339882E-4, :y 8000.0} {:x 8.186987580172651E-4, :y 4000.0} {:x 0.001175332730400542, :y 3000.0} {:x 0.001531966702783819, :y 7000.0} {:x 0.001888600675167096, :y 6000.0} {:x 0.002245234647550373, :y 1000.0} {:x 0.0026018686199336502, :y 0})}], :width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; **
;;; ## Final bias distribution
;; **

;; @@
(def bias-history-tensor 
  (a/get-hdf5-tensor store (:bias_history.h5 blobs) "/bias_history"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;affectionate-brook/bias-history-tensor</span>","value":"#'affectionate-brook/bias-history-tensor"}
;; <=

;; @@
(plot/histogram (apply concat (take-last 1000 bias-history-tensor)) 
                :bins 20 :plot-size 600)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"650c54b4-54dd-4fe4-852c-4c2e03ac93af","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"650c54b4-54dd-4fe4-852c-4c2e03ac93af","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"650c54b4-54dd-4fe4-852c-4c2e03ac93af"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"650c54b4-54dd-4fe4-852c-4c2e03ac93af","values":[{"x":-0.001183809158303044,"y":0},{"x":-0.0010826650105778936,"y":1000.0},{"x":-9.815208628527433E-4,"y":0.0},{"x":-8.803767151275929E-4,"y":0.0},{"x":-7.792325674024425E-4,"y":1000.0},{"x":-6.78088419677292E-4,"y":0.0},{"x":-5.769442719521416E-4,"y":0.0},{"x":-4.758001242269912E-4,"y":3000.0},{"x":-3.7465597650184076E-4,"y":0.0},{"x":-2.7351182877669033E-4,"y":0.0},{"x":-1.7236768105153994E-4,"y":1000.0},{"x":-7.122353332638954E-5,"y":2000.0},{"x":2.9920614398760856E-5,"y":1000.0},{"x":1.3106476212391125E-4,"y":1000.0},{"x":2.3220890984906165E-4,"y":0.0},{"x":3.3335305757421205E-4,"y":0.0},{"x":4.344972052993624E-4,"y":0.0},{"x":5.356413530245128E-4,"y":0.0},{"x":6.367855007496633E-4,"y":0.0},{"x":7.379296484748137E-4,"y":1000.0},{"x":8.390737961999641E-4,"y":1000.0},{"x":9.402179439251145E-4,"y":0}]}],"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"650c54b4-54dd-4fe4-852c-4c2e03ac93af\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"650c54b4-54dd-4fe4-852c-4c2e03ac93af\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"650c54b4-54dd-4fe4-852c-4c2e03ac93af\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"650c54b4-54dd-4fe4-852c-4c2e03ac93af\", :values ({:x -0.001183809158303044, :y 0} {:x -0.0010826650105778936, :y 1000.0} {:x -9.815208628527433E-4, :y 0.0} {:x -8.803767151275929E-4, :y 0.0} {:x -7.792325674024425E-4, :y 1000.0} {:x -6.78088419677292E-4, :y 0.0} {:x -5.769442719521416E-4, :y 0.0} {:x -4.758001242269912E-4, :y 3000.0} {:x -3.7465597650184076E-4, :y 0.0} {:x -2.7351182877669033E-4, :y 0.0} {:x -1.7236768105153994E-4, :y 1000.0} {:x -7.122353332638954E-5, :y 2000.0} {:x 2.9920614398760856E-5, :y 1000.0} {:x 1.3106476212391125E-4, :y 1000.0} {:x 2.3220890984906165E-4, :y 0.0} {:x 3.3335305757421205E-4, :y 0.0} {:x 4.344972052993624E-4, :y 0.0} {:x 5.356413530245128E-4, :y 0.0} {:x 6.367855007496633E-4, :y 0.0} {:x 7.379296484748137E-4, :y 1000.0} {:x 8.390737961999641E-4, :y 1000.0} {:x 9.402179439251145E-4, :y 0})}], :width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; **
;;; ## History of weights in training (avg pink, variance green)
;; **

;; @@
(def weights-history-avgs-tensor 
  (a/get-hdf5-tensor store (:weight_avgs.h5 blobs) "/weight_avgs"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;affectionate-brook/weights-history-avgs-tensor</span>","value":"#'affectionate-brook/weights-history-avgs-tensor"}
;; <=

;; @@
#_(plot/compose
  (plot/list-plot (map first weights-history-avgs-tensor) 
                  :joined true :plot-range [:all [-0.0004 0.0020]] :plot-size 600)
  (plot/list-plot (map second weights-history-avgs-tensor) 
                  :joined true :color "green"))
;; @@

;; **
;;; ![history](project-files/repo/store/%23uuid%20"26fc6a44-674b-5928-8e17-d3dfa539c92a")
;; **

;; @@

;; @@
