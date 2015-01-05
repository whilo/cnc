(ns cnc.analytics
  (:require [cnc.repo :refer [store stage repo-id]]
            [hasch.core :refer [uuid]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [datomic.api :as d]))

(defn db-transact [conn txs]
  (d/transact conn (map #(assoc % :db/id (d/tempid :db.part/user))
                        txs)))

(def conn (<!? (s/branch-value store eval (get-in @stage ["whilo@dopamine.kip" repo-id]) "calibrate")))

(require '[konserve.protocols :refer [-get-in]])

(defn load-key [id]
  (<!? (-get-in store [id])))

(->> (d/q '[:find ?cm
            :where
            [?e :ref/trans-params ?tid]
            [(cnc.analytics/load-key ?tid) ?np]
            [(get-in ?np [:output]) ?cm]]
          (d/db conn))
     (map (comp println first)))

(->> (d/q '[:find ?e
            :where
            [?e :val/id ?vid]]
          (d/db conn))
     (map first)
     (map #(d/entity (d/db conn) %))
     (map #(into {} %)))

(d/q '[:find ?val-id
       :where
       [?e :val/id ?val-id]]
     (d/db conn))

(d/q '[:find ?tau-m
       :where
       [?e :tau_m ?tau-m]]
     (d/db conn))

(comment
  (def schema (read-string (slurp "resources/schema.edn")))

  (d/delete-database "datomic:mem:///calibrationl-experiments")

  (clojure.pprint/pprint
   (<!? (s/commit-history-values store
                                 (get-in @stage ["whilo@dopamine.kip" repo-id :meta :causal-order])
                                 (first (get-in @stage ["whilo@dopamine.kip" repo-id :meta :branches "calibrate"]))
                                 )))
  )
