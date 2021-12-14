(defproject clj-sms "0.3"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[ch.qos.logback/logback-classic "1.2.8"]
                 [cheshire "5.10.1"]
                 [clojure.java-time "0.3.3"]
                 [com.carouselapps/to-jdbc-uri "0.5.0"]
                 [cprop "0.1.19"]
                 [expound "0.8.10"]
                 [funcool/cuerdas "2021.05.29-0"]
                 [funcool/promesa "6.0.2"]
                 [funcool/struct "1.4.0"]
                 [hikari-cp "2.13.0"]
                 [luminus-http-kit "0.1.9"]
                 [luminus-migrations "0.7.1"]
                 [luminus-transit "0.1.3"]
                 [metosin/muuntaja "0.6.8"]
                 [metosin/reitit "0.5.15"]
                 [metosin/ring-http-response "0.9.3"]
                 [mount "0.1.16"]
                 [nrepl "0.9.0"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/tools.logging "1.2.1"]
                 [org.postgresql/postgresql "42.3.1"]
                 [ring/ring-core "1.9.4"]
                 [ring/ring-defaults "0.3.2"]
                 [toucan "1.16.0"]
                 ;; 阿里大鱼
                 [com.aliyun/aliyun-java-sdk-core "4.5.30"]
                 [com.aliyun/aliyun-java-sdk-dysmsapi "2.1.0"]]



  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot clj-sms.core

  :plugins [[lein-ancient "0.7.0"]]

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "clj-sms.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[pjstadig/humane-test-output "0.11.0"]
                                 [prone "2021-04-23"]
                                 [ring/ring-devel "1.8.1"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.25.0"]
                                 [jonase/eastwood "1.0.0"]]

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
