(defproject cnc "0.1.0-SNAPSHOT"
  :description "Command and Control center for experiments."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 ;; TODO remove, wrong dependency order?
                 [clatrix "0.4.0"]
                 [net.mikera/core.matrix "0.32.1"]
 	         [com.datomic/datomic-free "0.9.5130"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [net.polyc0l0r/clj-hdf5 "0.2.2-SNAPSHOT"]
                 [cheshire "5.4.0"]
                 [net.polyc0l0r/boltzmann "0.1.2-SNAPSHOT"]
                 [com.taoensso/nippy "2.7.1"]
                 [com.taoensso/timbre "3.3.1"]
                 [gorilla-repl "0.3.4" :exclusions [http-kit]]
                 [es.topiq/rincanter "1.0.0-SNAPSHOT"]
                 [gg4clj "0.1.0"]
                 [quil "2.2.5"]

                 [io.replikativ/replikativ "0.1.0-SNAPSHOT"]]
  :jvm-opts ["-Xmx2g"]
  :main cnc.core)
