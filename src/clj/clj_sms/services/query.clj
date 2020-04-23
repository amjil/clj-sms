(ns clj-sms.services.query
  (:require
    [clara.rules :refer [defrule defquery mk-session query insert insert! fire-rules]]))

(defrecord Result [status msg])

(defquery query-errors
  [:?status]
  [?errors <- Result (= ?status status)])

(defn get-errors [session]
  (query session query-errors :?status false))
