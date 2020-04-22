(ns clj-sms.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[clj-sms started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[clj-sms has shut down successfully]=-"))
   :middleware identity})
