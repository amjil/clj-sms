(ns clj-sms.middleware.exception
  (:require [clojure.tools.logging :as log]
            [expound.alpha :as expound]
            [reitit.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]))

(derive ::error ::exception)
(derive ::failure ::exception)
(derive ::horror ::exception)


(defn handler [message exception request]
  {:status 500
   :body {:message message
          :exception (.getClass exception)
          :data (ex-data exception)
          :uri (:uri request)}})

(defn- custom-handler [exception request]
  {:status 200
   :body {:code 1 :msg (-> exception ex-data :msg)}})

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {;; log stack-traces for all exceptions
     ::error (partial handler "error")

     ;; ex-data with ::exception or ::failure
     ::exception (partial handler "exception")

     ;; ex-data with ::exception or ::check
     ::check custom-handler

     ::exception/default (partial handler "default")

     ;; SQLException and all it's child classes
     java.sql.SQLException (partial handler "sql-exception")

     ::exception/wrap (fn [handler e request]
                        (log/error e (.getMessage e))
                        (handler e request))})))
