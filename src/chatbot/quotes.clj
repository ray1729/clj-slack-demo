(ns chatbot.quotes
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.core.memoize :as memo]))

(def api-url "http://quotes.rest/")

(def api-key "h7GmOyL2jnVBjDgNyoaEDAeF")

(defn api-call
  [& args]
  (let [[path-parts [query]] (split-with string? args)
        path (str/join "/" path-parts)
        url  (str api-url path ".json")]
    (:body (http/get url {:query-params query
                          :headers {"X-Theysaidso-API-Secret" api-key}
                          :as :json}))))

(defn get-qod*
  ([]
   (get-qod* nil))
  ([opts]
   (let [res (api-call "qod" opts)]
     (get-in res [:contents :quotes 0]))))

(def get-qod (memo/ttl get-qod* :ttl/threshold (* 4 60 60 1000)))

(defn list-qod-categories*
  []
  (->> (api-call "qod" "categories")
       :contents
       :categories
       keys
       (map name)))

(def list-qod-categories (memo/ttl list-qod-categories* :ttl/threshold (* 24 60 60 1000)))

(defn valid-qod-category?
  [category]
  (some (fn [known] (= category known)) (list-qod-categories)))

(defn get-quote
  ([]
   (get-quote nil))
  ([opts]
   (:contents (api-call "quote" opts))))

(defn list-quote-categories
  ([]
   (list-quote-categories 0))
  ([start]
   (api-call "quote" "categories" {:start start})))

(comment

  (http/get (str api-url "qod.json"))

  (def res (http/get (str api-url "qod.json") {:as :json}))

  (:body res)

  (require '[clojure.pprint :refer [pprint]])

  (pprint (:body res))

  (get-in res [:body :contents :quotes 0 :quote])

  (def categories (http/get (str api-url "qod/categories.json") {:as :json}))

  (pprint categories)

  (keys (get-in categories [:body :contents :categories]))

  (http/get (str api-url "qod.json") {:query-params {:category "management"}
                                      :as :json})

  (api-call "qod" "categories")

  (api-call "qod")

  (api-call "qod" {:category "funny"})

  ;; We've been looking at qod (quote of the day); the more general
  ;; 'quote' API has more categories:

  (def all-cats (api-call "quote" "categories"))

  (pprint all-cats)

  (count (get-in all-cats [:contents :categories]))

  (def next-cats (api-call "quote" "categories" {:start 200}))

  (pprint next-cats)

  (api-call "quote" {:category "absinthe"})
  )
