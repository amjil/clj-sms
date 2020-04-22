(ns clj-sms.services.core
  (:require
    [clara.rules :refer [defrule mk-session insert insert! fire-rules]]
    [clara.rules.accumulators :as acc]
    [clj-sms.util.redis :as redis]
    [clj-sms.db.core :refer [rdb]]))

(defrecord Minutely [count])

(defrecord Hourly [count])

(defrecord Daily [count])

(defrecord Result [status msg])

(defrecord Phone [id])

(defrule get-redis-values
  [Phone (= ?id id)]
  =>
  (prn "11111")
  (insert! (->Minutely (if-let [count (redis/get rdb ?id)]
                         (read-string count)
                         0)))
  (insert! (->Hourly (if-let [count (redis/get rdb (str ?id "h"))]
                       (read-string count)
                       0)))
  (insert! (->Daily (if-let [count (redis/get rdb (str ?id "d"))]
                      (read-string count)
                      0))))

(defrule check-minutely
  [Minutely (>= count 2)]
  =>
  (prn "22222")
  (insert! (->Result false 'minute)))

(defrule check-hourly
  [Hourly (>= count 3)]
  =>
  (prn "3333")
  (insert! (->Result false 'hour)))

(defrule check-daily
  [Daily (>= count 5)]
  =>
  (prn "4444")
  (insert! (->Result false 'day)))

(defrule update-stores
  [Phone (= ?id id)]
  [?errors <- (acc/all) :from [Result (= false status)]]
  [:test (< (count ?errors) 0)]
  =>
  (prn "5555")
  (prn ?errors)
  (redis/incr rdb ?id)
  (redis/expire rdb ?id 120)

  (redis/incr rdb (str ?id "h"))
  (redis/expire rdb (str ?id "h") (* 60 60))

  (redis/incr rdb (str ?id "d"))
  (redis/expire rdb (str ?id "d") (* 60 60 24)))

(defn run-rules []
  (-> (mk-session 'clj-sms.services.core)
      (insert (->Phone "15248141905"))
      (fire-rules)))
