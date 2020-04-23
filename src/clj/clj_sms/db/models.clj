(ns clj-sms.db.models
  (:require
    [clj-sms.db.toucan]
    [toucan.models :refer [defmodel]]))

(defmodel Sms :user_sms_log)
  ; (types [this]
  ;   {:status :keyword}))
