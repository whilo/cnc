(defproject cnc "0.1.0-SNAPSHOT"
  :description "Command and Control center for experiments."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;; TODO remove, wrong dependencie order?
                 [clatrix "0.4.0"]
                 [net.mikera/core.matrix "0.32.1"]
	         [com.datomic/datomic-free "0.9.4899"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [net.polyc0l0r/clj-hdf5 "0.2.2-SNAPSHOT"]
                 [cheshire "5.4.0"]
                 [net.polyc0l0r/boltzmann "0.1.1"]
                 [gg4clj "0.1.0"]

                 [net.polyc0l0r/geschichte "0.1.0-SNAPSHOT" :exclusions [org.clojure/clojure] ]]
  :plugins [[lein-gorilla "0.3.4"]]
  :repl-options {:port 52815})
