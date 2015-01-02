(defproject cnc "0.1.0-SNAPSHOT"
  :description "Command and Control center for experiments."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [net.polyc0l0r/geschichte "0.1.0-SNAPSHOT"]
	         [com.datomic/datomic-free "0.9.4899"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [clj-hdf5 "0.2.1"]
                 [cheshire "5.4.0"]]
  :repl-options {:port 52815})
