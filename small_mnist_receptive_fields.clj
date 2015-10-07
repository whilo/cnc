;; gorilla-repl.fileformat = 1

;; **
;;; # Small Digits
;; **

;; @@
(ns digits-receptive-fields
  (:require [gorilla-plot.core :as plot]
            [gg4clj.core :as gg4clj]
            [clojure.math.combinatorics :refer [cartesian-product]]
            [cnc.analytics :as a]
            [cnc.execute :as exe]
            [clojure.pprint :refer [pprint]]
            [hasch.core :refer [uuid]]
            [boltzmann.matrix :refer [full-matrix]]
            [clojure.java.shell :refer [sh]]
            [cnc.core :refer [state]]
            [cnc.execute :refer [slurp-bytes]]
            [clojure.core.matrix :as mat]
            [clj-hdf5.core :as hdf5]
            [konserve.protocols :refer [-get-in -bget -exists?]]
            [full.async :refer [<??]]
            [boltzmann.core :refer [train-cd sample-gibbs]]
            [boltzmann.theoretical :refer [create-theoretical-rbm]]
            [boltzmann.jblas :refer [create-jblas-rbm]]
   			[clatrix.core :as clat]
            [boltzmann.formulas :as f]
            [boltzmann.protocols :refer [-weights -biases]]
            [boltzmann.visualize :as v]
            [clojure.core.matrix :refer [dot matrix]]
            [datomic.api :as d]
            [clojure.core.async :refer [chan] :as async]
            [quil.core :as q]
            [cheshire.core :as json]
            [clojure.java.io :as io]))
(def store (get-in @state [:repo :store]))

(defn write-json [base-dir name coll]
  (with-open [w (io/writer (str base-dir name))]
    (json/generate-stream coll w)))

(def conn (a/conn "train current rbms"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/conn</span>","value":"#'digits-receptive-fields/conn"}
;; <=

;; @@
(->> (d/q '[:find ?vls ?w-hist ?b-hist
       :where
       #_[?exp :ref/data #uuid "18127501-df3c-578d-8863-a3e17f2a61a7"]
       [?exp :ref/trans-params ?vid]
       [?exp :ref/training-params ?train-params-id]
       [(cnc.analytics/load-key ?vid) ?vls]
       [(:base-directory ?vls) ?base-dir]
       [(.contains ?base-dir "digits")]
       [(:output ?vls) ?out]
       [(:weight_theo_history.h5 ?out) ?w-hist]
       [(:bias_theo_history.h5 ?out) ?b-hist]]
     (d/db conn))
     (map (fn [[p w b]]
               (let [c (/ (-> (a/get-hdf5-tensor store w "/weight") first count) 2)
                     vc (- (-> (a/get-hdf5-tensor store b "/weight") first count) 
                           (-> p :exp-params :training-params :h_count))
                     wh (mapv #(->> % 
                                    (drop c) 
                                    (partition vc)
                                    (mapv vec))
                              (take-nth 10
                                        (a/get-hdf5-tensor store w "/weight")))
                     vbh (mapv #(->> % (take vc) vec) 
                               (take-nth 10
                                         (a/get-hdf5-tensor store b "/weight")))
                     hbh (mapv #(->> % (drop vc) vec) 
                               (take-nth 10
                                         (a/get-hdf5-tensor store b "/weight")))]
               (assoc p 
                 :restricted-weights (last wh)
                 :weight-history wh
                 
                 :v-biases (last vbh)
                 :v-bias-history vbh
                 :h-biases (last hbh)
                 :h-bias-history hbh))))
     (def digit-exps))

;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/digit-exps</span>","value":"#'digits-receptive-fields/digit-exps"}
;; <=

;; @@
(for [e digit-exps]
  (get-in e [:exp-params :training-params]))
;; @@

;; @@
(def digits (<?? (-bget store #uuid "121ab1db-a1e0-5bed-9933-6330f63fba65" 
                        #(-> % :input-stream slurp read-string))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/digits</span>","value":"#'digits-receptive-fields/digits"}
;; <=

;; @@
(def digit-exp (first digit-exps))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/digit-exp</span>","value":"#'digits-receptive-fields/digit-exp"}
;; <=

;; @@
;; fallback if replication doesn't work
(def weight-history (mapv #(->> % (drop 500) (partition 25) (v/receptive-fields 5)) 
                                   (take-nth 16 (hdf5/read (hdf5/get-dataset (hdf5/open "/home/void/ignatz/cnc/experiments/digits_ad45ed54/weight_theo_history.h5") "/weight")))))
;; @@

;; @@
(defn horizontal-tile [float-matrices]
  (->> float-matrices
       (apply interleave)
       (apply concat)
       (partition (* (count float-matrices)
                     (count (first float-matrices))))))

(defn tile [width float-matrices]
  (->> float-matrices
       (partition width)
       (map horizontal-tile)
       (apply concat)))


;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/tile</span>","value":"#'digits-receptive-fields/tile"}
;; <=

;; @@
(defn setup []
  (q/smooth)                          ;; Turn on anti-aliasing
  (q/frame-rate 100)                    ;; Set framerate to 1 FPS
  (q/background 200 100 100))


(defn draw-rects ([pixel-matrix] (draw-rects pixel-matrix 255))
  ([pixel-matrix alpha]
  (let [hc (count (first pixel-matrix))
        vc (count pixel-matrix)
        w (/ (q/width) hc)
        h (/ (q/height) vc)]
    (doseq [x (range hc)
            y (range vc)]
      (q/fill (* (+ 0.5 (* 0.5 (min (max (get-in pixel-matrix [y x]) -3) 3))) 255) alpha)
      (q/rect (* x w) (* y h) w h)))))

(def weight-atom (atom (:weight-history digit-exp) #_trad-weight-traces))
(def frame-counter (atom 0))
#_(def bias-atom (atom (:bias-history digit-exp)))

(q/defsketch example                  ;; Define a new sketch named example
  :title "Receptive fields"    ;; Set the title of the sketch
  :setup setup                        ;; Specify the setup fn
  :draw (fn draw []
          (let [fw (first @weight-atom)
                fb (repeat 184 0) #_(first @bias-atom)]
            (when fw
              (q/stroke 255 0 0 0)
              (q/stroke-weight 3)
            (->> fw
                 (map #(partition 12 (take 144 %)))
                 (tile 5) 
                 (mapv vec) 
                 draw-rects)
              (q/stroke 255 0 0 255)
              (q/stroke-weight 3)
              (draw-rects (mapv vec (partition 5 (drop 144 fb))) 0)
              #_(q/save (str "/var/tmp/frames/" (swap! frame-counter inc) ".png"))
            (swap! weight-atom rest)
              #_(swap! bias-atom rest))))                          ;; Specify the draw fn
  :size [500 500])                    ;; You struggle to beat the golden ratio
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/example</span>","value":"#'digits-receptive-fields/example"}
;; <=

;; @@
(mat/set-current-implementation :clatrix)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-keyword'>:clatrix</span>","value":":clatrix"}
;; <=

;; @@
(def back-ch (chan 3e3 (take-nth 100)))
(def trad-rbm (train-cd (create-jblas-rbm 154 40)
          (mat/matrix digits)
          :epochs 10000
          :init-learning-rate 0.1
          :learning-rate-fn (fn [i s] i #_(* i (/ 50 (+ 50 s))))
          :back-ch back-ch
          :k 1))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/trad-rbm</span>","value":"#'digits-receptive-fields/trad-rbm"}
;; <=

;; @@
(async/close! back-ch)
(def history (<?? (async/into [] back-ch)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/history</span>","value":"#'digits-receptive-fields/history"}
;; <=

;; @@
(count history)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>300</span>","value":"300"}
;; <=

;; @@
(def trad-weight-traces (take-nth 1 (map first history)))

;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/trad-weight-traces</span>","value":"#'digits-receptive-fields/trad-weight-traces"}
;; <=

;; @@
(def theo-samples (sample-gibbs trad-rbm 2e3))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/theo-samples</span>","value":"#'digits-receptive-fields/theo-samples"}
;; <=

;; @@
  (def rendered (->> (take 2e3 theo-samples)
                     (map #(partition 12 (take 144 %)))
                     (tile 100)
                     v/render-grayscale-float-matrix))

  (v/view rendered)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>#object[javax.swing.JFrame 0xb9b9d9b &quot;javax.swing.JFrame[frame2,0,24,1200x240,layout=java.awt.BorderLayout,title=MNIST Digit,normal,defaultCloseOperation=HIDE_ON_CLOSE,rootPane=javax.swing.JRootPane[,0,0,1200x240,layout=javax.swing.JRootPane$RootLayout,alignmentX=0.0,alignmentY=0.0,border=,flags=16777673,maximumSize=,minimumSize=,preferredSize=],rootPaneCheckingEnabled=true]&quot;]</span>","value":"#object[javax.swing.JFrame 0xb9b9d9b \"javax.swing.JFrame[frame2,0,24,1200x240,layout=java.awt.BorderLayout,title=MNIST Digit,normal,defaultCloseOperation=HIDE_ON_CLOSE,rootPane=javax.swing.JRootPane[,0,0,1200x240,layout=javax.swing.JRootPane$RootLayout,alignmentX=0.0,alignmentY=0.0,border=,flags=16777673,maximumSize=,minimumSize=,preferredSize=],rootPaneCheckingEnabled=true]\"]"}
;; <=

;; @@
(import '[javax.imageio ImageIO])
(ImageIO/write rendered "png" (io/file "/tmp/mnist.png"))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-unkown'>true</span>","value":"true"}
;; <=

;; @@

;; @@
