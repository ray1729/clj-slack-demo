# Writing a Chatbot with Clojure

Ray Miller
Jim Downing

---

## Overview

* Getting started at the REPL
* Clojure as a REST client
  * Interacting with the TheySaidSo API
* Posting messages to Slack
  * Simple messages
  * Message formatting and attachments
* A simple Clojure web server
* Handling Slack commands
  * https://api.slack.com/slash-commands
* Deploying to Heroku
* Putting the pieces together

---

## Getting started at the REPL

Install Leiningen: http://leiningen.org/#install

```clojure-repl
lein repl

user=> (println "Hello World")
```

---

## Clojure as a REST client

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
