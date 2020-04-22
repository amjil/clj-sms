(ns clj-sms.env
  (:require
    [clojure.tools.logging :as log]
    [clj-sms.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[clj-sms started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[clj-sms has shut down successfully]=-"))
   :middleware wrap-dev})
