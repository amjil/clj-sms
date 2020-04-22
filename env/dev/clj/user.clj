(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [clj-sms.config :refer [env]]
   [clojure.pprint]
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [clj-sms.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start []
  (mount/start))

(defn stop []
  (mount/stop))

(defn restart
  "Restarts application."
  []
  (mount/stop)
  (mount/start))
