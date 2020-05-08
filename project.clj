(defproject clj-sms "0.3"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.10.0"]
                 [clojure.java-time "0.3.2"]
                 [cprop "0.1.16"]
                 [expound "0.8.4"]
                 [funcool/cuerdas "2020.03.26-3"]
                 [funcool/promesa "5.1.0"]
                 [funcool/struct "1.4.0"]
                 [hikari-cp "2.12.0"]
                 [luminus-http-kit "0.1.6"]
                 [luminus-migrations "0.6.7"]
                 [luminus-transit "0.1.2"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.4.2"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.postgresql/postgresql "42.2.11"]
                 [ring/ring-core "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [toucan "1.15.1"]
                 ;; 阿里大鱼
                 [com.aliyun/aliyun-java-sdk-core "4.5.1"]
                 [com.aliyun/aliyun-java-sdk-dysmsapi "2.1.0"]]



  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot clj-sms.core

  :plugins [[lein-ancient "0.6.15"]]

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "clj-sms.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[pjstadig/humane-test-output "0.10.0"]
                                 [prone "2020-01-17"]
                                 [ring/ring-devel "1.8.1"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                 [jonase/eastwood "0.3.5"]]

                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user
                                 :timeout 120000}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
