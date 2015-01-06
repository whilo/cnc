(ns cnc.execute
  (:require [hasch.core :refer [uuid]]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

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
  (let [base-directory (str "experiments/" (uuid) "/")
        git-id (git-commit source-path)
        _ (debug "starting experiment in: " base-directory)
        _ (.mkdir (io/file base-directory))
        _ (pre-proc-fn base-directory exp-params)
        proc (apply sh (concat args [base-directory] [:dir base-directory]))
        output (post-proc-fn base-directory exp-params)]
    (merge {:exp-params exp-params
            :base-directory base-directory
            :process proc
            :output output}
           (when git-id
             {:git-commit-id git-id}))))


(comment
  (sh "hostname"))
