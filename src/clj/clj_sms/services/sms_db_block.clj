(ns clj-sms.services.sms-db-block
  (:require
    [toucan.db :as db]
    [honeysql.core :as sql]
    [clj-sms.db.models :as models]
    [java-time :as time]
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]))

(defn get-blocked [phone]
  (db/select models/Blocked :phone phone :status 1))
