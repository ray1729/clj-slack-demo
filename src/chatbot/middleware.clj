(ns chatbot.middleware
  (:require [ring.util.response :refer [response status content-type charset]]
            [cheshire.core :as json]))

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
