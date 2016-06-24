(ns chatbot.handler
  (:require [clojure.string :as str]
            [clojure.repl :refer [find-doc]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :refer [response status content-type charset]]
            [cheshire.core :as json]
            [chatbot.quotes :as q]))

(def slack-token "lPpwt1VScc5Qi8Dzw0mYTKrw")

(defmulti handle-slack-command (fn [request] (get-in request [:params :command])))

(defmethod handle-slack-command :default
  [{:keys [params]}]
  (-> (response (str "Unrecognized command: " (:command params)))
      (status 400)))

(defn quote-of-the-day-response
  [category]
  (let [opts (if category {:category category} {})
        {:keys [quote author]} (q/get-qod opts)]
    (response {:response_type "in_channel"
               :text (str ">>> " quote "\n— " author)})))

(defn list-categories-response
  []
  (response {:response_type "ephemeral"
             :text (str/join ", " (q/list-qod-categories))}))

(defn unrecognized-category-response
  [category]
  (response {:response_type "ephemeral"
             :text (format "Sorry, '%s' is not a recognized category." category)}))

(defmethod handle-slack-command "/qod"
  [{:keys [params] :as request}]
  (let [arg (str/lower-case (str/trim (:text params "")))]
    (cond
      (= arg "list-categories")   (list-categories-response)
      (q/valid-qod-category? arg) (quote-of-the-day-response arg)
      (empty? arg)                (quote-of-the-day-response nil)
      :else                       (unrecognized-category-response arg))))

(defmethod handle-slack-command "/cljdoc"
  [{:keys [params] :as request}]
  (let [arg (str/lower-case (str/trim (:text params "")))]
    (if-let [doc-str (find-doc arg)]
      (response {:repsonse_type "in_channel"
                 :text (str ">>> " doc-str)})
      (response {:response_type "ephemeral"
                 :text (str "No documentation found for " arg)}))))

(defn wrap-check-token
  [handler]
  (fn [request]
    (if (= (get-in request [:params :token]) slack-token)
      (handler request)
      (-> (response "Invalid token")
          (status 400)))))

(defn wrap-json-response
  [handler]
  (fn [request]
    (let [res (handler request)]
      (if (coll? (:body res))
        (-> res
            (update :body json/generate-string)
            (content-type "application/json")
            (charset "UTF-8"))
        res))))

(defroutes app-routes
  (GET "/status" [] (-> (response "OK") (content-type "text/plain")))
  (POST "/slack" [] (wrap-json-response (wrap-check-token handle-slack-command)))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
