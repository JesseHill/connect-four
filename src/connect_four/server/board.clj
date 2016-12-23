(ns connect-four.server.board
  (:require [cheshire.core :refer [generate-string parse-string parse-stream]]
            [clojure.walk :as walk]
            [clojure.tools.logging :as log]))

(defn serialize-board [board]
  (generate-string board))

(defn serialize-player [player]
  (name player))

(defn valid-board? [board]
  (when (and (vector? board) (= 6 (count board)))
    (not (some (fn [row]
                 (or (not (vector? row))
                     (not= 7 (count row))
                     (some (fn [val] (not (some #{val} [0 1 2]))) row)))
               board))))

(defn valid-player? [player]
  (some #{player} [:player-one :player-two]))

(defn parse-board [value parse]
  (try
    (vec (parse value))
  (catch Exception e
    (log/debug "Caught exception processing board:" e))))

(defn parse-board-string [value]
  (parse-board value parse-string))

(defn parse-board-stream [value]
  (parse-board value parse-stream))

(defn parse-player-string [value]
  (let [player (keyword value)]
    (when (valid-player? player)
      player)))

(defn update-status-for-piece [status piece]
  (if (= 0 piece)
    {:player nil :count 0}
    (if (= (:player status) piece)
      (let [updated (update status :count inc)]
        (if (= 4 (:count updated))
          (reduced updated)
          updated))
      {:player piece :count 1})))

(defn contains-winner? [row]
  (let [status (reduce update-status-for-piece {:player nil :count 0} row)]
    (when (= 4 (:count status))
      true)))

(defn get-horizontal-row [board index]
  (nth board index))

(defn get-vertical-row [board index]
  (reduce
    (fn [vertical-row horizontal-row]
      (conj vertical-row (nth horizontal-row index)))
      []
      board))

(defn get-value [board x y]
  (-> board (nth y) (nth x)))

(defn build-rising-row-indexes [x y]
  (map vector (range x 7) (range y -1 -1)))

(defn build-descending-row-indexes [x y]
  (map vector (range x 7) (range y 6)))

(defn get-rising-row [board index]
  (let [y (min index 5)
        x (max (- index 5) 0)
        indexes (build-rising-row-indexes x y)]
    (reduce
      (fn [acc [x y]] (conj acc (get-value board x y)))
      []
      indexes)))

(defn get-descending-row [board index]
  (let [y (max 0 (- 5 index))
        x (max 0 (- index 5))
        indexes (build-descending-row-indexes x y)]
    (reduce
      (fn [acc [x y]] (conj acc (get-value board x y)))
      []
      indexes)))

(defn check-rows [board get-row-fn row-count]
  (some (fn [i] (contains-winner? (get-row-fn board i))) (range row-count)))


(defn player-symbol [player]
  (case player
    :player-one 1
    :player-two 2))

(defn valid-move? [board move]
  (when-let [column (:column move)]
    (when (some #{column} (range 7))
      (let [top-value (-> board first (nth column))]
        (= 0 top-value)))))

(defn valid-moves [board]
  (filter (fn [move] (valid-move? board move))
          (map (fn [i] {:column i}) (range 7))))

(defn invalid-move? [board move]
  (not (valid-move? board move)))

(defn won? [board]
  (or (check-rows board get-horizontal-row 6)
      (check-rows board get-vertical-row 7)
      (check-rows board get-rising-row 12)
      (check-rows board get-descending-row 12)))

(defn tied? [board]
  (and (not (won? board))
       (empty? (valid-moves board))))

(defn last-empty-row [board column]
  ; 1 - reverse board so that we're looking at it from bottom to top
  ; 2 - zip the rows with an index
  ; 3 - look for the first index where the column value is 0
  (let [reversed (reverse board)
        indexes (range 5 -1 -1)
        reversed-with-indexes (map vector indexes reversed)]
    (some (fn [[index row]]
            (when (= 0 (nth row column))
              index))
          reversed-with-indexes)))

(defn updates-for-move [board move player]
  (when (valid-move? board move)
    (let [sym (player-symbol player)
          column (:column move)
          row (last-empty-row board column)]
      {:board (assoc-in board [row column] sym)
       :last-move {:column column :row row :player player}})))
