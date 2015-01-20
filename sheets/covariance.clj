;; gorilla-repl.fileformat = 1

;; **
;;; # Gorilla REPL
;;; 
;;; Welcome to gorilla :-)
;;; 
;;; Shift + enter evaluates code. Hit alt+g twice in quick succession or click the menu icon (upper-right corner) for more commands ...
;;; 
;;; It's a good habit to run each worksheet in its own namespace: feel free to use the declaration we've provided below if you'd like.
;; **

;; @@
(ns solitary-moss
  (:require [gorilla-plot.core :as plot]
            [clojure.core.matrix :refer [matrix transpose] :as mat]))

(defn ones [n]
  (matrix (repeat n 1)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;solitary-moss/ones</span>","value":"#'solitary-moss/ones"}
;; <=

;; @@
(def data [[1 1] [1 1] [1 1]])
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;solitary-moss/data</span>","value":"#'solitary-moss/data"}
;; <=

;; @@
(mat/mul (matrix data) (matrix (repeat 3 1)))
;; @@

;; @@
(defn mean [data]
  (let [c (-> data first count)]
(vector (transpose (mat/scale (ones c) (/ 1 c)))
         (mat/mmul (matrix data) (ones c)))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;solitary-moss/mean</span>","value":"#'solitary-moss/mean"}
;; <=

;; @@
(mean data)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-ratio'>1/2</span>","value":"1/2"},{"type":"html","content":"<span class='clj-ratio'>1/2</span>","value":"1/2"}],"value":"[1/2 1/2]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"},{"type":"html","content":"<span class='clj-long'>2</span>","value":"2"}],"value":"[2 2 2]"}],"value":"[[1/2 1/2] [2 2 2]]"}
;; <=

;; @@
(defn cov [data]
  (let [m (mean data)]
     (mat/sub data (mat/mmul m (ones (count data))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;solitary-moss/cov</span>","value":"#'solitary-moss/cov"}
;; <=

;; @@
(cov data)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-bigint'>-1N</span>","value":"-1N"},{"type":"html","content":"<span class='clj-bigint'>-1N</span>","value":"-1N"},{"type":"html","content":"<span class='clj-bigint'>-1N</span>","value":"-1N"}],"value":"[-1N -1N -1N]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-bigint'>-1N</span>","value":"-1N"},{"type":"html","content":"<span class='clj-bigint'>-1N</span>","value":"-1N"},{"type":"html","content":"<span class='clj-bigint'>-1N</span>","value":"-1N"}],"value":"[-1N -1N -1N]"}],"value":"[[-1N -1N -1N] [-1N -1N -1N]]"}
;; <=

;; @@
(defn centering [n]
  (mat/sub (mat/identity-matrix n) 
           (matrix (repeat n (repeat n (double (/ 1 n)))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;solitary-moss/centering</span>","value":"#'solitary-moss/centering"}
;; <=

;; **
;;; Compute covariance matrix with centering.
;; **

;; @@
(mat/mmul (matrix data) (centering 3))
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"},{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"},{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"}],"value":"[1.1102230246251565E-16 1.1102230246251565E-16 1.1102230246251565E-16]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"},{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"},{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"}],"value":"[1.1102230246251565E-16 1.1102230246251565E-16 1.1102230246251565E-16]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"},{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"},{"type":"html","content":"<span class='clj-double'>1.1102230246251565E-16</span>","value":"1.1102230246251565E-16"}],"value":"[1.1102230246251565E-16 1.1102230246251565E-16 1.1102230246251565E-16]"}],"value":"[[1.1102230246251565E-16 1.1102230246251565E-16 1.1102230246251565E-16] [1.1102230246251565E-16 1.1102230246251565E-16 1.1102230246251565E-16] [1.1102230246251565E-16 1.1102230246251565E-16 1.1102230246251565E-16]]"}
;; <=

;; **
;;; ## Scattering matrix
;; **

;; **
;;; The centering matrix is idempotent:
;; **

;; @@
(let [c (centering 3)]
  (mat/det (mat/sub (mat/mmul c c) c)))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-double'>-3.4211388289180104E-49</span>","value":"-3.4211388289180104E-49"}
;; <=

;; @@
(defn scattering [data]
  (let [c (centering (count data))]
    (mat/mmul data c (transpose data))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;solitary-moss/scattering</span>","value":"#'solitary-moss/scattering"}
;; <=

;; @@
(scattering data)
;; @@
;; =>
;;; {"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"},{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"},{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"}],"value":"[3.3306690738754696E-16 3.3306690738754696E-16 3.3306690738754696E-16]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"},{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"},{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"}],"value":"[3.3306690738754696E-16 3.3306690738754696E-16 3.3306690738754696E-16]"},{"type":"list-like","open":"<span class='clj-vector'>[</span>","close":"<span class='clj-vector'>]</span>","separator":" ","items":[{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"},{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"},{"type":"html","content":"<span class='clj-double'>3.3306690738754696E-16</span>","value":"3.3306690738754696E-16"}],"value":"[3.3306690738754696E-16 3.3306690738754696E-16 3.3306690738754696E-16]"}],"value":"[[3.3306690738754696E-16 3.3306690738754696E-16 3.3306690738754696E-16] [3.3306690738754696E-16 3.3306690738754696E-16 3.3306690738754696E-16] [3.3306690738754696E-16 3.3306690738754696E-16 3.3306690738754696E-16]]"}
;; <=

;; **
;;; ## Scale independence
;; **

;; **
;;; Pearson correlation:
;; **

;; @@
(defn correlation [x y]
  (double (/ (cov [x y])
             (* (mat/sqrt (cov [x x])) (mat/sqrt (cov [y y]))))))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;solitary-moss/correlation</span>","value":"#'solitary-moss/correlation"}
;; <=

;; @@
(correlation [1 2 3] [1 2 3])
;; @@

;; @@
(cov [[1 2 3] [1 2 3]])
;; @@

;; **
;;; Kendal tau, spearmann correlation coefficient. rank preserving (under monotony)
;; **

;; **
;;; distance correlation, distance variance (possible different dimensions), 0 if independent, 1 if close functional association, no -1
;; **

;; **
;;; annals of applied statistics 2009
;; **

;; @@
(defn distance-correlaton [x y]
  )
;; @@

;; **
;;; summing rvs is convoluted rvs. -> transform to fourier space, fourier transform of pdf is characteristic function
;; **

;; @@

;; @@
