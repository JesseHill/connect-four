(ns connect-four.server.player.remote
  (:require [connect-four.server.player :as p]
            [connect-four.server.transport :as t]
            [clojure.tools.logging :as log]
            [org.httpkit.client :as http]))

(defrecord Player [player options]
  p/Player
  (initialize [this]
    (log/debug "Building remote player:" (:player this))
    {:player (:player this)
     :type :remote})
  (get-move [this state]
    (let [uri (-> this :options :location)
          data (select-keys @state [:board :current-player :max-turn-time])
          request {:body (t/serialize data)
                   :headers {"Content-Type" "application/json"}}]
      (log/debug "Remote get-move posting request:" request "to uri:" uri)
      (try
        (let [response @(http/post uri request)]
          (if (= 200 (:status response))
            (t/deserialize (:body response))
            {:error "An error occured communicating with the remote player."}))
        (catch Exception e
          {:error (str "An error occured posting to:" uri)})))))

