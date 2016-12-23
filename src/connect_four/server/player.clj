(ns connect-four.server.player)

(defprotocol Player
  "A simple protocol representing a player."
  (initialize [this] "Run any necessary initialization.")
  (get-move [this game-state] "Get the next move for the player given the game-state."))