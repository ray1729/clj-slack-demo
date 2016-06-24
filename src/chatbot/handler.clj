(ns chatbot.handler
  (:require [clojure.string :as str]
            [clojure.repl :refer [find-doc]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :refer [response status content-type charset]]
            [chatbot.middleware :refer [wrap-json-response]]))


(defmulti handle-slack-command (fn [request] (get-in request [:params :command])))

(defmethod handle-slack-command :default
  [{:keys [params]}]
  (-> (response (str "Unrecognized command: " (:command params)))
      (status 400)))

(defmethod handle-slack-command "/cljdoc"
  [{:keys [params] :as request}]
  (let [arg (str/lower-case (str/trim (:text params "")))]
    (if-let [doc-str (with-out-str (find-doc arg))]
      (response {:repsonse_type "ephemeral"
                 :text (str ">>> " doc-str)})
      (response {:response_type "ephemeral"
                 :text (str "No documentation found for " arg)}))))

(def slack-command-handler
  (wrap-json-response handle-slack-command))

(defn status-response
  [request]
  (-> (response "OK")
      (content-type "text/plain")))

(defroutes app-routes
  (GET "/status" [] status-response)
  (POST "/slack" [] slack-command-handler)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
