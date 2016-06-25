# Writing a Chatbot with Clojure

* Ray Miller - Data Engineer, Metail
* Jim Downing - CTO, Metail

## Before we begin: -

* Please sign in with Jim so he can invite you to the Slack team we'll be using
* Get online
* You'll need: -
  * Java 7 or 8
  * Some kind of unix shell
  * Text editor

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
4. Custom Slack commands
5. Putting the pieces together

Follow along here: http://bit.ly/28W83M6

???

Who's used X before?
Clojure
Emacs

Who has used Slack before?

Which OS are people using?
Who has programmed in a language with higher order functions?
Get online.

TODO Check out how to increase text size in slides.

---

## Getting started at the REPL

Install Leiningen: http://leiningen.org/#install

```bash
lein repl
```

```clojure-repl
user=> (println "Hello World")
```

---
## Getting help

You can use functions from the `clojure.repl` namespace to get help in
the REPL:

```clojure
(require '[clojure.repl :refer [doc source find-doc]])
```

We can now use these functions to query documentation and view the
source of functions:

```clojure
(doc assoc)
(source assoc)
```

I always have the Clojure Cheatsheet open in a browser tab:
http://jafingerhut.github.io/cheatsheet/clojuredocs/cheatsheet-tiptip-cdocs-summary.html


---
## Getting started with this workshop

We're going to build out an application from a skeleton we made earlier.

```bash
git clone https://github.com/ray1729/clj-slack-demo.git
```

If you don't have git installed, you can download a zipfile from
https://github.com/ray1729/clj-slack-demo/archive/master.zip


---
## Clojure as a REST client

---
### Clojure as a REST client 1/5: Dependencies

We use the `clj-http` library, a sophisticated HTTP client, and the
`cheshire` library to parse and generate JSON strings. Check out
`project.clj` to see how these are configured.


```bash
cd clj-slack-demo
cat project.clj
```

When we start a REPL in the project directory, Leiningen will
automatically download the dependencies if we don't already have a
local copy.

```bash
lein repl
```

---
### Clojure as a REST client 2/5: Our first request

Load the HTTP client library and (for convenience) a pretty-print
function:

```clojure
(require '[clj-http.client :as http]
         '[clojure.pprint :refer [pprint]])
```

We can now make an HTTP request and print the result:

```clojure
(pprint (http/get "http://quotes.rest/qod.json"))
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

Open the file `src/chatbot/quotes.clj` in your favourite text editor.
You should see three functions whose bodies have been left for you to
fill in.

* A function `list-categories` to return the list of quote of
  the day categories.

* A function `get-qod` to return a quote of the day.

* A function `valid-qod-category?` that takes a string,
  `category`, and returns `true` if this is a recognized category,
  otherwise `false`.

* Modify your `get-qod` function to take an optional category. The
  function should verify that this is a valid category and retrieve a
  quote for that category.

---
### Extension exercises

* Calling `list-categories` every time we need to validate a category
  is an expensive operation - it requires a network request. Read
  about Clojure's `memoize` function and implement a memoized version
  of your `list-categories` function.

* The quote returned by the quote of the day API only changes once a
  day. Can you update your `get-qod` function to store
  the returned quote for a given category and refresh it only once a
  day?

---
## Posting messages to Slack

---
### Posting messages to Slack 1/3: Setting up in Slack

Configure an incoming WebHook:

https://dev-summer-cb.slack.com/apps/manage/

Left nav: Custom integrations > main pane: Incoming webhooks > main pane: Add configuration

Choose the #general channel and "Add Incoming Webhooks integration"

Copy the WebHook URL into your buffer.


Scroll down to Customize Name, and change it to e.g. {your name}-webhook

---

### Posting messages to Slack 2/3: Posting from the REPL

Paste / yank webhook URL into your REPL.

```clojure
(def webhook-url "https://hooks.slack.com/services/...")
```

Post a message to Slack:

```clojure
(http/post webhook-url {:form-params {:text "My first message"}
                                      :content-type :json})
```

---
### Posting messages to Slack 3/3: Advanced message formatting

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
### Exercise

* Use your `get-qod` function to retrieve a quote of the day, and
  post the resultant quote to Slack. *Hint: adding the prefix `>>>` to
  your message text formats it as a multi-line quote.*


---
class:center,middle

## Custom slack commands

---
### Slash commands

https://api.slack.com/slash-commands

Messages that start with a slash / are commands and will behave
differently from regular messages. For example, you can use the
"topic" command to change your current channel's topic to "Hello!" by
typing `/topic Hello!`. When you type `/remind me in 10 minutes to drink
a glass of water` the command will set a reminder for you to drink a
glass of water in 10 minutes.

---
### Custom commands

We implement a custom command by configuring Slack to POST the
command's payload to a remote URL. The remote server returns an
ephemeral message to the user or a posts a response to the channel.
The payload will look something like:

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

A walk through the skeleton web application.

---
### Exercise

* Extend the skeleton application to implement a handler to process a
  Slack quote of the day command.

* Try it out by posting a request to your app. You can do this from
  the REPL:

```clojure
(http/post "http://localhost:3000/slack"
  {:form-params {:token "gIkuvaNzQIHg97ATvDxqgjtO""
                 :command "/qod"
                 :text "life"}})
```

Note that `token` above should match the `slack-token` defined
`handler.clj` and `command` should match the command name you
implemented. What happens if you pass an invalid token? an unsupported
command?

---
### Extension exercises

* Extend your command handler to parse the optional `:text` parameter.

  * If the value is `list-categories`, return the list of available
    categories instead of a quote. If an

  * If the value is a valid categroy, return a quote in that category.

  * If the value is empty, return a random quote as before.

  * Otherwise, return an ephemeral message to the user who issued the
    command.

---
class:middle,center
## Putting the pieces together

---
### Putting the pieces together

1. Setup with Heroku
2. Login and create Heroku app
3. Configure Slack command integration
4. Configure leiningen for heroku integration
5. Build, deploy and start the Heroku app

---

### Pieces 1/5: Setup with Heroku

If you don't use Heroku, create an account at heroku.com

Then install the Heroku CLI https://toolbelt.heroku.com/

---

### Pieces 2/5: Login and create Heroku app
```bash
$ heroku login
Enter your Heroku credentials.
Email: adam@example.com
Password (typing will be hidden):
Authentication successful.

$ cd clj-slack-demo
$ heroku create
Creating vast-citadel-38177... done, stack is cedar-14
http://vast-citadel-38177.herokuapp.com/ | https://git.heroku.com/vast-citadel-38177.git
Git remote heroku added
```

---

### Pieces 3/5: Configure Slack command integration

1. https://dev-summer-cb.slack.com/apps/manage
2. Left nav: Custom Integrations > main pane: Slash Commands > main pane: Add configuration
3. Choose a command name that's unique to you. e.g. "/jim-qod". Click "Add Slash Command Integration"
4. Scroll down to "Integration settings". Set URL to {your Heroku app base URL}/slack

   e.g. https://vast-citadel-38177.herokuapp.com/slack

5. Copy the Token to your buffer
6. Customize the name to something unique to you.
7. "Save Integration"

---

### Pieces 4/5: Configure leiningen for heroku integration

Most of this has already been done for you in project.clj.

```clojure
 :plugins [[lein-ring "0.9.7"]
          [lein-heroku "0.5.3"]]
* :heroku {:app-name "vast-citadel-38177"
          :jdk-version "1.8"
          :include-files ["target/chatbot-0.1.0-SNAPSHOT-standalone.jar"]
          :process-types {"web" "java -jar target/chatbot-0.1.0-SNAPSHOT-standalone.jar"}}
```

You just need to customize the app-name on the highlighted line.

---

### Pieces 5/5: Build, deploy and start the Heroku app

Build and deploy:
```bash
lein ring uberjar
lein heroku deploy-uberjar
```

Start:
```bash
heroku ps:scale web=1 -a vast-citadel-38177
```

After a few seconds, try to access the status handler:

https://vast-citadel-38177.herokuapp.com/status


---

## Try it!

Go to the Slack channel, and type your command!

### If you enjoyed this...
Come and work with us!
http://metail.com/cambridge-jobs/
