(ns connect-four.server.player.random
  (:require [connect-four.server.player :as p]
            [connect-four.server.board :as b]
            [clojure.tools.logging :as log]))

(defrecord Player [player options]
  p/Player
  (initialize [this]
    (log/debug "Building random player:" (:player this))
    {:player (:player this)
     :type :random})
  (get-move [this state]
    (let [moves (b/valid-moves (:board @state))]
      (if (seq moves)
        (rand-nth moves)
        {:error "No valid moves!"}))))