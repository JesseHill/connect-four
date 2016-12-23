(ns connect-four.random-player.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [connect-four.server.util :as util]
            [connect-four.server.board :as board]
            [connect-four.server.player :as player]
            [connect-four.server.player.random :as random])
  (:gen-class))

(defn contains-keys? [m & ks]
  (every? true? (map #(contains? m %) ks)))

(def cli-options
  [["-b" "--board BOARD" "JSON representation of the board."
    :parse-fn board/parse-board-string
    :validate [board/valid-board? "Could not parse the given board."]]
   ["-p" "--player PLAYER" "The player to use (white or black)."
    :parse-fn board/parse-player-string
    :validate [board/valid-player? "Invalid player symbol (must be white or black)."]]
   ["-t" "--time MILLIS" "The maximum time allowed for a move."]
   ["-h" "--help"]])

(defn invalid-options? [options]
  (not (contains-keys? options :board :player)))

(defn usage [summary]
  (->> ["This application is an AI for a Connect Four game. It picks a valid move at random."
        ""
        "Usage: -b BOARD -p PLAYER"
        "Where BOARD is a JSON representation of the board and the player specifies \"player-one\" or \"player-two\""
        "Options:"
        summary
        ""]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn invalid-options-msg [summary]
  (log/info "Sorry, some required options were missing.")
  (newline)
  (usage summary))

(defn log-and-exit [status msg]
  (log/info msg)
  (System/exit status))

(defn get-move[options]
  (let [board (:board options)
        player (:player options)
        state (atom {:board board})]
    (player/get-move (random/->Player player {}) state)))

(defn -main
  "Main needs to:
    - Validate the given args.
    - Parse a board from the board JSON.
    - Invoke the player with the board to return a move."
  [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (log-and-exit 0 (usage summary))
      errors (log-and-exit 1 (error-msg errors))
      (invalid-options? options) (log-and-exit 1 (invalid-options-msg summary))
      :else (System/exit (:column (get-move options))))))
