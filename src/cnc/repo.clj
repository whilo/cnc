(ns cnc.repo
  (:require [cnc.eval-map :refer [find-fn]]
            [hasch.core :refer [uuid]]
            [konserve.store :refer [new-mem-store]]
            [konserve.filestore :refer [new-fs-store]]
            [konserve.protocols :refer [-get-in -assoc-in]]
            [geschichte.sync :refer [server-peer client-peer]]
            [geschichte.stage :as s]
            [geschichte.p2p.fetch :refer [fetch]]
            [geschichte.p2p.hash :refer [ensure-hash]]
            [geschichte.p2p.publish-on-request :refer [publish-on-request]]
            [geschichte.p2p.block-detector :refer [block-detector]]
            [geschichte.platform :refer [create-http-kit-handler! <!? start]]
            [clojure.core.async :refer [>!!]]
            [datomic.api :as d] ;; to read schema file id literals
            ))


(defn init-repo [config]
  (let [{:keys [user repo branches store remote peer]} config
        store (<!? (new-fs-store store))
        peer-server (server-peer (create-http-kit-handler! peer) ;; TODO client-peer?
                                 store
                                 (comp (partial block-detector :peer-core)
                                       (partial fetch store)
                                       ensure-hash
                                       (partial publish-on-request store)
                                       (partial block-detector :p2p-surface)))
        #_(client-peer "benjamin"
                       store
                       (comp (partial fetch store)
                             ensure-hash
                             (partial publish-on-request store)))
        stage (<!? (s/create-stage! user peer-server eval))
        res {:store store
             :peer peer-server
             :stage stage
             :id repo}]

    (when-not (= peer :client)
      (start peer-server))

    (when remote
      (<!? (s/connect! stage remote)))

    (when repo
      (<!? (s/subscribe-repos! stage {user {repo branches}})))
    res))


(comment
  (do
    (require '[cnc.core :refer [state]])
    (def stage (get-in @state [:repo :stage]))
    (def store (get-in @state [:repo :store]))
    (def repo-id (get-in @state [:repo :id])))
  (clojure.pprint/pprint @stage)

  (<!? (s/connect! stage "ws://127.0.0.1:31744"))
  ;; initialization steps
  (def new-id (<!? (s/create-repo! stage "ev-cd experiments.")))


  (clojure.pprint/pprint (<!? (-get-in store [#uuid "214e7e59-8ba0-543a-9ea3-7076bb1f518b"])))
  (clojure.pprint/pprint (<!? (-get-in store [#uuid "059280a0-3682-592b-bac4-99ba12795972"])))

  (<!? (s/transact stage ["weilbach@dopamine.kip" repo-id "master"]
                   [[(find-fn 'create-db)
                     {:name "ev-experiments"}]
                    [(find-fn 'transact-schema)
                     (-> "resources/schema.edn"
                         slurp
                         read-string)]]))

  (<!? (s/commit! stage {"weilbach@dopamine.kip" {repo-id #{"master"}}}))



  (<!? (s/branch! stage
                  ["weilbach@dopamine.kip" repo-id]
                  "train current rbms"
                  (first (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches "master"]))))





  (<!? (-assoc-in store ["schema"] (read-string (slurp "resources/schema.edn"))))

  (<!? (-get-in store ["weilbach@dopamine.kip" repo-id :branches])))
