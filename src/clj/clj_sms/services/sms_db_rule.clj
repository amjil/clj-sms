(ns clj-sms.services.sms-db-rule
  (:require
    [clara.rules :refer [defrule mk-session insert insert! fire-rules]]
    [clara.rules.accumulators :as acc]
    [toucan.db :as db]
    [honeysql.core :as sql]
    [clj-sms.db.models :as models]
    [java-time :as time]
    [clojure.tools.logging :as log]))

(defrecord Minutely [count])

(defrecord Hourly [count])

(defrecord Daily [count])

(defrecord Result [status msg])

(defrecord Record [id value])

(defrule get-records
  [Record (= ?id id)]
  =>
  (log/warn  "get-records ....... started")
  (let [date (time/local-date)
        data (->> (db/select models/Sms :phone ?id :created_at [:> date])
                  (map #(assoc % :created_at (time/local-date-time (:created_at %)))))

        mtime (time/minus (time/local-date-time) (time/minutes 2))
        mdata (filter #(time/before? mtime (:created_at %)) data)

        htime (time/minus (time/local-date-time) (time/hours 1))
        hdata (filter #(time/before? htime (:created_at %)) data)]

    (insert! (->Hourly (count hdata)))
    (insert! (->Minutely (count mdata)))
    (insert! (->Daily (count data)))

    (log/warn "date count = " (count data))
    (log/warn "mdate count = " (count mdata))
    (log/warn "hdate count = " (count hdata)))
  (log/warn  "get-records ....... ended"))

(defrule check-minutely
  [Minutely (>= count 1)]
  =>
  (prn "22222")
  (insert! (->Result false 'minute)))

(defrule check-hourly
  [Hourly (>= count 5)]
  =>
  (prn "3333")
  (insert! (->Result false 'hour)))

(defrule check-daily
  [Daily (>= count 10)]
  =>
  (prn "4444")
  (insert! (->Result false 'day)))

(defrule update-stores
  [Record (= ?id id) (= ?value value)]
  [Daily (= ?dcount count)]
  [Hourly (= ?hcount count)]
  [Minutely (= ?mcount count)]
  [?errors <- (acc/all) :from [Result (= false status)]]
  [:test (and (< ?dcount 10) (< ?hcount 5) (< ?mcount 1))]
  =>
  (prn ?id)
  (prn ?errors)
  (prn "xxxxxxx")
  (db/insert! models/Sms :phone ?id, :sms ?value))

(defn run-rules []
  (-> (mk-session 'clj-sms.services.sms-db-rule)
      (insert (->Record "15248141905" "123456"))
      (fire-rules)))
