(ns chatbot.quotes
  (:require [clj-http.client :as http]
            [clojure.string :as str]))

(def api-url "http://quotes.rest/")

(def api-key "h7GmOyL2jnVBjDgNyoaEDAeF")

(defn list-qod-categories
  []
  ;; XXX FIX ME
  )

(defn valid-qod-category?
  [category]
  ;; XXX FIX ME
  )

(defn get-qod
  ([]
   (get-qod nil))
  ([category]
   ;; XXX FIX ME
   ))
