(defproject cnc "0.1.0-SNAPSHOT"
  :description "Command and Control center for experiments."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;; TODO remove, wrong dependency order?
                 [clatrix "0.4.0"]
                 [net.mikera/core.matrix "0.32.1"]
 	         [com.datomic/datomic-free "0.9.4899"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [net.polyc0l0r/clj-hdf5 "0.2.2-SNAPSHOT"]
                 [cheshire "5.4.0"]
                 [net.polyc0l0r/boltzmann "0.1.1"]
                 [com.taoensso/nippy "2.7.1"]
                 [com.taoensso/timbre "3.3.1"]
                 [gorilla-repl "0.3.4" :exclusions [http-kit]]
                 [gg4clj "0.1.0"]

                 [net.polyc0l0r/geschichte "0.1.0-SNAPSHOT"]]
  :jvm-opts ["-Xdebug" "-Xrunjdwp:transport=dt_socket,server=y,suspend=n"]
  :main cnc.core)
