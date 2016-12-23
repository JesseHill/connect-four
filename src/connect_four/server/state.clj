(ns connect-four.server.state
  (:require [mount.core :as mount :refer [defstate]]
            [clojure.tools.logging :as log]))

(defn build-state [options]
  (atom {:status "Connected ..."
         :running false
         :winner nil
         :current-player nil
         :last-move nil
         :moves 0
         :clients []
         :player-one-name (or (-> options :player-one :name) "Team One")
         :player-two-name (or (-> options :player-two :name) "Team Two")
         :player-one-moves []
         :player-two-moves []
         :player-one-timer 0
         :player-two-timer 0
         :board [
           [0 0 0 0 0 0 0]
           [0 0 0 0 0 0 0]
           [0 0 0 0 0 0 0]
           [0 0 0 0 0 0 0]
           [0 0 0 0 0 0 0]
           [0 0 0 0 0 0 0]
         ]
         :min-turn-time (:min-turn-time options)
         :max-turn-time (:max-turn-time options)
         :wait-for-client (:wait-for-client options)
         }))

(defn player-name [state player]
  (case player
    :player-one (:player-one-name state)
    :player-two (:player-two-name state)))

(defn player-timer-key [player]
  (case player
    :player-one :player-one-timer
    :player-two :player-two-timer))

(defn player-timer [state player]
  (get state (player-timer-key player)))

(defn current-player-name [state]
  (player-name state (:current-player state)))

(defn tie-breaker-winner [state]
  (if (<= (player-timer state :player-one)
          (player-timer state :player-two))
    :player-one
    :player-two))

(defn different? [key state-1 state-2]
  (not= (key state-1) (key state-2)))

(defn clients-connected [state]
  (if-let [clients (:clients state)]
    (count clients)
    0))

(defn client-connected [state client]
  (swap! state update-in [:clients] conj client))

(defn client-disconnected [state client]
  (swap! state update-in [:clients] (fn [clients] (remove #{client} clients))))

(defn wait []
  (log/info "Waiting for a client connection ...")
  (Thread/sleep 1000))

(defn game-starting [state]
  (while (and (:wait-for-client @state)
              (<= (clients-connected @state) 0))
    (wait))
  (swap! state merge {:status "game.started"
                      :running true
                      :current-player :player-one}))

(defstate state :start (build-state (mount/args)))