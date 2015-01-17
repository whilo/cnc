(ns cnc.analytics
  (:require [cnc.eval-map :refer [eval-map mapped-eval]]
            [clj-hdf5.core :as hdf5]
            [hasch.core :refer [uuid]]
            [geschichte.stage :as s]
            [geschichte.platform :refer [<!?]]
            [konserve.protocols :refer [-get-in -bget]]
            [datomic.api :as d]
            [clojure.core.matrix :as mat]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn get-hdf5-tensor [store id path]
  (let [db (hdf5/open (<!? (-bget store id :file)))
        m (mat/matrix (hdf5/read  (hdf5/get-dataset db path)))
        _ (hdf5/close db)]
    m))

(defn sample-freqs [samples]
  (let [c (count samples)]
    (->> (frequencies samples)
         (map (fn [[k v]] [k (float (/ v c))]))
         (into {}))))


(comment
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in -bget]]
           '[geschichte.platform :refer [<!?]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id]))


  (defn load-key [id]
    (<!? (-get-in store [id])))

  (def conn (<!? (s/branch-value store mapped-eval
                                 (get-in @stage ["weilbach@dopamine.kip" repo-id])
                                 "train small rbms")))

  (clojure.pprint/pprint (seq (d/datoms (d/db conn) :eavt)))
  (clojure.pprint/pprint conn)



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
       (map #(into {} %))
       clojure.pprint/pprint)

  (->> (d/q '[:find ?tp-id
              :where
              [?spls :sampling/count ?c]
              [?spls :ref/trans-params ?tp-id]]
            (d/db conn))
       ffirst)

  (->> (d/q '[:find ?epochs
              :where
              [?e :ref/trans-params ?t-id]
              [(cnc.analytics/load-key ?t-id) ?t]
              [(get-in ?t [:exp-params :training-params :epochs]) ?epochs]]
            (d/db conn))
       (clojure.pprint/pprint))


  (->> (d/q '[:find ?e ?v
              :where
              #_[?e :ref/data #uuid "25d4dfa7-3c11-559a-9aa3-618a5258a911"]
              [?e :train/h_count 5]
              [?e :val/id ?v]
              ]
            (d/db conn))
       (clojure.pprint/pprint))

  (load-key #uuid "057c440b-9415-5e4e-bd4f-ffab8ec7ff9c")

  (d/q '[:find ?d-id
         :where
         [?e :ref/data ?d-id]
         ]
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

  (require '[clj-hdf5.core :as hdf5])
  (def db (hdf5/open (<!? (-bget store #uuid "0ce9da38-9a10-51eb-ae96-4768b2fa78d6" :file))))

  (def weights-history (hdf5/read  (hdf5/get-dataset db "/weights_history")))
  (count weights-history)
  (spit "/tmp/weights_history.json" (vec (get weights-history 110010)))

  )
