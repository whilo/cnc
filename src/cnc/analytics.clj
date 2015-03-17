(ns cnc.analytics
  (:require [clj-hdf5.core :as hdf5]
            [clojure.core.matrix :as mat]
            [clojure.set :as set]
            [cnc.eval-map :refer [eval-map mapped-eval]]
            [datomic.api :as d]
            [geschichte.platform :refer [<!?]]
            [geschichte.realize :refer [branch-value]]
            [hasch.core :refer [uuid]]
            [konserve.protocols :refer [-get-in -bget]]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

(defn get-hdf5-tensor [store id path]
  (let [db (hdf5/open (<!? (-bget store id :file)))
        m (hdf5/read (hdf5/get-dataset db path))
        _ (hdf5/close db)]
    m))

(defn sample-freqs [samples]
  (let [c (count samples)]
    (->> (frequencies samples)
         (map (fn [[k v]] [k (float (/ v c))]))
         (into {}))))

(defn drop-half [elems]
  (let [c (count elems)]
    (when-not (= (mod c 2) 0)
      (throw (ex-info "Odd number of elements.")))
    (drop (/ c 2) elems)))


(do
  (require '[cnc.core :refer [state]]
           '[konserve.protocols :refer [-get-in -bget -exists?]]
           '[geschichte.platform :refer [<!?]])
  (def stage (get-in @state [:repo :stage]))
  (def store (get-in @state [:repo :store]))
  (def repo-id (get-in @state [:repo :id])))

(defn conn [branch]
  (<!? (branch-value store mapped-eval
                     (get-in @stage ["weilbach@dopamine.kip" repo-id])
                     branch)))

(defn load-key [& path]
    (<!? (-get-in store path)))

(comment


  (aprint.core/aprint s/commit-value-cache)




  ((load-key #uuid "37107994-69aa-5a8f-9fd9-5616298b993b") :exp-params)

  {:weights [[2.0 -2.0]
             [-2.0 2.0]],
   :v-bias [0.0 0.0],
   :h-bias [0.8 0.2]}




  (clojure.pprint/pprint (seq (d/datoms (d/db conn) :eavt)))
  (clojure.pprint/pprint conn)

  (<!? (-exists? store  #uuid "2976ffdf-5cf4-5188-9dfa-ab1e89752363"))

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

  (->> (d/q '[:find ?tp-id ?c
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
         [?e :ref/data ?d-id]]
       (d/db conn))

  [#uuid "3197da4c-3806-544f-a62b-2f48383691d4"] [#uuid "37107994-69aa-5a8f-9fd9-5616298b993b"]

  (-> #uuid "37107994-69aa-5a8f-9fd9-5616298b993b" load-key :output first)

  (d/q '[:find ?tau-m
         :where
         [?e :neuron/tau_m ?tau-m]]
       (d/db conn))

  (d/delete-database "datomic:mem:///ev-experiments")

  (clojure.pprint/pprint
   (<!? (s/commit-history-values store
                                 (get-in @stage ["weilbach@dopamine.kip" repo-id :state :causal-order])
                                 (first (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches "master"]))
                                 )))

  (get-in @stage [:config :subs])
  (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches])
  (get-in @(get-in @stage [:volatile :peer]) [:meta-sub])
  (<!? (s/checkout! stage ["weilbach@dopamine.kip" repo-id] "sample"))
  (update-in (get-in @stage [:config :subs]) ["weilbach@dopamine.kip" repo-id] conj "sample")
  (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches])
  (<!? (s/subscribe-repos! stage (update-in (get-in @stage [:config :subs]) ["weilbach@dopamine.kip" repo-id] conj "sample")))
  (<!? (s/pull! stage ["weilbach@dopamine.kip" repo-id "sample"]
                "train current rbms" :allow-induced-conflict? true))

  (aprint.core/aprint (get-in @stage ["weilbach@dopamine.kip" repo-id]))
  (seq (get-in @stage ["weilbach@dopamine.kip" repo-id :state :branches "train current rbms"]))

  (def heads '(#uuid "323847ce-67af-5738-b8dc-881d558b076f" #uuid "2dc6068d-4a1c-5911-b1cc-cff5b3d17618"))


  (<!? (s/merge! stage ["weilbach@dopamine.kip" repo-id "train current rbms"]
                 heads))

  (require '[clj-hdf5.core :as hdf5])
  (def db (hdf5/open (<!? (-bget store #uuid "0ce9da38-9a10-51eb-ae96-4768b2fa78d6" :file))))

  (def weights-history (hdf5/read  (hdf5/get-dataset db "/weights_history")))
  (count weights-history)
  (spit "/tmp/weights_history.json" (vec (get weights-history 110010))))
