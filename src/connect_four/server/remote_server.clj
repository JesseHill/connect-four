(ns connect-four.server.remote-server
  (:require [mount.core :as mount :refer [defstate]]
            [org.httpkit.server :as http]
            [clojure.tools.logging :as log]
            [connect-four.server.player :as player]
            [connect-four.server.player.local :as local]
            [connect-four.server.transport :as t]))

(defn handle-request [executable request]
  (log/debug "Remote server handling request: " request "with exe:" executable)
  (let [data (t/deserialize (:body request))
        state (atom data)
        player (local/->Player (:current-player data) {:location executable})
        move (player/get-move player state)]
    {:status 200
     :body (t/serialize move)}))

(defn build-api [executable]
  (partial handle-request executable))

(defn start-server [options]
  (let [port (:remote-server-port options)]
    (log/info "Starting remote server on port:" port)
    (let [api (build-api (:remote-server options))]
      (http/run-server api {:port port}))))

(defstate server :start (start-server (mount/args)))