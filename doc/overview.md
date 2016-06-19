# Writing a Chatbot with Clojure

Jim Downing
Ray Miller

## Overview

* Getting started at the REPL
* Clojure as a REST client
** Interacting with the TheySaidSo API
* Posting messages to Slack
** Simple messages
** Message formatting and attachments
* A simple Clojure web server
* Handling Slack commands
** https://api.slack.com/slash-commands
* Deploying to Heroku
* Putting the pieces together

## Getting started at the REPL

Install Leiningen: http://leiningen.org/#install

    lein repl

    > (println "Hello World")

## Clojure as a REST client

## Deploying to Heroku

### Install Heroku Toolbelt

https://toolbelt.heroku.com/

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

Add to project.clj:

    :heroku {:app-name "vast-citadel-38177"}

Deploy:

    lein ring uberjar
    lein heroku deploy-uberjar

Start:

     heroku ps:scale web=1 -a vast-citadel-38177

### Testing

    (require '[clj-http.client :as http] '[chatbot.handler])

    (http/post "https://vast-citadel-38177.herokuapp.com/slack"
               {:form-params {:command "/quote" :token chatbot.handler/slack-token}})

## Putting the pieces together

Create Slack command integration, use URL for your Heroku app:

https://vast-citadel-38177.herokuapp.com/slack
