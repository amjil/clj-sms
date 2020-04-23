(ns clj-sms.db.toucan
  (:require
    [toucan.db :as db]
    [toucan.models :refer [defmodel]]
    [clj-sms.db.core :refer [*db*]]))

(db/set-default-db-connection! {:datasource *db*})
