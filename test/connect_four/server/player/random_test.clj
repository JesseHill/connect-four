(ns connect-four.server.player.random-test
  (:require [clojure.test :refer :all]
            [connect-four.server.player :refer :all]
            [connect-four.server.player.random :refer :all]))

(def board [
 [0 2 3 0 5 6 7]
 [1 2 3 0 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
])

(deftest test-get-move
  (let [player (->Player :player-one {})
        move (get-move player (atom {:board board}))]
    (is (some #{move} [{:column 0} {:column 3}]))))
