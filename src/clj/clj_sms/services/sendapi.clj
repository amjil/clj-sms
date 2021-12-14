(ns clj-sms.services.sendapi
  (:require
    [promesa.exec :as exec]
    [clj-sms.services.third.dysmsapi :as dysmsapi]))

(defmulti sendsms :type)

(defmethod sendsms :default [params]
  (exec/schedule! 100 #(-> params dysmsapi/send-sms)))
