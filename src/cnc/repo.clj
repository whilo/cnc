(ns cnc.repo
  (:require [hasch.core :refer [uuid]]
            [konserve.store :refer [new-mem-store]]
            [konserve.filestore :refer [new-fs-store]]
            [konserve.protocols :refer [-get-in -assoc-in]]
            [geschichte.sync :refer [server-peer]]
            [geschichte.stage :as s]
            [geschichte.p2p.fetch :refer [fetch]]
            [geschichte.p2p.publish-on-request :refer [publish-on-request]]
            [geschichte.platform :refer [create-http-kit-handler! <!?]]
            [clojure.core.async :refer [<!! >!!]]
            [datomic.api :as d] ;; to read schema file id literals
            ))

(def store (<!? (new-fs-store "repo/store")))


(def peer (server-peer (create-http-kit-handler! "ws://127.0.0.1:31744")
                       store
                       (comp (partial fetch store)
                             (partial publish-on-request store))))

(def stage (<!? (s/create-stage! "whilo@dopamine.kip" peer eval)))

(def repo-id #uuid "aaec3835-9114-4e70-8945-40e78d1cb988")

(<!? (s/subscribe-repos! stage {"whilo@dopamine.kip" {repo-id #{"calibrate"
                                                                 "master"
                                                                 "small-exps"}}}))



(comment
  ;; initialization steps
  (def new-id (<!? (s/create-repo! stage "ev-cd experiments."
                                   :init-fn '(fn init-db [old {:keys [name schema]}]
                                               (let [uri (str "datomic:mem:///" name)]
                                                 (d/create-database uri )
                                                 (let [conn (d/connect uri)]
                                                   (d/transact conn schema)
                                                   conn)))
                                   :init-params {:name "calibration-experiments"
                                                 :schema (read-string (slurp "resources/schema.edn"))}
                                   :branch"calibrate")))


  (<!? (-assoc-in store ["schema"] (read-string (slurp "resources/schema.edn"))))

  (<!? (-get-in store ["whilo@dopamine.kip" repo-id]))

  (<!? (s/branch! stage
                  ["whilo@dopamine.kip" repo-id]
                  "small-exps"
                  (first (get-in @stage ["whilo@dopamine.kip" repo-id :meta :branches "calibrate"]))))

  )
