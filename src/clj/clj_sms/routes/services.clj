(ns clj-sms.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [clj-sms.middleware.formats :as formats]
    [clj-sms.middleware.exception :as exception]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]
    [spec-tools.data-spec :as ds]

    [clj-sms.services.sms-db-send :as sms-send]
    [clj-sms.services.sms-db-check :as sms-check]
    [clj-sms.services.blacklist :as block]))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/ping"
    {:get (constantly (ok {:message "pong"}))}]

   ["/send"
    {:post {:summary "check user sms."
            :parameters {:body {:phone string? (ds/opt :code) string?}}
            :responses {200 {:body {:code int? :msg string?, (ds/opt :errors) any?}}}
            :handler (fn [{{{:keys [phone] :as params} :body} :parameters}]
                       (sms-send/send phone params)
                       {:status 200
                        :body {:code 0 :msg "success"}})}}]

   ["/check"
    {:post {:summary "check user sms."
            :parameters {:body {:phone string? :code string?}}
            :responses {200 {:body {:code int? :msg string?, (ds/opt :errors) any?}}}
            :handler (fn [{{{:keys [phone code]} :body} :parameters}]
                       (sms-check/check phone code)
                       {:status 200
                        :body {:code 0 :msg "success"}})}}]

   ;; test get phone sms code
   ["/get/:phone"
    {:get {:summary "get phone code."
              :parameters {:path {:phone string?}}
              :responses {200 {:body {:code int? :msg string?, (ds/opt :errors) any?
                                                              , (ds/opt :data) any?}}}
              :handler (fn [{{{:keys [phone]} :path} :parameters}]
                         (let [code (sms-check/phone-code phone)]
                           {:status 200
                            :body
                            {:code 0 :msg "success"
                             :data code}}))}}]

   ["/block/:phone"
    {:get {:summary "check phone block."
              :parameters {:path {:phone string?}}
              :responses {200 {:body {:code int? :msg string?, (ds/opt :errors) any?
                                                              , (ds/opt :data) any?}}}
              :handler (fn [{{{:keys [phone]} :path} :parameters}]
                         (let [data (block/get-block phone)]
                           {:status 200 :body {:code 0 :msg "success" :data data}}))}

     :delete {:summary "delete phone from block."
              :parameters {:path {:phone string?}}
              :responses {200 {:body {:code int? :msg string?, (ds/opt :errors) any?
                                                              , (ds/opt :data) any?}}}
              :handler (fn [{{{:keys [phone]} :path} :parameters}]
                         (block/delete-block phone)
                         {:status 200 :body {:code 0 :msg "success"}})}}]


   ["/block"
    {:post {:summary "add phone to block."
              :parameters {:body {:phone string? :reason string?}}
              :responses {200 {:body {:code int? :msg string?, (ds/opt :errors) any?
                                                              , (ds/opt :data) any?}}}
              :handler (fn [{{{:keys [phone reason]} :body} :parameters}]
                         (block/add-block phone reason)
                         {:status 200 :body {:code 0 :msg "success"}})}}]])
