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
    (<!? (-get-in store [#uuid "3a70fab5-b1c0-5a77-bc5f-2e804bbc519b"])))
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
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"bf600c7c-a7fe-4418-b98c-4a5375956005","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"bf600c7c-a7fe-4418-b98c-4a5375956005","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"bf600c7c-a7fe-4418-b98c-4a5375956005","values":[{"x":[0,0],"y":1.0},{"x":[0,1],"y":0.0},{"x":[1,0],"y":0.0},{"x":[1,1],"y":0.0}]},{"name":"a8f93b4d-d0a7-4bce-bc91-630e1d177473","values":[{"x":[0,0],"y":0.147185280919075},{"x":[0,1],"y":0.26907309889793396},{"x":[1,0],"y":0.4376562237739563},{"x":[1,1],"y":0.14608539640903473}]}],"marks":[{"type":"rect","from":{"data":"bf600c7c-a7fe-4418-b98c-4a5375956005"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"a8f93b4d-d0a7-4bce-bc91-630e1d177473"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"steelblue"},"opacity":{"value":0.5}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"bf600c7c-a7fe-4418-b98c-4a5375956005\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"bf600c7c-a7fe-4418-b98c-4a5375956005\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"bf600c7c-a7fe-4418-b98c-4a5375956005\", :values ({:x (0 0), :y 1.0} {:x (0 1), :y 0.0} {:x (1 0), :y 0.0} {:x (1 1), :y 0.0})} {:name \"a8f93b4d-d0a7-4bce-bc91-630e1d177473\", :values ({:x (0 0), :y 0.14718528} {:x (0 1), :y 0.2690731} {:x (1 0), :y 0.43765622} {:x (1 1), :y 0.1460854})}), :marks ({:type \"rect\", :from {:data \"bf600c7c-a7fe-4418-b98c-4a5375956005\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"a8f93b4d-d0a7-4bce-bc91-630e1d177473\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"steelblue\"}, :opacity {:value 0.5}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
;; <=

;; @@
(def weights-history-tensor 
  (a/get-hdf5-tensor store (:weights_history.h5 blobs) "/weights_history"))
(partition 7 (last weights-history-tensor))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>-0.00286292415927035</span>","value":"-0.00286292415927035"},{"type":"html","content":"<span class='clj-double'>-0.002048442392857577</span>","value":"-0.002048442392857577"},{"type":"html","content":"<span class='clj-double'>-0.0027836422602909522</span>","value":"-0.0027836422602909522"},{"type":"html","content":"<span class='clj-double'>-0.003945354577332286</span>","value":"-0.003945354577332286"},{"type":"html","content":"<span class='clj-double'>-0.002087944709918562</span>","value":"-0.002087944709918562"},{"type":"html","content":"<span class='clj-double'>-0.006609196995871262</span>","value":"-0.006609196995871262"},{"type":"html","content":"<span class='clj-double'>-0.0026387280639916864</span>","value":"-0.0026387280639916864"}],"value":"(-0.00286292415927035 -0.002048442392857577 -0.0027836422602909522 -0.003945354577332286 -0.002087944709918562 -0.006609196995871262 -0.0026387280639916864)"},{"type":"list-like","open":"<span class='clj-lazy-seq'>(</span>","close":"<span class='clj-lazy-seq'>)</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>-0.003173677701515195</span>","value":"-0.003173677701515195"},{"type":"html","content":"<span class='clj-double'>-0.0021457292046024746</span>","value":"-0.0021457292046024746"},{"type":"html","content":"<span class='clj-double'>-0.0030592932383629785</span>","value":"-0.0030592932383629785"},{"type":"html","content":"<span class='clj-double'>-0.00286292415927035</span>","value":"-0.00286292415927035"},{"type":"html","content":"<span class='clj-double'>-0.006609196995871262</span>","value":"-0.006609196995871262"},{"type":"html","content":"<span class='clj-double'>-0.002048442392857577</span>","value":"-0.002048442392857577"},{"type":"html","content":"<span class='clj-double'>-0.0026387280639916864</span>","value":"-0.0026387280639916864"}],"value":"(-0.003173677701515195 -0.0021457292046024746 -0.0030592932383629785 -0.00286292415927035 -0.006609196995871262 -0.002048442392857577 -0.0026387280639916864)"}],"value":"((-0.00286292415927035 -0.002048442392857577 -0.0027836422602909522 -0.003945354577332286 -0.002087944709918562 -0.006609196995871262 -0.0026387280639916864) (-0.003173677701515195 -0.0021457292046024746 -0.0030592932383629785 -0.00286292415927035 -0.006609196995871262 -0.002048442392857577 -0.0026387280639916864))"}
;; <=

;; @@
(plot/histogram (apply concat (take-last 1000 weights-history-tensor)) 
                :bins 40 :plot-size 600)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"16117853-ed26-448d-aa9c-69274ffa9815","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"16117853-ed26-448d-aa9c-69274ffa9815","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"16117853-ed26-448d-aa9c-69274ffa9815"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"16117853-ed26-448d-aa9c-69274ffa9815","values":[{"x":-0.006609196995871262,"y":0},{"x":-0.006495178130795919,"y":2000.0},{"x":-0.006381159265720577,"y":0.0},{"x":-0.006267140400645234,"y":0.0},{"x":-0.0061531215355698915,"y":0.0},{"x":-0.006039102670494549,"y":0.0},{"x":-0.005925083805419206,"y":0.0},{"x":-0.005811064940343864,"y":0.0},{"x":-0.005697046075268521,"y":0.0},{"x":-0.005583027210193179,"y":0.0},{"x":-0.005469008345117836,"y":0.0},{"x":-0.005354989480042494,"y":0.0},{"x":-0.005240970614967151,"y":0.0},{"x":-0.005126951749891809,"y":0.0},{"x":-0.005012932884816466,"y":0.0},{"x":-0.0048989140197411235,"y":0.0},{"x":-0.004784895154665781,"y":0.0},{"x":-0.0046708762895904385,"y":0.0},{"x":-0.004556857424515096,"y":0.0},{"x":-0.004442838559439753,"y":0.0},{"x":-0.004328819694364411,"y":0.0},{"x":-0.004214800829289068,"y":0.0},{"x":-0.004100781964213726,"y":1763.0},{"x":-0.003986763099138383,"y":0.0},{"x":-0.003872744234063041,"y":237.0},{"x":-0.003758725368987699,"y":0.0},{"x":-0.003644706503912357,"y":0.0},{"x":-0.0035306876388370148,"y":0.0},{"x":-0.0034166687737616727,"y":0.0},{"x":-0.0033026499086863306,"y":0.0},{"x":-0.0031886310436109884,"y":0.0},{"x":-0.0030746121785356463,"y":2000.0},{"x":-0.0029605933134603042,"y":2000.0},{"x":-0.002846574448384962,"y":2000.0},{"x":-0.00273255558330962,"y":2000.0},{"x":-0.002618536718234278,"y":2000.0},{"x":-0.002504517853158936,"y":0.0},{"x":-0.0023904989880835937,"y":0.0},{"x":-0.0022764801230082516,"y":0.0},{"x":-0.0021624612579329095,"y":192.0},{"x":-0.0020484423928575674,"y":5808.0},{"x":-0.0019344235277822253,"y":0}]}],"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"16117853-ed26-448d-aa9c-69274ffa9815\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"16117853-ed26-448d-aa9c-69274ffa9815\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"16117853-ed26-448d-aa9c-69274ffa9815\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"16117853-ed26-448d-aa9c-69274ffa9815\", :values ({:x -0.006609196995871262, :y 0} {:x -0.006495178130795919, :y 2000.0} {:x -0.006381159265720577, :y 0.0} {:x -0.006267140400645234, :y 0.0} {:x -0.0061531215355698915, :y 0.0} {:x -0.006039102670494549, :y 0.0} {:x -0.005925083805419206, :y 0.0} {:x -0.005811064940343864, :y 0.0} {:x -0.005697046075268521, :y 0.0} {:x -0.005583027210193179, :y 0.0} {:x -0.005469008345117836, :y 0.0} {:x -0.005354989480042494, :y 0.0} {:x -0.005240970614967151, :y 0.0} {:x -0.005126951749891809, :y 0.0} {:x -0.005012932884816466, :y 0.0} {:x -0.0048989140197411235, :y 0.0} {:x -0.004784895154665781, :y 0.0} {:x -0.0046708762895904385, :y 0.0} {:x -0.004556857424515096, :y 0.0} {:x -0.004442838559439753, :y 0.0} {:x -0.004328819694364411, :y 0.0} {:x -0.004214800829289068, :y 0.0} {:x -0.004100781964213726, :y 1763.0} {:x -0.003986763099138383, :y 0.0} {:x -0.003872744234063041, :y 237.0} {:x -0.003758725368987699, :y 0.0} {:x -0.003644706503912357, :y 0.0} {:x -0.0035306876388370148, :y 0.0} {:x -0.0034166687737616727, :y 0.0} {:x -0.0033026499086863306, :y 0.0} {:x -0.0031886310436109884, :y 0.0} {:x -0.0030746121785356463, :y 2000.0} {:x -0.0029605933134603042, :y 2000.0} {:x -0.002846574448384962, :y 2000.0} {:x -0.00273255558330962, :y 2000.0} {:x -0.002618536718234278, :y 2000.0} {:x -0.002504517853158936, :y 0.0} {:x -0.0023904989880835937, :y 0.0} {:x -0.0022764801230082516, :y 0.0} {:x -0.0021624612579329095, :y 192.0} {:x -0.0020484423928575674, :y 5808.0} {:x -0.0019344235277822253, :y 0})}], :width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
;; <=

;; @@
(def bias-history-tensor 
  (a/get-hdf5-tensor store (:bias_history.h5 blobs) "/bias_history"))
;; @@

;; @@
(plot/histogram (apply concat (take-last 1000 bias-history-tensor)) 
                :bins 30 :plot-size 600)
;; @@
;; =>
;;; {"type":"vega","content":{"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"scales":[{"name":"x","type":"linear","range":"width","zero":false,"domain":{"data":"be2ddd70-3ff6-4b23-8225-79e732894525","field":"data.x"}},{"name":"y","type":"linear","range":"height","nice":true,"zero":false,"domain":{"data":"be2ddd70-3ff6-4b23-8225-79e732894525","field":"data.y"}}],"marks":[{"type":"line","from":{"data":"be2ddd70-3ff6-4b23-8225-79e732894525"},"properties":{"enter":{"x":{"scale":"x","field":"data.x"},"y":{"scale":"y","field":"data.y"},"interpolate":{"value":"step-before"},"fill":{"value":"steelblue"},"fillOpacity":{"value":0.4},"stroke":{"value":"steelblue"},"strokeWidth":{"value":2},"strokeOpacity":{"value":1}}}}],"data":[{"name":"be2ddd70-3ff6-4b23-8225-79e732894525","values":[{"x":-0.007045275833854129,"y":0},{"x":-0.006785453730562431,"y":1000.0},{"x":-0.006525631627270733,"y":0.0},{"x":-0.006265809523979035,"y":0.0},{"x":-0.006005987420687337,"y":0.0},{"x":-0.005746165317395639,"y":0.0},{"x":-0.0054863432141039415,"y":0.0},{"x":-0.0052265211108122435,"y":0.0},{"x":-0.004966699007520546,"y":1000.0},{"x":-0.004706876904228848,"y":1000.0},{"x":-0.00444705480093715,"y":0.0},{"x":-0.004187232697645452,"y":0.0},{"x":-0.003927410594353754,"y":0.0},{"x":-0.0036675884910620563,"y":0.0},{"x":-0.003407766387770359,"y":0.0},{"x":-0.0031479442844786613,"y":0.0},{"x":-0.0028881221811869638,"y":0.0},{"x":-0.0026283000778952663,"y":0.0},{"x":-0.0023684779746035688,"y":0.0},{"x":-0.0021086558713118713,"y":0.0},{"x":-0.0018488337680201735,"y":0.0},{"x":-0.0015890116647284758,"y":1000.0},{"x":-0.001329189561436778,"y":0.0},{"x":-0.0010693674581450804,"y":1000.0},{"x":-8.095453548533826E-4,"y":0.0},{"x":-5.497232515616849E-4,"y":0.0},{"x":-2.8990114826998723E-4,"y":1000.0},{"x":-3.007904497828956E-5,"y":0.0},{"x":2.297430583134081E-4,"y":0.0},{"x":4.895651616051058E-4,"y":0.0},{"x":7.493872648968035E-4,"y":1000.0},{"x":0.0010092093681885012,"y":0}]}],"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50}},"value":"#gorilla_repl.vega.VegaView{:content {:axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :scales [{:name \"x\", :type \"linear\", :range \"width\", :zero false, :domain {:data \"be2ddd70-3ff6-4b23-8225-79e732894525\", :field \"data.x\"}} {:name \"y\", :type \"linear\", :range \"height\", :nice true, :zero false, :domain {:data \"be2ddd70-3ff6-4b23-8225-79e732894525\", :field \"data.y\"}}], :marks [{:type \"line\", :from {:data \"be2ddd70-3ff6-4b23-8225-79e732894525\"}, :properties {:enter {:x {:scale \"x\", :field \"data.x\"}, :y {:scale \"y\", :field \"data.y\"}, :interpolate {:value \"step-before\"}, :fill {:value \"steelblue\"}, :fillOpacity {:value 0.4}, :stroke {:value \"steelblue\"}, :strokeWidth {:value 2}, :strokeOpacity {:value 1}}}}], :data [{:name \"be2ddd70-3ff6-4b23-8225-79e732894525\", :values ({:x -0.007045275833854129, :y 0} {:x -0.006785453730562431, :y 1000.0} {:x -0.006525631627270733, :y 0.0} {:x -0.006265809523979035, :y 0.0} {:x -0.006005987420687337, :y 0.0} {:x -0.005746165317395639, :y 0.0} {:x -0.0054863432141039415, :y 0.0} {:x -0.0052265211108122435, :y 0.0} {:x -0.004966699007520546, :y 1000.0} {:x -0.004706876904228848, :y 1000.0} {:x -0.00444705480093715, :y 0.0} {:x -0.004187232697645452, :y 0.0} {:x -0.003927410594353754, :y 0.0} {:x -0.0036675884910620563, :y 0.0} {:x -0.003407766387770359, :y 0.0} {:x -0.0031479442844786613, :y 0.0} {:x -0.0028881221811869638, :y 0.0} {:x -0.0026283000778952663, :y 0.0} {:x -0.0023684779746035688, :y 0.0} {:x -0.0021086558713118713, :y 0.0} {:x -0.0018488337680201735, :y 0.0} {:x -0.0015890116647284758, :y 1000.0} {:x -0.001329189561436778, :y 0.0} {:x -0.0010693674581450804, :y 1000.0} {:x -8.095453548533826E-4, :y 0.0} {:x -5.497232515616849E-4, :y 0.0} {:x -2.8990114826998723E-4, :y 1000.0} {:x -3.007904497828956E-5, :y 0.0} {:x 2.297430583134081E-4, :y 0.0} {:x 4.895651616051058E-4, :y 0.0} {:x 7.493872648968035E-4, :y 1000.0} {:x 0.0010092093681885012, :y 0})}], :width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}}}"}
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
;;; ![history](project-files/repo/store/%23uuid%20"22c394c0-b111-583d-9056-938e29f66802")
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
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"76f4b73a-ddf0-4bbf-817a-08c73a0e037c","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"76f4b73a-ddf0-4bbf-817a-08c73a0e037c","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"76f4b73a-ddf0-4bbf-817a-08c73a0e037c","values":[{"x":[0,0],"y":1.0},{"x":[0,1],"y":0.0},{"x":[1,0],"y":0.0},{"x":[1,1],"y":0.0}]},{"name":"63ae46c0-a958-4923-9442-5d34d60224d0","values":[{"x":[0,0],"y":0.24104209999999573},{"x":[0,1],"y":0.24794790000000427},{"x":[1,0],"y":0.2519579000000043},{"x":[1,1],"y":0.25905209999999573}]}],"marks":[{"type":"rect","from":{"data":"76f4b73a-ddf0-4bbf-817a-08c73a0e037c"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"red"},"opacity":{"value":0.2}},"hover":{"fill":{"value":"#FF29D2"}}}},{"type":"rect","from":{"data":"63ae46c0-a958-4923-9442-5d34d60224d0"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"blue"},"opacity":{"value":0.2}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"76f4b73a-ddf0-4bbf-817a-08c73a0e037c\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"76f4b73a-ddf0-4bbf-817a-08c73a0e037c\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"76f4b73a-ddf0-4bbf-817a-08c73a0e037c\", :values ({:x (0 0), :y 1.0} {:x (0 1), :y 0.0} {:x (1 0), :y 0.0} {:x (1 1), :y 0.0})} {:name \"63ae46c0-a958-4923-9442-5d34d60224d0\", :values ({:x (0 0), :y 0.24104209999999573} {:x (0 1), :y 0.24794790000000427} {:x (1 0), :y 0.2519579000000043} {:x (1 1), :y 0.25905209999999573})}), :marks ({:type \"rect\", :from {:data \"76f4b73a-ddf0-4bbf-817a-08c73a0e037c\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"red\"}, :opacity {:value 0.2}}, :hover {:fill {:value \"#FF29D2\"}}}} {:type \"rect\", :from {:data \"63ae46c0-a958-4923-9442-5d34d60224d0\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"blue\"}, :opacity {:value 0.2}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
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
(pprint (mat/join-along 1 hidden-weights visi-weights))
;; @@
;; ->
;;; [[0.0 0.0 0.0 0.0 0.0 -0.00286292415927035 -0.003173677701515195]
;;;  [0.0 0.0 0.0 0.0 0.0 -0.006609196995871262 -0.003945354577332286]
;;;  [0.0 0.0 0.0 0.0 0.0 -0.002048442392857577 -0.0021457292046024746]
;;;  [0.0 0.0 0.0 0.0 0.0 -0.0026387280639916864 -0.002087944709918562]
;;;  [0.0 0.0 0.0 0.0 0.0 -0.0027836422602909522 -0.0030592932383629785]
;;;  [-0.00286292415927035
;;;   -0.002048442392857577
;;;   -0.0027836422602909522
;;;   -0.003945354577332286
;;;   -0.002087944709918562
;;;   0.0
;;;   0.0]
;;;  [-0.006609196995871262
;;;   -0.0026387280639916864
;;;   -0.003173677701515195
;;;   -0.0021457292046024746
;;;   -0.0030592932383629785
;;;   0.0
;;;   0.0]]
;;; 
;; <-
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(:output experiment)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-map'>{</span>","close":"<span class='clj-map'>}</span>","separator":", ","items":[{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:dist_joint_sim.h5</span>","value":":dist_joint_sim.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;3f7cfc8f-1b50-5016-b569-fcbc25397fbc&quot;</span>","value":"#uuid \"3f7cfc8f-1b50-5016-b569-fcbc25397fbc\""}],"value":"[:dist_joint_sim.h5 #uuid \"3f7cfc8f-1b50-5016-b569-fcbc25397fbc\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:spike_trains.h5</span>","value":":spike_trains.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;22aa7b89-479c-501a-ae20-fc2f0e69512e&quot;</span>","value":"#uuid \"22aa7b89-479c-501a-ae20-fc2f0e69512e\""}],"value":"[:spike_trains.h5 #uuid \"22aa7b89-479c-501a-ae20-fc2f0e69512e\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:training_overview.png</span>","value":":training_overview.png"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;22c394c0-b111-583d-9056-938e29f66802&quot;</span>","value":"#uuid \"22c394c0-b111-583d-9056-938e29f66802\""}],"value":"[:training_overview.png #uuid \"22c394c0-b111-583d-9056-938e29f66802\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:training_overview.pdf</span>","value":":training_overview.pdf"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;032ab631-85cb-5fbe-bdbe-9a6f8aa2797d&quot;</span>","value":"#uuid \"032ab631-85cb-5fbe-bdbe-9a6f8aa2797d\""}],"value":"[:training_overview.pdf #uuid \"032ab631-85cb-5fbe-bdbe-9a6f8aa2797d\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:weight_avgs.h5</span>","value":":weight_avgs.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;39b77e0a-0c1d-5a33-a148-1113dfa9fe20&quot;</span>","value":"#uuid \"39b77e0a-0c1d-5a33-a148-1113dfa9fe20\""}],"value":"[:weight_avgs.h5 #uuid \"39b77e0a-0c1d-5a33-a148-1113dfa9fe20\"]"},{"type":"list-like","open":"","close":"","separator":" ","items":[{"type":"html","content":"<span class='clj-keyword'>:weights_history.h5</span>","value":":weights_history.h5"},{"type":"html","content":"<span class='clj-unkown'>#uuid &quot;00ad7117-af93-5782-a31a-48d6062db46e&quot;</span>","value":"#uuid \"00ad7117-af93-5782-a31a-48d6062db46e\""}],"value":"[:weights_history.h5 #uuid \"00ad7117-af93-5782-a31a-48d6062db46e\"]"}],"value":"{:dist_joint_sim.h5 #uuid \"3f7cfc8f-1b50-5016-b569-fcbc25397fbc\", :spike_trains.h5 #uuid \"22aa7b89-479c-501a-ae20-fc2f0e69512e\", :training_overview.png #uuid \"22c394c0-b111-583d-9056-938e29f66802\", :training_overview.pdf #uuid \"032ab631-85cb-5fbe-bdbe-9a6f8aa2797d\", :weight_avgs.h5 #uuid \"39b77e0a-0c1d-5a33-a148-1113dfa9fe20\", :weights_history.h5 #uuid \"00ad7117-af93-5782-a31a-48d6062db46e\"}"}
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
  #_(plot/bar-chart states (map (partial get-in joint-tensor) states) 
                  :color "red" :opacity 0.2 :plot-size 600))
;; @@
;; =>
;;; {"type":"vega","content":{"width":600,"height":370.82818603515625,"padding":{"bottom":20,"top":10,"right":10,"left":50},"scales":[{"name":"x","type":"ordinal","range":"width","domain":{"data":"12376414-f07f-44a5-b6c1-89201360cf0b","field":"data.x"}},{"name":"y","range":"height","nice":true,"domain":{"data":"12376414-f07f-44a5-b6c1-89201360cf0b","field":"data.y"}}],"axes":[{"scale":"x","type":"x"},{"scale":"y","type":"y"}],"data":[{"name":"12376414-f07f-44a5-b6c1-89201360cf0b","values":[{"x":[0,0],"y":0.28008009999999933},{"x":[0,1],"y":0.09543990000000065},{"x":[1,0],"y":0.47184990000000065},{"x":[1,1],"y":0.15263009999999932}]}],"marks":[{"type":"rect","from":{"data":"12376414-f07f-44a5-b6c1-89201360cf0b"},"properties":{"enter":{"y":{"scale":"y","field":"data.y"},"width":{"offset":-1,"scale":"x","band":true},"x":{"scale":"x","field":"data.x"},"y2":{"scale":"y","value":0}},"update":{"fill":{"value":"blue"},"opacity":{"value":0.2}},"hover":{"fill":{"value":"#FF29D2"}}}}]},"value":"#gorilla_repl.vega.VegaView{:content {:width 600, :height 370.8282, :padding {:bottom 20, :top 10, :right 10, :left 50}, :scales [{:name \"x\", :type \"ordinal\", :range \"width\", :domain {:data \"12376414-f07f-44a5-b6c1-89201360cf0b\", :field \"data.x\"}} {:name \"y\", :range \"height\", :nice true, :domain {:data \"12376414-f07f-44a5-b6c1-89201360cf0b\", :field \"data.y\"}}], :axes [{:scale \"x\", :type \"x\"} {:scale \"y\", :type \"y\"}], :data ({:name \"12376414-f07f-44a5-b6c1-89201360cf0b\", :values ({:x (0 0), :y 0.28008009999999933} {:x (0 1), :y 0.09543990000000065} {:x (1 0), :y 0.47184990000000065} {:x (1 1), :y 0.15263009999999932})}), :marks ({:type \"rect\", :from {:data \"12376414-f07f-44a5-b6c1-89201360cf0b\"}, :properties {:enter {:y {:scale \"y\", :field \"data.y\"}, :width {:offset -1, :scale \"x\", :band true}, :x {:scale \"x\", :field \"data.x\"}, :y2 {:scale \"y\", :value 0}}, :update {:fill {:value \"blue\"}, :opacity {:value 0.2}}, :hover {:fill {:value \"#FF29D2\"}}}})}}"}
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
