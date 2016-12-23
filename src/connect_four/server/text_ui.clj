(ns connect-four.server.text-ui
  (:require [mount.core :as mount :refer [defstate]]
            [connect-four.server.state :refer [state] :as st]
            [clojure.tools.logging :as log]))

(defn log-last-move [state]
  (let [move (:last-move state)]
    (log/info
      (st/current-player-name state)
      "moved - column:"
      (:column move)
      "row:"
      (:row move))
    (log/info (:board state))))

(defn format-time [millis]
  (str millis " milliseconds"))

(defn print-tie-info [state]
  (log/info "The game ended in a tie!")
  (log/info (st/player-name state :player-one) "took" (format-time (st/player-timer state :player-one)))
  (log/info (st/player-name state :player-two) "took" (format-time (st/player-timer state :player-two)))
  (log/info "The tie breaker goes to:" (st/player-name state (st/tie-breaker-winner state))))

(defn print-error [state]
  (let [name (st/current-player-name state)]
    (log/info "An error occured:" name
      (case (:error state)
        :timeout "timed out."
        :invalid-move "returned an invalid move."
        "caused an error to occur."))))

(defn print-winner-info [state]
  (when (:error state) (print-error state))
  (let [name (st/player-name state (:winner state))]
    (log/info name "won!")))

(defn log-winner [state]
  (if (= :tie (:winner state))
    (print-tie-info state)
    (print-winner-info state)))

(defn start-ui []
  (add-watch
    state
    :text-ui
    (fn [_ _ old-state new-state]
      (when (st/different? :last-move old-state new-state)
        (log-last-move new-state))
      (when (st/different? :winner old-state new-state)
        (log-winner new-state)))))

(defn stop-ui []
  (remove-watch state :text-ui))

(defstate text-ui :start (start-ui)
                  :stop (stop-ui))