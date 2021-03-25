(ns clj-sms.services.sendapi
  (:require
    [promesa.exec :as exec]
    [clj-sms.services.third.dysmsapi :as dysmsapi]))

(defmulti sendsms :type)

(defmethod sendsms :default [params]
  (exec/schedule! 100 #(-> params dysmsapi/send-params dysmsapi/send-sms)))

; (defmethod sendsms :onlinepay2 [params]
  ; (exec/schedule! 100 #(dysmsapi/send-sms (-> params (assoc :sms-template "SMS_213550053") (clojure.set/rename {:code :verify})))))

; (defmethod sendsms :onlinepay1 [params]
  ; (exec/schedule! 100 #(dysmsapi/send-sms (-> params (assoc :sms-template "SMS_189030084")))))


(comment 
  
  (sendsms {:phone "15248141905" :type "onlinepay2" :code "359821" :amount "43.81"})
  
  (prn ""))