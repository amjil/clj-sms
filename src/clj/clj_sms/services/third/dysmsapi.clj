(ns clj-sms.services.third.dysmsapi
  (:require
    [clojure.tools.logging :as log]
    [clj-sms.config :refer [env]]
    [cheshire.core :as cheshire])
  (:import
    [com.aliyuncs CommonRequest
                  CommonResponse
                  DefaultAcsClient
                  IAcsClient]
    [com.aliyuncs.exceptions ClientException
                             ServerException]
    [com.aliyuncs.http MethodType]
    [com.aliyuncs.profile DefaultProfile]))

(defn send-sms [params]
  (log/warn "dysmsapi sendsms ....... startd")
  (log/warn "params = " params)
  (let [params (dissoc params :type)
        dysms-config (-> env :third-party :dysms)
        profile (DefaultProfile/getProfile "cn-hangzhou" (:key dysms-config) (:secret dysms-config))
        client (DefaultAcsClient. profile)

        request (doto (CommonRequest.)
                      (.setSysMethod MethodType/POST)
                      (.setSysDomain "dysmsapi.aliyuncs.com")
                      (.setSysVersion "2017-05-25")
                      (.setSysAction "SendSms")
                      (.putQueryParameter "RegionId" "cn-hangzhou")
                      (.putQueryParameter "PhoneNumbers" (:phone params))
                      (.putQueryParameter "SignName" (:sign-name dysms-config))
                      (.putQueryParameter "TemplateCode" (:sms-template params))
                      (.putQueryParameter "TemplateParam" (cheshire/generate-string (dissoc params :phone :sms-template))))]

    (try
      (let [response (.getCommonResponse client request)]
        (log/warn (.getData response)))
      (catch ServerException e
        (.printStackTrace e))
      (catch ClientException e
        (.printStackTrace e))))
  (log/warn "dysmsapi sendsms ....... ended"))
