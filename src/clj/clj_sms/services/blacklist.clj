(ns clj-sms.services.blacklist
  (:require
    [toucan.db :as db]
    [clj-sms.db.models :as models]
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]))

(defn add-block [phone reason]
  (let [data (db/select models/Block :phone phone :status 1)]
    (if-not (empty? data)
      (db/update-where! models/Block {:phone phone} :status 0))

    (db/insert! models/Block :phone phone :reason reason)))


(defn delete-block [phone]
  (db/update-where! models/Block {:phone phone :status 1}
    :status -1))

(defn get-block [phone]
  (let [data (first (db/select models/Block :phone phone :status 1))]

    (if (empty? data)
      {:reason "not blocked"}
      {:reason (:reason data) :updated_at (:updated_at data)})))
