(ns cnc.log
  (:require [taoensso.nippy :as nippy]
            [taoensso.timbre :as timbre]
            [konserve.protocols :refer [-bget -bassoc]]
            [konserve.filestore :refer [new-fs-store]]
            [geschichte.platform :refer [<!?]])
  (:import [java.io DataOutputStream ByteArrayOutputStream]))

(defn setup-logging! []
  (def log-store (<!? (new-fs-store (str "logs/" (java.util.Date.)))))

  (def log-counter (atom 0))

  (timbre/set-config! [:appenders :standard-out :min-level] :debug)

  (timbre/set-config! [:appenders :fs-store] {:doc "Simple file appender."
                                              :min-level nil :enabled? true
                                              :fn (fn [{:keys [ap-config] :as args}]
                                                    (with-open [baos (ByteArrayOutputStream.)
                                                                dos (DataOutputStream. baos)]
                                                      (try
                                                        (nippy/freeze-to-stream! dos (map (fn [a] (if (nippy/freezable? a) a (str a)))
                                                                                          args))
                                                        (<!? (-bassoc log-store (swap! log-counter inc) (.toByteArray baos)))
                                                        (catch Exception e
                                                          (.printStacktrace e)))))}))





(comment
  (:folder log-store)
  (<!! (-bget log-store 161 (comp nippy/thaw-from-stream! :input-stream))))
