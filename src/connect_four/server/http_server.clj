(ns connect-four.server.http-server
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream])
  (:require [mount.core :refer [defstate]]
            [org.httpkit.server :as httpkit :refer :all]
            [ring.logger :as logger]
            [ring.middleware.json :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [connect-four.server.state :refer [state] :as st]
            [cognitect.transit :as transit]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(defn serialize [data]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer data)
    (.toByteArray out)))

(def api-config
  (assoc-in api-defaults [:static :resources] "public"))

(defn channel-notifier [channel]
  (fn [_ _ _ new-state]
    (send! channel (serialize new-state))))

(defn game-state-handler [req]
  (with-channel req channel
    (st/client-connected state (.hashCode channel))
    (send! channel (serialize @state))
    (add-watch state channel (channel-notifier channel))
    (on-close channel (fn [status]
                        (remove-watch state channel)
                        (st/client-disconnected state (.hashCode channel))))
    (on-receive channel (fn [data] ;; echo it back
                          (send! channel data)))))

(defroutes api-routes
  (context "/api" []
    ; important, do not remove this route:
    (GET "/hello" [] (:hello-message @state))

    (GET "/game-state" [] game-state-handler)
    (route/not-found "Invalid API URI.")
    )
  (route/resources "/")
  (route/not-found (io/resource "public/index.html")))

(def api
  (-> api-routes
    (logger/wrap-with-logger)
    (json/wrap-json-body {:keywords? true})
    (json/wrap-json-response)
    (wrap-defaults api-config)
    ))

(defn start-server []
  (let [port 5000]
    (log/info "Started connect-four server on port:" port)
    (run-server api {:port port})))

(defn stop-server [server]
  ; server is a function that, when called, shuts down the server
  (server))

(defstate server :start (start-server)
                 :stop (stop-server server))