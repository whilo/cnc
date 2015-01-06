(ns cnc.core
  (:gen-class)
  (:require [cognitect.transit :as tr]
            [clojure.core.async :refer [<!! >!!]]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [hasch.core :refer [uuid]]
            [cnc.log :refer [setup-logging!]]
            [cnc.repo :refer [init-repo]]
            [taoensso.timbre :as timber]
            [clojure.tools.nrepl.server :refer [start-server stop-server]]
            ))

(timber/refer-timbre)

(defn -main [& args]
  (let [config (read-string (slurp "resources/config.edn"))
        nrepl-server (start-server :port (:nrepl-port config))
        repo (init-repo config)
        logging (setup-logging!)]
    (defonce state (atom {:config config
                          :nrepl-server nrepl-server
                          :repo repo}))))

(comment
;; nrepl-server
  (stop-server server)

  (-main))



;; initial test: neuron calibration
;; call in random process subfolder
;; input is neuron-params as json
;; call process
;; output is calibration file as json
;; read files into datomic
