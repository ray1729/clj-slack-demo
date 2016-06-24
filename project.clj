(defproject chatbot "0.1.0-SNAPSHOT"
  :description "Clojure slack command integration demo"
  :url "https://github.com/ray1729/clj-slack-demo"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.0"]
                 [ring/ring-defaults "0.2.0"]
                 [clj-http "3.1.0"]
                 [cheshire "5.6.1"]
                 [org.clojure/core.memoize "0.5.9"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-heroku "0.5.3"]]
  :heroku {:app-name "vast-citadel-38177"
           :jdk-version "1.8"
           :include-files ["target/chatbot-0.1.0-SNAPSHOT-standalone.jar"]
           :process-types {"web" "java -jar target/chatbot-0.1.0-SNAPSHOT-standalone.jar"}}
  :ring {:handler chatbot.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}
   :uberjar {:aot :all}})
