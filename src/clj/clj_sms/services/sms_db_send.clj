(ns clj-sms.services.sms-db-send
  (:refer-clojure :exclude [send])
  (:require
    [toucan.db :as db]
    [honeysql.core :as sql]
    [clj-sms.db.models :as models]
    [java-time :as time]
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]
    [clj-sms.services.generate-code :as generate-code]
    [clj-sms.services.sendapi :as sendapi]
    [promesa.exec :as exec]
    [cuerdas.core :as str]
    [clj-sms.middleware.exception :as exception]))

(defn- get-format-msg [x y]
  (let [num-value (-> env :sms-check (get y))]
    (str/format (-> env :sms-check (get x)) {:num num-value})))

(defn send [phone code]

  ;; check if blocked
  (if (true? (-> env :sms-check :block-list))
    (let [data (first (db/select models/Block :phone phone :status 1))]
      (if-not (empty? data)
        (throw (ex-info "check" {:type ::exception/check :msg (-> env :sms-check :blocked-msg)})))))

  (let [date (time/local-date)
        data (->> (db/select models/Sms :phone phone :status 0 :created_at [:> date])
                  (map #(assoc % :created_at (time/local-date-time (:created_at %)))))

        mtime (time/minus (time/local-date-time) (time/minutes (-> env :sms-check :minute-scale)))
        mdata (filter #(time/before? mtime (:created_at %)) data)

        htime (time/minus (time/local-date-time) (time/hours 1))
        hdata (filter #(time/before? htime (:created_at %)) data)]
    (log/warn "date count = " (count data))
    (log/warn "mdate count = " (count mdata))
    (log/warn "hdate count = " (count hdata))

    ;; check minute send num
    (if (>= (count mdata) (-> env :sms-check :minute))
      (throw (ex-info "check" {:type ::exception/check :msg (get-format-msg :minute-check-msg :minute)})))

    ;; check minute send hour
    (if (>= (count hdata) (-> env :sms-check :hour))
      (throw (ex-info "check" {:type ::exception/check :msg (get-format-msg :hour-check-msg :hour)})))

    ;; check minute send day
    (if (>= (count data) (-> env :sms-check :day))
      (throw (ex-info "check" {:type ::exception/check :msg (get-format-msg :hour-check-msg :day)}))))

  ;; insert db row
  (let [code (if code code (generate-code/generate))]
    (db/insert! models/Sms :phone phone, :sms code)

    ;; send third party
    (if-not (true? (:dev env))
      (sendapi/sendsms {:phone phone :code code}))))
