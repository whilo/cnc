(ns cnc.analytics
  (:require [cnc.eval-map :refer [eval-map mapped-eval]]
            [hasch.core :refer [uuid]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [konserve.protocols :refer [-get-in]]
            [datomic.api :as d]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)


(comment
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in]]
           '[geschichte.platform :refer [<!?]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))


  (defn load-key [id]
    (<!? (-get-in store [id])))

  (def conn (<!? (s/branch-value store mapped-eval
                                 (get-in @stage ["weilbach@dopamine.kip" repo-id]) "train small rbms")))

  (clojure.pprint/pprint (seq (d/datoms (d/db conn) :eavt)))



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
         [?e :neuron/tau_m ?tau-m]]
       (d/db conn))

  (d/delete-database "datomic:mem:///ev-experiments")

  (clojure.pprint/pprint
   (<!? (s/commit-history-values store
                                 (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :causal-order])
                                 (first (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "train small rbms"]))
                                 )))

  (get-in @stage ["weilbach@dopamine.kip" repo-id :meta])

  (<!? (s/pull! stage ["weilbach@dopamine.kip" repo-id "calibrate"]
                "train small rbms" :allow-induced-conflict? true))

  (<!? (s/merge! stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                 (seq (get-in @stage ["weilbach@dopamine.kip" repo-id :meta :branches "train small rbms"]))))
  )
