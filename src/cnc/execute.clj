(ns cnc.execute
  (:require [hasch.core :refer [uuid]]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [cheshire.core :as json]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn write-json [base-dir name coll]
  (with-open [w (io/writer (str base-dir name))]
    (json/generate-stream coll w)))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy (clojure.java.io/input-stream x) out)
    (.toByteArray out)))

(defn git-commit [file-name]
  (let [f (io/file file-name)
        dir (if (.isDirectory f)
              file-name
              (.getParent f))]
    (->> (sh "git" "show" :dir dir)
         :out
         (re-find #"commit (\S+)")
         second)))

(defn run-experiment! [pre-proc-fn post-proc-fn {:keys [args source-path] :as exp-params}]
  (let [base-directory (str "experiments/" (java.util.Date.) "_" (subs (str (uuid)) 0 8) "/")
        git-id (git-commit source-path)
        _ (debug "starting experiment in: " base-directory)
        _ (.mkdir (io/file base-directory))
        _ (pre-proc-fn base-directory exp-params)
        proc (apply sh (concat args [base-directory] [:dir base-directory]))
        _ (when-not (= (:exit proc) 0)
            (throw (ex-info "Process execution failed."
                            {:process proc
                             :exp-params exp-params})))
        output (post-proc-fn base-directory exp-params)
        _ (debug "finished experiment in: " base-directory)]
    (merge output
           {:exp-params exp-params
            :base-directory base-directory
            :process proc}
           (when git-id
             {:git-commit-id git-id}))))


(comment
  (sh "hostname")


  (def t (Thread. (fn [] (println "alive") (Thread/sleep (* 60 1000)))))
  (.setDaemon t false)
  (.start t))
