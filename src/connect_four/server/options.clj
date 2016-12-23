(ns connect-four.server.options
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.string :as string]))

(defn file-exists? [file-path]
  (when (string? file-path)
    (try
      (.exists (clojure.java.io/as-file file-path))
      (catch Exception e false))))

(defn http-uri? [uri]
  (when (string? uri)
    (re-matches #"http://.*" uri)))

(defn read-location [location]
  (if (and (string? location) (re-matches #"^\[[\d\s]+\]$" location))
    (try
      (read-string location)
      (catch Exception e location))
    location))

(defn robot-opts? [data]
  (vector? (read-location data)))

(defn validate-player-location [location]
  (or (file-exists? location)
      (http-uri? location)
      (robot-opts? location)
      (= "random" location)))

(defn valid-remote-server? [executable]
  (file-exists? executable))

(defn valid-timeout? [arg]
  (and (integer? arg) (pos? arg)))

(def cli-options
  [
   [nil "--p1-name NAME" "Player one's team name"
    :default "Player One"]
   [nil "--p1-location LOCATION" "Player one's uri or executable"
    :validate [validate-player-location "Invalid player location"]]

   [nil "--p2-name NAME" "Player two's team name"
    :default "Player Two"]
   [nil "--p2-location LOCATION" "Player two's uri or executable"
    :validate [validate-player-location "Invalid player location"]]

   ["-r" "--remote-server EXE" "Start a server for a remote player"
    :validate [valid-remote-server? "Could not validate the remote server options"]]
   ["-p" "--remote-server-port PORT" "Port number for a remote server"
    :default 6000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]

   ["-w" "--wait-for-client" "Wait for a web client to connect before starting game"]

   ["-m" "--min-turn-time MILLIS" "Minimum amount of time to wait between turns."
    :default 2000
    :parse-fn #(Integer/parseInt %)
    :validate [valid-timeout? "Must be a positive integer."]]

   ["-x" "--max-turn-time MILLIS" "Maximum amount of time to allow an AI for a turn."
    :default 15000
    :parse-fn #(Integer/parseInt %)
    :validate [valid-timeout? "Must be a positive integer."]]

   ["-h" "--help"]])

(defn parse [args]
  (parse-opts args cli-options))

(defn usage [options-summary]
  (string/join
    \newline
    ["This application is a board for dueling Connect Four AI implementations."
     ""
     "You can specify player options for each of the two players."
     ""
     "The player can be one of three types:"
     "random - the game will make a random valid move for the player"
     "local - the game will invoke a local executable to determine the next move"
     "remote - the game will invoke a remote player determine the next move"
     ""
     "To specify a random player, just leave the --p1-location blank or pass the random string:"
     "--p1-location random"
     ""
     "To specify a local player, provide an executable like:"
     "--p1-location ./player.sh"
     ""
     "To specify a remote player, specify the player's uri like:"
     "--p1-location http://192.168.1.200:6000"
     ""
     "To host a remote player, specify the executable and optionally set a port:"
     "--remote-server ./player.sh --remote-server-port 6000"
     ""
     "The game will log moves to the console and run a webserver on port 5000 for a UI."
     ""
     "Pass the -w option in order to have the server wait for a client connection before starting the game."
     ""
     "The game will by default time out if a player has not responeded within 15 seconds"
     "You can change this with the -x arg (-x 20000 for 20 seconds)."
     ""
     "Usage:"
     "java -jar connect-four.jar"
     "java -jar connect-four.jar --p1-name \"Thundering Wallabies\" --p2-name \"Rogue Sloths\""
     "java -jar connect-four.jar --p1-location p1.sh --p2-location http://10.138.123.79:6000"
     ""
     "Options:"
     options-summary
     ""]))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn invalid-options-msg [summary]
  (log/info "Sorry, some required options were missing.")
  (newline)
  (usage summary))

(defn exit [status msg]
  (log/info msg)
  (System/exit status))

(defn player-type [location]
  (cond
    (file-exists? location) :local
    (http-uri? location) :remote
    (vector? location) :robot
    :default :random))

(defn player-options [key name location]
  (let [loc (read-location location)]
    {key {:name name
          :type (player-type loc)
          :location loc}}))

(defn update-players [options]
  (merge options
         (player-options :player-one (:p1-name options) (:p1-location options))
         (player-options :player-two (:p2-name options) (:p2-location options))))

(defn parse-options [args]
  (let [{:keys [options errors summary] :as parsed} (parse args)]
    (if (some? errors)
      parsed
      (assoc parsed :options (update-players options)))))

(defn parse-or-exit [args]
  (let [{:keys [options errors summary]} (parse-options args)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    options))