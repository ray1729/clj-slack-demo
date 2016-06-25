(ns chatbot.handler
  (:require [clojure.string :as str]
            [clojure.repl :refer [find-doc]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :refer [response status content-type]]
            [chatbot.middleware :refer [wrap-json-response]]))

(defn cmd-cljdoc
  [{:keys [params] :as request}]
  (let [arg (str/lower-case (str/trim (:text params "")))]
    (if-let [doc-str (with-out-str (find-doc arg))]
      (response {:repsonse_type "ephemeral"
                 :text (str ">>> " doc-str)})
      (response {:response_type "ephemeral"
                 :text (str "No documentation found for " arg)}))))

(defn bad-request
  [message]
  (-> (response message)
      (content-type "text/plain")
      (status 400)))

(def slack-commands
  {"/cljdoc" {:token "k6A62fHGHmSdD1aKhs1BRwQT" :impl cmd-cljdoc}})

(defn slack-handler
  [{:keys [params] :as request}]
  (if-let [{:keys [token impl]} (slack-commands (:command params))]
    (if (= (:token params) token)
      (impl request)
      (bad-request "Invalid token"))
    (bad-request "Unsupported command")))

(defn status-handler
  [request]
  (-> (response "OK")
      (content-type "text/plain")))

(defroutes app-routes
  (GET "/status" [] status-handler)
  (POST "/slack" [] (wrap-json-response slack-handler))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
