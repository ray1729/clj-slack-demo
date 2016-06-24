# Writing a Chatbot with Clojure

* Ray Miller
* Jim Downing

---

## Objective

Create a Slack command integration to provide a quote of the day.

* We'll implement a web server to handle a Slack command integration.
  Our server will retrieve a quote of the day from a third-party REST
  API and post the resulting quote back to the Slack channel.

---

## Overview

1. Getting started at the REPL
2. Clojure as a REST client
3. Posting messages to Slack
4. A simple Clojure web server
5. Handling Slack commands
6. Putting the pieces together

???

Who's used X before?
Clojure
Emacs

Which OS are people using?
Who has programmed in a language with higher order functions?
Get online.

TODO Add bitly link to these slides.
TODO Check out how to increase text size in slides.
TODO Sign in sheet for slack invite email addresses

---

## Getting started at the REPL

Install Leiningen: http://leiningen.org/#install

```bash
lein new hello-world
cd hello-world
lein repl
```

```clojure-repl
user=> (println "Hello World")
```

???
TODO in slide 'getting help', add link to the clojure cheat sheet.

---
## Getting help

```clojure
(require '[clojure.repl :refer [doc source find-doc]])
(doc assoc)
(source assoc)
```

---
## Clojure as a REST client

---
### Clojure as a REST client 1/5: Dependencies

Edit `project.clj` and add the following to dependencies:

```clojure
[clj-http "3.1.0"]
[cheshire "5.6.1"]
```

Restart the REPL:

```bash
lein repl
```

---
### Clojure as a REST client 2/5: Our first request

```clojure
(require '[clj-http.client :as http] '[clojure.pprint :refer [pprint]])
(def response (http/get "http://quotes.rest/qod.json"))
(pprint response)
```

---

### Clojure as a REST client 3/5: Parsing the body

```clojure
(def response (http/get "http://quotes.rest/qod.json" {:as :json}))
(pprint response)
(get-in response [:body :contents :quotes 0 :quote])
```

---
### Clojure as a REST client 4/5: Query parameters

```clojure
(def categories (http/get "http://quotes.rest/qod/categories.json" {:as :json}))
(get-in categories [:body :contents :categories])
(pprint (http/get "http://quotes.rest/qod.json"
                  {:as :json :query-params {:category "art"}}))
```

---
### Clojure as a REST client 5/5: Setting request headers

```clojure
(def api-key "h7GmOyL2jnVBjDgNyoaEDAeF")
(http/get "http://quotes.rest/qod.json"
          {:as :json
           :query-params {:category "life"}
           :headers {"X-Theysaidso-API-Secret" api-key}})
```

---
## Exercises

* Write a function `list-categories` to return the list of quote of
  the day categories.

* Write a function `get-quote` to return a quote of the day.

* Modify your `get-quote` function to take an optional category. The
  function should verify that this is a valid category and retrieve a
  quote for that category.

### Extension exercises

* Calling `list-categories` every time we need to validate a category
  is an expensive operation - it requires a network request. Read
  about Clojure's `memoize` function and implement a memoized version
  of your `list-categories` function.

* The quote returned by the quote of the day API only changes once a
  day. Can you update your `get-quote` function to store the returned
  quote for a given category and refresh it only once a day?

---
## Posting messages to Slack 1/2

Configure an incoming WebHook:

https://dev-summer-cb.slack.com/apps/manage/

Make a note of the WebHook URL:

```clojure
(def webhook-url "https://hooks.slack.com/services/...")
```

Post a message to Slack:

```clojure
(http/post webhook-url {:form-params {:text "My first message"}
                                :content-type :json})
```

??? TODO
Grab my changes from this morning.

---
### Posting messages to Slack 2/2: Advanced message formatting

https://api.slack.com/docs/message-attachments

```clojure
(def params
  {:attachments
    [{:fallback
      "New ticket from Andrea Lee - Ticket #1943: Can't rest my password - https://groove.hq/path/to/ticket/1943",
      :pretext "New ticket from Andrea Lee",
      :title "Ticket #1943: Can't reset my password",
      :title_link "https://groove.hq/path/to/ticket/1943",
      :text "Help! I tried to reset my password but nothing happened!",
      :color "#7CD197"}]})
(http/post webhook-url {:form-params params :content-type :json})
```

---
## Exercise

* Use your `get-quote` function to retrieve a quote of the day, and
  post the resultant quote to Slack. *Hint: adding the prefix `>>>` to
  your message text formats it as a multi-line quote.*

---
## Implementing a web service in Clojure

---
### Web Server 1/9: Ring

* Clojure’s equivalent of Ruby’s Rack, Python’s WSGI and Perl’s Plack
* Deals with the Java Servlet API
* Provides utilities for parsing requests and generating responses
* Extensible through middleware

---
### Web Server 2/9: Our first Ring application

```bash
lein new compojure chatbot
cd chatbot
lein ring server
```

---
### Web Server 3/9: Our first Ring application

```clojure
(ns chatbot.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
```

---
### Web Server 4/9: The request map

Update `project.clj`:

```clojure
{:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                      [ring/ring-mock “0.3.0”]
*                     [ring/ring-devel “1.4.0”]]}}
```

Edit `chatbot/handler.clj`:

```clojure
(ns chatbot.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults
                :refer [wrap-defaults site-defaults]]
*           [ring.handler.dump :refer [handle-dump]]))

(defroutes app-routes
  (GET "/" [] "Hello World")
* (GET "/dump/*" [] handle-dump)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
```

* Restart your app and make some requests to <http://localhost:3000/dump/>
* Try adding query parameters to the URL

---
### Web Server 5/9: The response map

```clojure
(defn my-handler
  [request]
  {:body "Hello World"
   :status 200
   :headers {"Content-Type" "text/plain"}})

(defroutes app-routes
  (GET "/" [] my-handler)
  (route/not-found "Not Found"))
```

---
### Web Server 6/9: response utility

```clojure
(require '[ring.util.response :refer [response status charset content-type]])
(response "Hello World")
(-> (response "Not found") (status 404) (content-type "text/plain"))
```

---
### Web Server 7/9: Middleware

A ring handler is simply a function that receives a request map and returns a response map.

Ring middleware is simply a function that modifies the behaviour of a
handler. It can run before the handler and modify the request map, or
after the handler and modify the response map.

---
### Web Server 8/9: Request Middleware

```clojure
(defn wrap-check-token
  [handler]
  (fn [request]
    (if (= (get-in request [:params :token]) slack-token)
      (handler request)
      (-> (response "Invalid token")
          (status 400)))))
```

---
### Web Server 9/9: Response Middleware

```clojure
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
```

---
## Exercise

* In your webserver project, create a namespace `quotes.clj` and add
your `get-quote` and `list-categories` functions to that namespace.

* Update your `project.clj` to add the dependencies required by your
quotes functions.

???

TODO add header s-expr for quotes.clj

---
## Handling slack commands

https://api.slack.com/slash-commands

Messages that start with a slash / are commands and will behave
differently from regular messages. For example, you can use the
"topic" command to change your current channel's topic to "Hello!" by
typing /topic Hello!. When you type /remind me in 10 minutes to drink
a glass of water the command will set a reminder for you to drink a
glass of water in 10 minutes.

---
### Custom commands

We can configure Slack to POST a command's payload to a remote URL,
and have the remote server return a response to the channel. The
payload will look something like:

```
token=gIkuvaNzQIHg97ATvDxqgjtO
team_id=T0001
team_domain=example
channel_id=C2147483705
channel_name=test
user_id=U2147483697
user_name=Steve
command=/weather
text=94070
response_url=https://hooks.slack.com/commands/1234/5678
```

---
### Handling a custom command

To handle a Slack command, we should:

* Check the token to make sure it is a valid command
* Check the command to make sure it is recogvized by our handler
* Parse any arguments from the `text` field
* Generate a response to return to Slack

---
### Custom command example

```clojure

(require '[clojure.repl :refer [find-doc]]
         '[clojure.string :as str]
         '[ring.util.response :refer [response content-type charset status]])

(def slack-token "...")

(defn bad-request
  [message]
  (-> (response message)
      (content-type "text/plain")
      (status 400))

(defn wrap-check-token
  [handler]
  (fn [request]
    (if (= (get-in request [:params :token]) slack-token)
      (handler request)
      (bad-request "Invalid token"))))

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

(defmulti handle-slack-command (fn [request] (get-in request [:params :command]))

(defmethod handle-slack-command :default
  [{:keys [params]}]
  (bad-request (str "Unrecognized command: " (:command params))))

(defmethod handle-slack-command "/cljdoc"
  [{:keys [params]}]
  (let [query (str/trim (:text params ""))]
    (if-let [doc-str (with-out-str (find-doc query))]
      (response {:text (str ">>> " doc-str)})
      (response {:text (str "No documentation found for " query)}))))

(def slack-handler (wrap-json-response (wrap-check-token handle-slack-command)))
```

---
## Exercise

* Modify your web application to implement handler to process a Slack
quote of the day command.

??? Testing section here?

---
## Deploying to Heroku

- Install Heroku Toolbelt
- Login and create app
- Configure leiningen integration
- Deploy and start app
- Testing

---

### Heroku 1/5: Install Heroku Toolbelt

https://toolbelt.heroku.com/

---

### Heroku 2/5: Login and create app
```bash
$ heroku login
Enter your Heroku credentials.
Email: adam@example.com
Password (typing will be hidden):
Authentication successful.

$ cd ~/myapp
$ heroku create
Creating vast-citadel-38177... done, stack is cedar-14
http://vast-citadel-38177.herokuapp.com/ | https://git.heroku.com/vast-citadel-38177.git
Git remote heroku added
```

---

### Heroku 3/5: Configure leiningen integration

Most of this has already been done for you in project.clj

```clojure
:plugins [[lein-ring "0.9.7"]
          [lein-heroku "0.5.3"]]
* :heroku {:app-name "young-thicket-18780"
         :jdk-version "1.8"
         :include-files ["target/chatbot-0.1.0-SNAPSHOT-standalone.jar"]
         :process-types {"web" "java -jar target/chatbot-0.1.0-SNAPSHOT-standalone.jar"}}
```

You just need to customize the app-name on the highlighted line.

???
TODO: Would be good to get this out of project.clj

---

### Deploy and start app
Deploy:
```bash
lein ring uberjar
lein heroku deploy-uberjar
```
Start:
```bash
heroku ps:scale web=1 -a vast-citadel-38177
```

---

### Testing

```clojure
(require '[clj-http.client :as http] '[chatbot.handler])

(http/post "https://vast-citadel-38177.herokuapp.com/slack"
           {:form-params {:command "/quote"
                          :token chatbot.handler/slack-token}})
```

---

## Putting the pieces together

Create Slack command integration, use URL for your Heroku app:

https://vast-citadel-38177.herokuapp.com/slack
