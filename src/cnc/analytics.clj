(ns cnc.analytics
  (:require [clj-hdf5.core :as hdf5]
            [clojure.core.matrix :as mat]
            [clojure.set :as set]
            [cnc.eval-map :refer [eval-map mapped-eval]]
            [datomic.api :as d]
            [geschichte.platform :refer [<!?]]
            [geschichte.stage :as s]
            [hasch.core :refer [uuid]]
            [konserve.protocols :refer [-get-in -bget]]
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

  (aprint.core/aprint s/commit-value-cache)


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

  (d/q '[:find ?e (distinct ?vid)
         :where
         [?e :ref/trans-params ?vid]]
       (d/db conn))

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


  (->> (d/q '[:find ?tid
              :where
              [?e :ref/data #uuid "19a5da17-6b4c-548c-8b6f-24faf06c089c"]
              [?e :ref/trans-params ?ti]]
            (d/db conn))
       (clojure.pprint/pprint))


  (load-key #uuid "19a5da17-6b4c-548c-8b6f-24faf06c089c")

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
                                 (get-in @stage ["weilbach@dopamine.kip" repo-id :state :causal-order])
                                 (first (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches "train small rbms"]))
                                 )))

  (get-in @stage [:config :subs])
  (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches])
  (get-in @(get-in @stage [:volatile :peer]) [:meta-sub])
  (<!? (s/checkout! stage ["weilbach@dopamine.kip" repo-id] "sample"))

  (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches])
  (<!? (s/subscribe-repos! stage (update-in (get-in @stage [:config :subs]) ["weilbach@dopamine.kip" repo-id] conj "sample")))
  (<!? (s/pull! stage ["weilbach@dopamine.kip" repo-id "calibrate"]
                "train small rbms" :allow-induced-conflict? false))

  (aprint.core/aprint (get-in @stage ["weilbach@dopamine.kip" repo-id]))
  (seq (get-in @stage ["weilbach@dopamine.kip" repo-id]))
  (def heads '(#uuid "0870de8f-2424-5b47-ae03-00567593483e" #uuid "0cf765eb-a646-53ae-9d99-5ce903af3dd7"))


  (<!? (s/merge! stage ["weilbach@dopamine.kip" repo-id "train small rbms"]
                 heads))

  (require '[clj-hdf5.core :as hdf5])
  (def db (hdf5/open (<!? (-bget store #uuid "0ce9da38-9a10-51eb-ae96-4768b2fa78d6" :file))))

  (def weights-history (hdf5/read  (hdf5/get-dataset db "/weights_history")))
  (count weights-history)
  (spit "/tmp/weights_history.json" (vec (get weights-history 110010)))

  )
