(ns clj-sms.test.handler
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [clj-sms.handler :refer :all]
    [clj-sms.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'clj-sms.config/env
                 #'clj-sms.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 301 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= nil (:status response)))))
  (testing "services"))
