(ns cnc.repo
  (:require [cnc.eval-map :refer [find-fn]]
            [hasch.core :refer [uuid]]
            [konserve.store :refer [new-mem-store]]
            [konserve.filestore :refer [new-fs-store]]
            [konserve.protocols :refer [-get-in -assoc-in]]
            [geschichte.sync :refer [server-peer]]
            [geschichte.stage :as s]
            [geschichte.p2p.fetch :refer [fetch]]
            [geschichte.p2p.hash :refer [ensure-hash]]
            [geschichte.p2p.publish-on-request :refer [publish-on-request]]
            [geschichte.platform :refer [create-http-kit-handler! <!? start]]
            [clojure.core.async :refer [>!!]]
            [datomic.api :as d] ;; to read schema file id literals
            ))

(def config (atom (read-string (slurp "resources/config.edn"))))

(def store (<!? (new-fs-store (:store @config))))

(def peer (server-peer (create-http-kit-handler! (:peer @config))
                       store
                       (comp (partial fetch store)
                             ensure-hash
                             (partial publish-on-request store))))

(def stage (<!? (s/create-stage! (:user @config) peer eval)))

(def repo-id (:repo @config))

(<!? (s/subscribe-repos! stage (:subs @config)))

(start peer)



(comment

  (<!? (s/connect! stage "ws://127.0.0.1:31744"))
  ;; initialization steps
  (def new-id (<!? (s/create-repo! stage "ev-cd experiments.")))


  (<!? (s/transact stage ["weilbach@dopamine.kip" new-id "master"]
                   [[(find-fn 'create-db)
                     {:name "ev-experiments"}]
                    [(find-fn 'transact-schema)
                     (-> "resources/schema.edn"
                         slurp
                         read-string)]]))

  (<!? (s/commit! stage {"weilbach@dopamine.kip" {new-id #{"master"}}}))





  (<!? (s/branch! stage
                  ["weilbach@dopamine.kip" repo-id]
                  "calibrate"
                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "master"]))))





  (<!? (-assoc-in store ["schema"] (read-string (slurp "resources/schema.edn"))))

  (<!? (-get-in store ["weilbach@dopamine.kip" repo-id]))
  )
