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

(defn run-experiment! [setup-fn {:keys [args source-path] :as exp-params}]
  (let [date (java.util.Date.)
        base-directory (str "experiments/" date "_" (subs (str (uuid)) 0 8) "/")
        git-id (git-commit source-path)
        _ (info "starting experiment in: " base-directory)
        _ (.mkdir (io/file base-directory))
        _ (spit (str base-directory "exp-params.edn") (merge exp-params
                                                             (when git-id
                                                               {:git-commit-id git-id})))

        _ (setup-fn base-directory exp-params)
        proc (apply sh (concat args [base-directory] [:dir base-directory]))
        _ (when-not (= (:exit proc) 0)
            (throw (ex-info "Process execution failed."
                            {:process proc
                             :exp-params exp-params})))
        _ (info "finished experiment in: " base-directory)]
    {:exp-params exp-params
     :base-directory base-directory
     :date date
     :process proc}))

(defn gather-results! [base-directory blob-names]
  (let [files (doall (map #(->> % (str base-directory) io/file)
                          blob-names))]
    {:exp-params (read-string (slurp (str base-directory "exp-params.edn")))
     :base-directory base-directory
     :output (into {} (map (fn [n b] [(keyword n)
                                     (uuid b)])
                           blob-names files))
     :new-blobs files}))


(comment
  (sh "hostname")

  (def t (Thread. (fn [] (println "alive") (Thread/sleep (* 60 1000)))))
  (.setDaemon t false)
  (.start t))
