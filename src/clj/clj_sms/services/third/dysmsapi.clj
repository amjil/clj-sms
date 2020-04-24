(ns clj-sms.services.third.dysmsapi
  (:require
    [clojure.tools.logging :as log]
    [clj-sms.env :refer [env]])
  (:import
    [com.aliyuncs CommonRequest
                  CommonResponse
                  DefaultAcsClient
                  IAcsClient]
    [com.aliyuncs.exceptions ClientException
                             ServerException]
    [com.aliyuncs.http MethodType]
    [com.aliyuncs.profile DefaultProfile]))

(defn sendsms [params]
  (log/warn "dysmsapi sendsms ....... startd")
  (let [dysms-config (-> env :third-party :dysms)
        profile (DefaultProfile/getProfile "cn-hangzhou"  (:key dysms-config) (:secret dysms-config))
        client (DefaultAcsClient. profile)

        request (doto (CommonRequest.)
                      (.setSysMethod MethodType/POST)
                      (.setSysDomain "dysmsapi.aliyuncs.com")
                      (.setSysVersion "2017-05-25")
                      (.setSysAction "SendSms")
                      (.putQueryParameter "RegionId" "cn-hangzhou")
                      (.putQueryParameter "PhoneNumbers" (:phone params))
                      (.putQueryParameter "SignName" (:sign-name dysms-config))
                      (.putQueryParameter "TemplateCode" (:sms-template dysms-config))
                      (.putQueryParameter "TemplateCode" (str "{\"code\":\"" (:code params) "\"}")))]

    (try
      (let [response (.getCommonResponse client request)]
        (log/warn (.getData response)))
      (catch ServerException e
        (.printStackTrace e))
      (catch ClientException e
        (.printStackTrace e))))
  (log/warn "dysmsapi sendsms ....... ended"))
