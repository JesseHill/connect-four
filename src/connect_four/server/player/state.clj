(ns connect-four.server.player.state
  (:require [connect-four.server.player :as player]
            [connect-four.server.player.local :as local]
            [connect-four.server.player.remote :as remote]
            [connect-four.server.player.robot :as robot]
            [connect-four.server.player.random :as random]
            [mount.core :refer [defstate] :as mount]))

(defn build-player [player data]
  (case (:type data)
    :local (local/->Player player data)
    :remote (remote/->Player player data)
    :robot (robot/->Player player (atom (:location data)))
    (random/->Player player data)))

(defn create-player [player data]
  (let [player (build-player player data)]
    (player/initialize player)
    player))

(defstate player-one :start (create-player :player-one (:player-one (mount/args))))
(defstate player-two :start (create-player :player-two (:player-two (mount/args))))