(ns clj-sms.db.core
  (:require
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]
    [mount.core :refer [defstate]]
    [clj-sms.util.redis :as redis]
    [clojure.set :refer [rename-keys]]
    [hikari-cp.core :refer [make-datasource]]
    [to-jdbc-uri.core :refer [to-jdbc-uri]]
    [toucan.db :as db]
    [toucan.models :refer [defmodel add-type!]]
    [cheshire.core :refer [generate-string parse-string]]))

(defn- format-url [pool-spec]
  (if (:jdbc-url pool-spec)
    (update pool-spec :jdbc-url to-jdbc-uri)
    pool-spec))

(defn make-config [{:keys [jdbc-url adapter datasource datasource-classname] :as pool-spec}]
  (when (not (or jdbc-url adapter datasource datasource-classname))
    (throw (Exception. "one of :jdbc-url, :adapter, :datasource, or :datasource-classname is required to initialize the connection!")))
  (-> pool-spec
      (format-url)
      (rename-keys
        {:auto-commit?  :auto-commit
         :conn-timeout  :connection-timeout
         :min-idle      :minimum-idle
         :max-pool-size :maximum-pool-size})))

(defn connect!
  "attempts to create a new connection and set it as the value of the conn atom,
   does nothing if conn atom is already populated"
  [pool-spec]
  (make-datasource (make-config pool-spec)))

(defn disconnect!
  "checks if there's a connection and closes it
   resets the conn to nil"
  [conn]
  (when-let [ds (:datasource conn)]
    (when-not (.isClosed ds)
      (.close ds))))

(defn reconnect!
  "calls disconnect! to ensure the connection is closed
   then calls connect! to establish a new connection"
  [conn pool-spec]
  (disconnect! conn)
  (connect! pool-spec))

(defstate rdb
  :start
  (if-let [redis-url (env :redis-url)]
    (redis/init {:url redis-url})
    (redis/init)))

(defstate ^:dynamic *db*
  :start (if-let [jdbc-url (env :database-url)]
           (let [conn
                 (connect! {:jdbc-url jdbc-url
                            :maximum-pool-size 2})]
             (db/set-default-db-connection! {:datasource conn})
             conn)
           (do
             (log/warn "database connection URL was not found, please set :database-url in your config, e.g: dev-config.edn")
             *db*))
  :stop (disconnect! *db*))


(add-type! :json
  :in  generate-string
  :out #(parse-string % keyword))
