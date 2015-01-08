(ns cnc.core
  (:gen-class)
  (:require [clojure.core.async :refer [<!! >!!]]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clj-hdf5.core :as hdf5]
            [cheshire.core :as json]
            [cnc.log :refer [setup-logging!]]
            [cnc.repo :refer [init-repo]]
            [taoensso.timbre :as timber]
            [gorilla-repl.core :as g]))

(timber/refer-timbre)

(defn -main [& args]
  (let [config (read-string (slurp "resources/config.edn"))
        grepl-server (g/run-gorilla-server (:gorilla config))
;        nrepl-server (start-server :port (:nrepl-port config))
        repo (init-repo config)
        logging (setup-logging!)]
    (defonce state (atom {:config config
                          :grepl-server grepl-server
                          :repo repo
                          :logging logging}))))

(comment
;; nrepl-server
  (stop-server server)

  (-main)

  (require '[clojure.tools.nrepl.server :refer [start-server stop-server]])

)



;; initial test: neuron calibration
;; call in random process subfolder
;; input is neuron-params as json
;; call process
;; output is calibration file as json
;; read files into datomic
