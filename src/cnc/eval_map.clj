(ns cnc.eval-map
  "This namespace relates the data repository
  to this code repository."
  (:require [hasch.core :refer [uuid]]
            [datomic.api :as d]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

;; code vs. data?

;; scope for eval in this namespace

(defn db-transact [conn txs]
  (d/transact conn (map #(assoc % :db/id (d/tempid :db.part/user))
                        txs)))

(def eval-map
  {'(fn create-db [old {:keys [name]}]
      (let [uri (str "datomic:mem:///" name)]
        (d/create-database uri)
        (d/connect uri)))

   (fn [old {:keys [name]}]
     (let [uri (str "datomic:mem:///" name)]
       (d/create-database uri)
       (d/connect uri)))

   '(fn transact-schema [conn schema]
      (d/transact conn schema)
      conn)

   (fn [conn schema]
     (d/transact conn schema)
     conn)


   '(fn sampling->datoms [conn params]
      (let [id (uuid params)
            {samples :output
             git-id :git-commit-id
             {:keys [weights v-bias h-bias seed]} :exp-params}  params]
        (db-transact conn [{:val/id (uuid samples)
                            :sampling/count (count samples)
                            :sampling/seed seed
                            :ref/rbm-weights (uuid weights)
                            :ref/rbm-v-bias (uuid v-bias)
                            :ref/rbm-h-bias (uuid h-bias)
                            :git/commit-id git-id
                            :ref/trans-params id}])
        conn))

   (fn [conn params]
     (let [id (uuid params)
           {samples :output
            git-id :git-commit-id
            {:keys [weights v-bias h-bias seed]} :exp-params}  params]
       (db-transact conn [{:val/id (uuid samples)
                           :sampling/count (count samples)
                           :sampling/seed seed
                           :ref/rbm-weights (uuid weights)
                           :ref/rbm-v-bias (uuid v-bias)
                           :ref/rbm-h-bias (uuid h-bias)
                           :git/commit-id git-id
                           :ref/trans-params id}])
       conn))

   '(fn calibration->datoms [conn params]
      (let [id (uuid params)
            {{{v_rest_min :V_rest_min
               {alpha :alpha
                v_p05 :v_p05} :fit} :calibration
                neuron-params :neuron_parameters
                ;; FIX: git-commit-id is in params
                git-id :git-commit-id} :output} params]
        (db-transact conn [{:val/id (uuid (:output params))
                            :git/commit-id git-id
                            :calib/alpha alpha
                            :calib/v-p05 v_p05
                            :ref/neuron-params (uuid (dissoc neuron-params :_type :pynn_model))
                            :ref/trans-params id}])
        conn))

   (fn [conn params]
     (let [id (uuid params)
           {{{v_rest_min :V_rest_min
              {alpha :alpha
               v_p05 :v_p05} :fit} :calibration
               neuron-params :neuron_parameters} :output
               git-id :git-commit-id} params]
       (db-transact conn [{:val/id (uuid (:output params))
                           :git/commit-id git-id
                           :calib/alpha alpha
                           :calib/v-p05 v_p05
                           :ref/neuron-params (uuid (dissoc neuron-params :_type :pynn_model))
                           :ref/trans-params id}])
       conn))

   '(fn calibration->datoms [conn params]
      (let [id (uuid params)
            {{{v_rest_min :V_rest_min
               {alpha :alpha
                v_p05 :v_p05} :fit} :calibration
                neuron-params :neuron_parameters} :output
                git-id :git-commit-id} params]
        (db-transact conn [{:val/id (uuid (:output params))
                            :git/commit-id git-id
                            :calib/alpha alpha
                            :calib/v-p05 v_p05
                            :ref/neuron-params (uuid (dissoc neuron-params :_type :pynn_model))
                            :ref/trans-params id}])
        conn))

   (fn [conn params]
     (let [id (uuid params)
           {{{v_rest_min :V_rest_min
              {alpha :alpha
               v_p05 :v_p05} :fit} :calibration
               neuron-params :neuron_parameters} :output
               git-id :git-commit-id} params]
       (db-transact conn [{:val/id (uuid (:output params))
                           :git/commit-id git-id
                           :calib/alpha alpha
                           :calib/v-p05 v_p05
                           :ref/neuron-params (uuid (dissoc neuron-params :_type :pynn_model))
                           :ref/trans-params id}])
       conn))

   '(fn add-neuron-params [conn params]
      (let [namespaced (->> params
                            (map (fn [[k v]]
                                   [(keyword "neuron" (name k)) v]))
                            (into {}))]
        (db-transact conn [(assoc namespaced :val/id (uuid params))]))
      conn)

   (fn [conn params]
     (let [namespaced (->> params
                           (map (fn [[k v]]
                                  [(keyword "neuron" (name k)) v]))
                           (into {}))]
       (db-transact conn [(assoc namespaced :val/id (uuid params))]))
     conn)

   '(fn train-ev-cd->datoms [conn params]
      (let [id (uuid params)
            {{{v_rest_min :V_rest_min
               {alpha :alpha
                v_p05 :v_p05} :fit} :calibration
                neuron-params :neuron_parameters
                source :source} :output} params]
        (db-transact conn [{:val/id (uuid (:output params))
                            :source/id (uuid source)
                            :calib/alpha alpha
                            :calib/v-p05 v_p05
                            :ref/neuron-params (uuid (dissoc neuron-params :_type :pynn_model))
                            :ref/trans-params id}])
        conn))

   (fn [conn params]
     (let [id (uuid params)
           {{{v_rest_min :V_rest_min
              {alpha :alpha
               v_p05 :v_p05} :fit} :calibration
               neuron-params :neuron_parameters
               source :source} :output} params]
       (db-transact conn [{:val/id (uuid (:output params))
                           :source/id (uuid source)
                           :calib/alpha alpha
                           :calib/v-p05 v_p05
                           :ref/neuron-params (uuid (dissoc neuron-params :_type :pynn_model))
                           :ref/trans-params id}])
       conn))})

(defn mapped-eval [code]
  (if (eval-map code)
    (eval-map code)
    (do (debug "eval-map didn't match:" code)
        (eval code))))


(defn find-fn [name]
  (first (filter (fn [[_ fn-name]]
                   (= name fn-name))
                 (keys eval-map))))


(comment
  (find-fn 'sampling->datoms))