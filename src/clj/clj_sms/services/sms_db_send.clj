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
    [clj-sms.services.sendapi :as sendapi]
    [promesa.exec :as exec]))

(defrecord Minutely [count])

(defrecord Hourly [count])

(defrecord Daily [count])

(defrecord Block [sts])

(defrecord Record [id value])

(defrecord Result [status msg])

(defrule check-blocked
  [Record (= ?id id)]
  =>
  (log/warn "check-blocked ...... started")
  (if (true? (-> env :sms-check :block-list))
    (let [data (first (db/select models/Block :phone ?id :status 1))]
      (if (empty? data)
        (insert! (->Block false))
        (insert! (->Block true))))
    (insert! (->Block false)))
  (log/warn "check-blocked ...... ended"))

(defrule get-records
  [Record (= ?id id)]
  [Block (= sts false)]
  =>
  (log/warn  "get-records ....... started")
  (let [date (time/local-date)
        data (->> (db/select models/Sms :phone ?id :status 0 :created_at [:> date])
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
  (insert! (->Result false 'minute)))

(defrule check-hourly
  [Hourly (>= count (-> env :sms-check :hour))]
  =>
  (log/warn "check-hourly")
  (insert! (->Result false 'hour)))

(defrule check-daily
  [Daily (>= count (-> env :sms-check :day))]
  =>
  (log/warn "check-daily")
  (insert! (->Result false 'day)))


(defrule update-stores-and-send
  [Record (= ?id id) (= ?value value)]
  [Daily (= ?dcount count)]
  [Hourly (= ?hcount count)]
  [Minutely (= ?mcount count)]
  ; [?errors <- (acc/all) :from [Result (= false status)]]
  [:test (and (< ?dcount (-> env :sms-check :day))
              (< ?hcount (-> env :sms-check :hour))
              (< ?mcount (-> env :sms-check :minute)))]
  =>
  ; send sms
  (if-not (true? (:dev env))
    (sendapi/sendsms {:phone ?id :code ?value}))

  (exec/schedule! 100
    #(db/insert! models/Sms :phone ?id, :sms ?value)))

(defquery query-errors
  [:?status]
  [?errors <- Result (= ?status status)])

(defn run-query [session]
  (query session query-errors :?status false))

(defn run-rules [phone code]
  (-> (mk-session 'clj-sms.services.sms-db-send)
      (insert (->Record phone (if code code (generate-code/generate))))
      (fire-rules)))
