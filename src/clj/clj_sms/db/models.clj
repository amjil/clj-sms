(ns clj-sms.db.models
  (:require
    [toucan.models :refer [defmodel]]))

(defmodel Sms :user_sms_log)
  ; (types [this]
  ;   {:status :keyword}))

(defmodel Block :user_black_list)
