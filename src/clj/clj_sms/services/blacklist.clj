(ns clj-sms.services.blacklist
  (:require
    [toucan.db :as db]
    [clj-sms.db.models :as models]
    [clara.rules :refer [defrule defquery mk-session query insert insert! fire-rules]]
    [clara.rules.accumulators :as acc]
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]))

(defrecord Result [status msg])

(defrecord Phone [num reason])

(defrule add-block
  [Phone (= ?num num) (= ?reason reason)]
  =>
  (let [data (first (db/select models/Block :phone ?num))]
    (if data
      (insert! (->Result false 'exists))
      (db/insert! models/Block :phone ?num :reason ?reason))))

(defquery query-errors
  [:?status]
  [?errors <- Result (= ?status status)])

(defn run-query [session]
  (query session query-errors :?status false))

(defn run-rules [phone reason]
  (-> (mk-session 'clj-sms.services.blacklist)
      (insert (->Phone phone reason))
      (fire-rules)))
