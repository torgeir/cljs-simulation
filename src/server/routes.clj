(ns server.routes
  (:require [rum.core :as rum]
            [compojure.route :as route]
            [compojure.core :refer :all]
            [simulation.ui :as ui]))


(defonce state
  (atom {:running false}))


(defn layout [state partial]
  (str
    "<!doctype html>
     <html>
      <head>
        <title>A cljs simulation.</title>
        <link rel='stylesheet' href='css/app.css'>
      </head>
      <script>window.state = '" (pr-str state) "';</script>
      <body>
        <div id='app'>" partial "</div>
        <script src='js/simulation.js'></script>
      </body>
    </html>"))


(defn index [req]
  {:status  200
   :headers {"content-type" "text/html"}
   :body (layout @state (rum/render-html (ui/app state nil)))})


(defroutes app
  (route/resources "/" {:root "public"})
  (GET "/" [] #'index))
