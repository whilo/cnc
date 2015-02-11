(ns cnc.eval-map
  "This namespace relates the data repository
  to this code repository."
  (:require [hasch.core :refer [uuid]]
            [datomic.api :as d]
            [taoensso.timbre :as timber]))

(timber/refer-timbre)

;; code vs. data?

;; scope for eval in this namespace

;; TODO database/value per branch

(defn db-transact [conn txs]
  (debug "TRANSACT DATOMIC:"
         @(d/transact conn (map #(assoc % :db/id (d/tempid :db.part/user))
                                txs)))
  conn)

(def eval-map
  {'(fn create-db [old {:keys [name]}]
      (let [uri (str "datomic:mem:///" name)]
        (d/create-database uri)
        (d/connect uri)))

   (fn [old init]
     (let [uri (str "datomic:mem:///" name)]
       (d/create-database uri)
       (d/connect uri)))

   '(fn transact-schema [conn schema]
      (d/transact conn schema)
      conn)

   (fn [conn schema] ;; HACK to initialize datomic with the final schema
     (d/transact conn (-> "resources/schema.edn" slurp read-string))
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

   '(fn bias-sampling->datoms [conn params]
      (let [id (uuid params)
            {git-id :git-commit-id
             output :output
             {:keys [v-bias h-bias]} :exp-params}  params]
        (db-transact conn [{:val/id (uuid output)
                            :ref/rbm-v-bias (uuid v-bias)
                            :ref/rbm-h-bias (uuid h-bias)
                            :git/commit-id git-id
                            :ref/trans-params id}])
        conn))

   (fn [conn params]
     (let [id (uuid params)
           {git-id :git-commit-id
            output :output
            {:keys [v-bias h-bias]} :exp-params}  params]
       (db-transact conn [{:val/id (uuid output)
                           :ref/rbm-v-bias (uuid v-bias)
                           :ref/rbm-h-bias (uuid h-bias)
                           :git/commit-id git-id
                           :ref/trans-params id}])
       conn))

   '(fn lif-sampling->datoms [conn params]
      (let [id (uuid params)
            {git-id :git-commit-id
             output :output
             {:keys [v-bias h-bias weights]} :exp-params}  params]
        (db-transact conn [{:val/id (uuid output)
                            :ref/rbm-weights (uuid weights)
                            :ref/rbm-v-bias (uuid v-bias)
                            :ref/rbm-h-bias (uuid h-bias)
                            :git/commit-id git-id
                            :ref/trans-params id}])
        conn))

   (fn lif-sampling->datoms [conn params]
     (let [id (uuid params)
           {git-id :git-commit-id
            output :output
            {:keys [v-bias h-bias weights]} :exp-params}  params]
       (db-transact conn [{:val/id (uuid output)
                           :ref/rbm-weights (uuid weights)
                           :ref/rbm-v-bias (uuid v-bias)
                           :ref/rbm-h-bias (uuid h-bias)
                           :git/commit-id git-id
                           :ref/trans-params id}])
       conn))

   '(fn data->datoms [conn params]
      (let [id (uuid params)
            {data :output
             name :name}  params]
        (db-transact conn [{:val/id (uuid data)
                            :data/name name
                            :ref/trans-params id}])
        conn))

   (fn [conn params]
     (let [id (uuid params)
           {data :output
            name :name} params]
       (db-transact conn [{:val/id (uuid data)
                           :data/name name
                           :ref/trans-params id}])
       conn))


   '(fn calibration->datoms [conn params]
      (let [id (uuid params)
            {{{v_rest_min :V_rest_min
               {alpha :alpha
                v_p05 :v_p05} :fit} :calibration} :output
                {neuron-params :neuron-params} :exp-params
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
               v_p05 :v_p05} :fit} :calibration} :output
               {neuron-params :neuron-params} :exp-params
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
                ;; FIX: git-commit-id in params and neuron-parameters is in exp-params
                neuron-params :neuron_parameters
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
               v_p05 :v_p05} :fit} :calibration} :output
               {neuron-params :neuron-params} :exp-params
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

   '(fn add-training-params [conn params]
      (let [namespaced (->> params
                            (map (fn [[k v]]
                                   [(keyword "train" (name k)) v]))
                            (into {}))]
        (db-transact conn [(assoc namespaced :val/id (uuid params))]))
      conn)

   (fn [conn params]
     (let [namespaced (->> params
                           (map (fn [[k v]]
                                  [(keyword "train" (name k))
                                   (cond (= k :weight_recording_interval)
                                         (float v)
                                         (= k :sim_setup_kwargs)
                                         (:rng_seeds_seed v)
                                         :else v)]))
                           (into {}))]
       (db-transact conn [(assoc namespaced :val/id (uuid params))]))
     conn)

   '(fn train-ev-cd->datoms [conn params]
      (let [id (uuid params)
            {{neuron-params :neuron-params
              training-params :training-params
              data-id :data-id} :exp-params
              git-id :git-commit-id} params]
        (db-transact conn [{:val/id (uuid (:output params))
                            :git/commit-id git-id
                            :ref/data data-id
                            :ref/neuron-params (uuid neuron-params)
                            :ref/training-params (uuid training-params)
                            :ref/trans-params id}])
        conn))

   (fn [conn params]
     (let [id (uuid params)
           {{neuron-params :neuron-params
             training-params :training-params
             data-id :data-id} :exp-params
             git-id :git-commit-id} params]
       (db-transact conn [{:val/id (uuid (:output params))
                           :git/commit-id git-id
                           :ref/data data-id
                           :ref/neuron-params (uuid neuron-params)
                           :ref/training-params (uuid training-params)
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
