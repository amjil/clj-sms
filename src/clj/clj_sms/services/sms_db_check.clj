(ns clj-sms.services.sms-db-check
  (:require
    [clj-sms.db.core :refer [*db*]]
    [next.jdbc.sql :as sql]
    [honeysql.core :as hsql]
    [clj-sms.db.models :as models]
    [java-time :as time]
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]
    [promesa.exec :as exec]
    [cuerdas.core :as str]
    [clj-sms.middleware.exception :as exception]))

(defn- get-config [id]
  (-> env :sms-check (get id)))

(defn check [mobile code]
  (let [date (time/minus (time/local-date-time) (time/seconds (-> env :sms-check :valid-time)))
        data (first (db/select models/Sms :phone mobile, :status 0, :created_at [:> date], {:limit 1 :order-by [[:created_at :desc]]}))]

    (if (empty? data)
      (throw (ex-info "check" {:type ::exception/check :msg (get-config :no-record-msg)})))

    (if (not= code (:sms data))
      (throw (ex-info "check" {:type ::exception/check :msg (get-config :not-match-msg)})))

    (db/update! models/Sms (:id data) :status 1)))


(defn phone-code [phone]
  (if (true? (:dev env))

    (let [date (time/minus (time/local-date-time) (time/seconds (-> env :sms-check :valid-time)))
          data (first (db/select models/Sms :phone phone, :status 0, :created_at [:> date], {:limit 1 :order-by [[:created_at :desc]]}))]

      (if (empty? data)
        (throw (ex-info "check" {:type ::exception/check :msg :no-record})))

      (:sms data))

    (throw (ex-info "check" {:type ::exception/check :msg "错误！！"}))))
