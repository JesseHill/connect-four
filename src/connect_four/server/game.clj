(ns connect-four.server.game
  (:require [mount.core :as mount :refer [defstate]]
            [connect-four.server.state :refer [state] :as st]
            [connect-four.server.board :as board]
            [connect-four.server.player :as player]
            [connect-four.server.util :as util]
            [connect-four.server.player.state :refer [player-one player-two]]
            [clojure.tools.logging :as log]))

(defn alternate-player [state]
  (if (= :player-one (:current-player @state))
    :player-two
    :player-one))

(defn updates-for-current-player [state]
  {:current-player (alternate-player state)})

(defn updates-for-game-won [state]
  {:status "player.won"
   :running false
   :winner (:current-player @state)})

(defn updates-for-game-tied [state]
  {:status "game.tied"
   :running false
   :winner :tie})

(defn updates-for-player-error [state error]
  {:status "player.error"
   :error error
   :running false
   :winner (alternate-player state)})

(defn updates-for-state [board state]
  (cond
    (board/won? board) (updates-for-game-won state)
    (board/tied? board) (updates-for-game-tied state)
    :default (updates-for-current-player state)))

(defn updates-for-timers [state move player]
  {(st/player-timer-key player) (+ (:time-elapsed move)
                                   (st/player-timer @state player))})

(defn check-for-error [state move]
  (or (:error move)
      (when (board/invalid-move? (:board @state) move) :invalid-move)))

(defn update-state-for-error [state error]
  (swap! state merge (updates-for-player-error state error)))

(defn update-state-for-valid-move [state move]
  (let [{:keys [board current-player]} @state
        move-diff (board/updates-for-move board move current-player)
        timer-diff (updates-for-timers state move current-player)
        state-diff (updates-for-state (:board move-diff) state)]
    ; It's important to do one swap! for all the turn's
    ; changes so that we don't have a flashy UI.
    (swap! state merge move-diff timer-diff state-diff)))

(defn update-state [state move]
  (log/debug "update-state:" state "move:" move)
  (if-let [error (check-for-error state move)]
    (update-state-for-error state error)
    (update-state-for-valid-move state move)))

(defn get-player-move [state player]
  (util/time-limited (:max-turn-time @state) :timeout
    (player/get-move player state)))

(defn get-timed-player-move [state player]
  (let [start (System/currentTimeMillis)
        move (get-player-move state player)
        stop (System/currentTimeMillis)]
    (log/debug "Got player move:" move)
    (merge move
           {:time-elapsed (- stop start)})))

(defn get-next-move [state player]
  (util/exec-and-wait (:min-turn-time @state) get-timed-player-move state player))

(defn get-current-player [state player-one player-two]
  (if (= :player-one (:current-player @state))
    player-one
    player-two))

(defn running? [state]
  (:running @state))

(defn start-game [state player-one player-two]
  (log/debug "Starting game!")
  (st/game-starting state)
  (while (running? state)
    (let [player (get-current-player state player-one player-two)
          move (get-next-move state player)]
      (update-state state move))))

(defstate game :start (start-game state player-one player-two))