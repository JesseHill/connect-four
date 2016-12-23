(ns system.games-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [connect-four.server.game :refer :all]
            [connect-four.server.remote-server :refer [server]]
            [connect-four.server.state :as st :refer [state]]
            [connect-four.server.player :as player]
            [connect-four.server.player.robot :as robot]
            [connect-four.server.text-ui]
            [connect-four.server.player.state :refer [player-one player-two]]))

(defn next-move [moves state]
  (let [move (first @moves)]
    (swap! moves rest)
    {:column move}))

(defn playback-strategy [coll]
  (let [moves (atom coll)]
    {:fn (partial next-move moves)}))

(defn build-robot [player moves]
  (let [robot (robot/->Player player (atom moves))]
    (player/initialize robot)
    robot))

(defn play-game [p1-moves p2-moves expected-winner expected-board]
  (mount/start-with {#'server nil
                     #'state (st/build-state {:min-turn-time 1
                                              :max-turn-time 1000})
                     #'player-one (build-robot :player-one p1-moves)
                     #'player-two (build-robot :player-two p2-moves)})
  (println (:board @state))
  (is (= expected-winner (:winner @state)))
  (is (= expected-board (:board @state)))

  (mount/stop))

(deftest run-sample-games
  (testing "The game should finish if a player one returns a nil move"
    (play-game [2]
               [3]
               :player-two
               [[0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 1 2 0 0 0]]))
  (testing "The game should finish if a player one returns an invalid move"
    (play-game [2 12]
               [3]
               :player-two
               [[0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 1 2 0 0 0]]))
  (testing "The game should finish if a player one returns a move for a full column"
    (play-game [0 0 0 0]
               [0 0 0]
               :player-two
               [[2 0 0 0 0 0 0]
                [1 0 0 0 0 0 0]
                [2 0 0 0 0 0 0]
                [1 0 0 0 0 0 0]
                [2 0 0 0 0 0 0]
                [1 0 0 0 0 0 0]]))
  (testing "The game should finish if a player two returns a nil move"
    (play-game [2 3]
               [3]
               :player-one
               [[0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 1 0 0 0]
                [0 0 1 2 0 0 0]]))
  (testing "The game should finish if a player two times out"
    (play-game [2 3]
               [3 :timeout]
               :player-one
               [[0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 0 0 0 0]
                [0 0 0 1 0 0 0]
                [0 0 1 2 0 0 0]]))
  (testing "The game should finish with a tie in this case"
    (play-game [0 1 3 4 0 2 4 6 5 5 1 2 5 6 3 3 1 0 1 2 4]
               [2 5 6 1 3 0 4 6 6 4 0 2 5 6 5 4 1 0 3 2 3]
               :tie
               [[2 1 2 2 1 2 2]
                [1 2 1 2 2 2 1]
                [2 1 2 1 2 1 2]
                [2 1 1 1 2 1 2]
                [1 2 1 2 1 1 1]
                [1 1 2 1 1 2 2]]))

)