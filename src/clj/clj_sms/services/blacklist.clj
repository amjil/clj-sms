(ns clj-sms.services.blacklist
  (:require
    [toucan.db :as db]
    [clj-sms.db.models :as models]
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]))

(defn add-block [phone reason]
  (let [data (first (db/select models/Block :phone phone))]
    (if data
      (db/insert! models/Block :phone phone :reason reason))))

(defn delete-block [phone]
  (db/update-where! models/Block {:phone phone}
    :status 0))
