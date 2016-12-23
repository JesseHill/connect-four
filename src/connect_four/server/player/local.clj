(ns connect-four.server.player.local
  (:require [connect-four.server.player :as p]
            [connect-four.server.board :as b]
            [clojure.tools.logging :as log]
            [clojure.java.shell :as shell]))

(defn log-and-invoke [executable board-string player-string time-string]
  (log/info "Calling" executable "with board:" board-string "player:" player-string "time:" time-string)
  (shell/sh executable "-b" board-string "-p" player-string "-t" time-string))

(defrecord Player [player options]
  p/Player
  (initialize [this]
    (log/debug "Building local player:" (:player this) "with options:" options)
    {:player (:player this)
     :type :local})
  (get-move [this state]
    (let [player (:current-player @state)
          board-string (b/serialize-board (:board @state))
          player-string (b/serialize-player player)
          time-string (str (:max-turn-time @state))
          executable (-> this :options :location)
          output (log-and-invoke executable board-string player-string time-string)
          move (:exit output)]
      (log/info (:out output))
      (log/info (:err output))
      (log/info executable "returned move:" move)
      {:column move})))