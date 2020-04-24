(ns clj-sms.services.third.dysmsapi
  (:require
    [clojure.tools.logging :as log])
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
  (let [profile (DefaultProfile/getProfile "cn-hangzhou"  "LTAI4GH55adzYzFXrYE9CmSC"  "WNXvpxKkfrj0IdyDXYDY8iMTauwdVS")
        client (DefaultAcsClient. profile)

        request (doto (CommonRequest.)
                      (.setSysMethod MethodType/POST)
                      (.setSysDomain "dysmsapi.aliyuncs.com")
                      (.setSysVersion "2017-05-25")
                      (.setSysAction "SendSms")
                      (.putQueryParameter "RegionId" "cn-hangzhou")
                      (.putQueryParameter "PhoneNumbers" (:phone params))
                      (.putQueryParameter "SignName" "旅游在线")
                      (.putQueryParameter "TemplateCode" "SMS_189030084")
                      (.putQueryParameter "TemplateCode" (str "{\"code\":\"" (:code params) "\"}")))]

    (try
      (let [response (.getCommonResponse client request)]
        (log/warn (.getData response)))
      (catch ServerException e
        (.printStackTrace e))
      (catch ClientException e
        (.printStackTrace e))))
  (log/warn "dysmsapi sendsms ....... ended"))
