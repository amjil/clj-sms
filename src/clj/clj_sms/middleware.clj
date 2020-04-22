(ns clj-sms.middleware
  (:require
    [clj-sms.env :refer [defaults]]
    [clj-sms.config :refer [env]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)))))
