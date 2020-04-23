(ns clj-sms.services.sms-db-send
  (:require
    [clara.rules :refer [defrule defquery mk-session query insert insert! fire-rules]]
    [clara.rules.accumulators :as acc]
    [toucan.db :as db]
    [honeysql.core :as sql]
    [clj-sms.db.models :as models]
    [java-time :as time]
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]
    [clj-sms.services.generate-code :as generate-code]
    [clj-sms.services.query :as rule-query]))

(defrecord Minutely [count])

(defrecord Hourly [count])

(defrecord Daily [count])

(defrecord Record [id value])

(defrule get-records
  [Record (= ?id id)]
  =>
  (log/warn  "get-records ....... started")
  (let [date (time/local-date)
        data (->> (db/select models/Sms :phone ?id :created_at [:> date])
                  (map #(assoc % :created_at (time/local-date-time (:created_at %)))))

        mtime (time/minus (time/local-date-time) (time/minutes (-> env :sms-check :minute-scale)))
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
  [Minutely (>= count (-> env :sms-check :minute))]
  =>
  (log/warn "check-minutely")
  (insert! (rule-query/->Result false 'minute)))

(defrule check-hourly
  [Hourly (>= count (-> env :sms-check :hour))]
  =>
  (log/warn "check-hourly")
  (insert! (rule-query/->Result false 'hour)))

(defrule check-daily
  [Daily (>= count (-> env :sms-check :day))]
  =>
  (log/warn "check-daily")
  (insert! (rule-query/->Result false 'day)))

(defrule update-stores
  [Record (= ?id id) (= ?value value)]
  [Daily (= ?dcount count)]
  [Hourly (= ?hcount count)]
  [Minutely (= ?mcount count)]
  ; [?errors <- (acc/all) :from [Result (= false status)]]
  [:test (and (< ?dcount (-> env :sms-check :day))
              (< ?hcount (-> env :sms-check :hour))
              (< ?mcount (-> env :sms-check :minute)))]
  =>
  (db/insert! models/Sms :phone ?id, :sms ?value))

(defn run-rules [phone]
  (-> (mk-session 'clj-sms.services.sms-db-send)
      (insert (->Record phone (generate-code/generate)))
      (fire-rules)))
