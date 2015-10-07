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
       [?exp :ref/data #uuid "18127501-df3c-578d-8863-a3e17f2a61a7"]
       [?exp :ref/trans-params ?vid]
       [?exp :ref/training-params ?train-params-id]
       [(cnc.analytics/load-key ?vid) ?vls]
       [(:base-directory ?vls) ?base-dir]
       [(.contains ?base-dir "digits_dc7d")]
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
#_(-> digit-exps first :exp-params)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/digit-exps</span>","value":"#'digits-receptive-fields/digit-exps"}
;; <=

;; @@
(def digits (<?? (-bget store #uuid "18127501-df3c-578d-8863-a3e17f2a61a7" 
                        #(-> % :input-stream slurp read-string))))
digits
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[0 1 1 1 0 0 0 0 0 1 0 0 1 1 0 0 0 0 0 1 0 1 1 1 0]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[1 0 0 0 0 1 0 1 0 0 1 1 1 1 0 0 0 1 0 0 0 0 1 0 0]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>1</span>","value":"1"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"},{"type":"html","content":"<span class='clj-long'>0</span>","value":"0"}],"value":"[1 1 1 1 0 1 0 0 0 0 1 1 1 0 0 0 0 0 1 0 1 1 1 0 0]"}],"value":"[[0 1 1 1 0 0 0 0 0 1 0 0 1 1 0 0 0 0 0 1 0 1 1 1 0] [1 0 0 0 0 1 0 1 0 0 1 1 1 1 0 0 0 1 0 0 0 0 1 0 0] [1 1 1 1 0 1 0 0 0 0 1 1 1 0 0 0 0 0 1 0 1 1 1 0 0]]"}
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
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/weight-history</span>","value":"#'digits-receptive-fields/weight-history"}
;; <=

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

(def weight-atom (atom weight-history #_(:weight-history digit-exp)))
(def frame-counter (atom 0))
#_(def bias-atom (atom (:bias-history digit-exp)))

(q/defsketch example                  ;; Define a new sketch named example
  :title "Receptive fields"    ;; Set the title of the sketch
  :setup setup                        ;; Specify the setup fn
  :draw (fn draw []
          (let [fw (first @weight-atom)
                fb (repeat 45 0) #_(first @bias-atom)]
            (when fw
              (q/stroke 255 0 0 0)
              (q/stroke-weight 3)
            (->> fw
                 (tile 5) 
                 (mapv vec) 
                 draw-rects)
              (q/stroke 255 0 0 255)
              (q/stroke-weight 3)
              (draw-rects (mapv vec (partition 5 (drop 25 fb))) 0)
              #_(q/save (str "/var/tmp/frames/" (swap! frame-counter inc) ".png"))
            (swap! weight-atom rest)
              #_(swap! bias-atom rest))))                          ;; Specify the draw fn
  :size [500 500])                    ;; You struggle to beat the golden ratio
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;digits-receptive-fields/example</span>","value":"#'digits-receptive-fields/example"}
;; <=

;; @@

;; @@
