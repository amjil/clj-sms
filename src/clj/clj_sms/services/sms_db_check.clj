(ns clj-sms.services.sms-db-check
  (:require
    [clara.rules :refer [defrule defquery mk-session query insert insert! fire-rules]]
    [clara.rules.accumulators :as acc]
    [toucan.db :as db]
    [honeysql.core :as sql]
    [clj-sms.db.models :as models]
    [java-time :as time]
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]
    [promesa.exec :as exec]))

(defrecord Record [id value])

(defrecord Sms [id value])

(defrecord Result [status msg])

(defrule get-record
  [Record (= ?id id)]
  =>
  (let [date (time/minus (time/local-date-time) (time/seconds (-> env :sms-check :valid-time)))
        data (first (db/select models/Sms :phone ?id, :status 0, :created_at [:> date], {:limit 1 :order-by [[:created_at :desc]]}))]
    (if (empty? data)
      (insert! (->Result false 'no_record))
      (insert! (->Sms (-> data :id) (-> data :sms))))))

(defrule check
  [Record (= ?user-value value)]
  [Sms (= ?value value) (= ?id id)]
  ; [:test (not= ?value ?user-value)]
  =>
  (if (not= ?value ?user-value)
    (insert! (->Result false 'not_march))
    (exec/schedule! 100
      #(db/update! models/Sms ?id :status 1))))

(defquery query-errors
  [:?status]
  [?errors <- Result (= ?status status)])

(defn run-query [session]
  (query session query-errors :?status false))

(defn run-rules [phone sms]
  (-> (mk-session 'clj-sms.services.sms-db-check)
      (insert (->Record phone sms))
      (fire-rules)))
