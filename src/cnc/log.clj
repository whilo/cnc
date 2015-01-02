(ns cnc.log
  (:require [taoensso.nippy :as nippy]
            [taoensso.timbre :as timbre]
            [konserve.protocols :refer [-bget -bassoc]]
            [konserve.filestore :refer [new-fs-store]]
            [clojure.core.async :refer [<!! >!!]])
  (:import [java.io DataOutputStream ByteArrayOutputStream]))

(defn setup-logging! []
  (def log-store (<!! (new-fs-store (str "logs/" (java.util.Date.)))))

  (def log-counter (atom 0))


  (timbre/set-config! [:appenders :fs-store] {:doc "Simple file appender."
                                              :min-level nil :enabled? true
                                              :fn (fn [{:keys [ap-config] :as args}]
                                                    (try
                                                      (let [baos (ByteArrayOutputStream.)
                                                            dos (DataOutputStream. baos)]
                                                        (nippy/freeze-to-stream! dos args)
                                                        (<!! (-bassoc log-store (swap! log-counter inc) (.toByteArray baos))))
                                                      (catch Exception e
                                                        (.printStackTrace e))))}))


(comment
  (:folder log-store)
  (<!! (-bget log-store 161 (comp nippy/thaw-from-stream! :input-stream))))
