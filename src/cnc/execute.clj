(ns cnc.execute
  (:require [hasch.core :refer [uuid]]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn run-experiment! [pre-proc-fn post-proc-fn {:keys [args] :as exp-params}]
  (let [base-directory (str "experiments/" (uuid) "/")
        _ (debug "starting experiment in: " base-directory)
        _ (.mkdir (io/file base-directory))
        _ (pre-proc-fn base-directory exp-params)
        proc (apply sh (concat args [base-directory] [:dir base-directory]))
        output (post-proc-fn base-directory exp-params)]
    {:exp-params exp-params
     :base-directory base-directory
     :process proc
     :output output}))
