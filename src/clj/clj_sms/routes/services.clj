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
    [clj-sms.services.sms-db-check :as sms-check]))

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
            :handler (fn [{{{:keys [phone code]} :body} :parameters}]
                       {:status 200
                        :body
                        (let [rule-session (sms-send/run-rules phone code)
                              errors (map #(-> % :?errors :msg) (sms-send/run-query rule-session))]
                          (if (empty? errors)
                            {:code 0 :msg "success"}
                            {:code 1 :msg (str (first errors)) :errors errors}))})}}]

   ["/check"
    {:post {:summary "check user sms."
            :parameters {:body {:phone string? :value string?}}
            :responses {200 {:body {:code int? :msg string?, (ds/opt :errors) any?}}}
            :handler (fn [{{{:keys [phone value]} :body} :parameters}]
                       {:status 200
                        :body
                        (let [rule-session (sms-check/run-rules phone value)
                              errors (map #(-> % :?errors :msg) (sms-check/run-query rule-session))]
                          (if (empty? errors)
                            {:code 0 :msg "success"}
                            {:code 1 :msg (str (first errors)) :errors errors}))})}}]])
