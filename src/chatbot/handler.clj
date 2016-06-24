(ns chatbot.handler
  (:require [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer [find-doc]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :refer [response status content-type charset]]
            [chatbot.middleware :refer [wrap-check-token wrap-json-response]]))

(def slack-token "lPpwt1VScc5Qi8Dzw0mYTKrw")

(defmulti handle-slack-command (fn [request] (get-in request [:params :command])))

(defmethod handle-slack-command :default
  [{:keys [params]}]
  (-> (response (str "Unrecognized command: " (:command params)))
      (status 400)))

(defmethod handle-slack-command "/cljdoc"
  [{:keys [params] :as request}]
  (let [arg (str/lower-case (str/trim (:text params "")))]
    (if-let [doc-str (find-doc arg)]
      (response {:repsonse_type "ephemeral"
                 :text (str ">>> " doc-str)})
      (response {:response_type "ephemeral"
                 :text (str "No documentation found for " arg)}))))

(def slack-command-handler
  (wrap-json-response (wrap-check-token handle-slack-command slack-token)))

(defn status-response
  [request]
  (pprint request)
  (-> (response "OK")
      (content-type "text/plain")))

(defroutes app-routes
  (GET "/status" [] status-response)
  (POST "/slack" [] slack-command-handler)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
