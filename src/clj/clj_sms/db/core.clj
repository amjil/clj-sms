(ns clj-sms.db.core
  (:require
    [mount.core :refer [defstate]]
    [clj-sms.util.redis :as redis]))

(defstate rdb
  :start
  (redis/init))
  ; (if-let [redis-url (env :redis-url)]
  ;   (redis/init {:url redis-url})
  ;   (redis/init)))
