(ns connect-four.server.board-test
  (:require [connect-four.server.board :refer :all]
            [clojure.test :refer :all]))

(deftest test-serialize-board
  (is (= "[[0,0]]" (serialize-board [[0 0]]))))

(deftest test-serialize-player
  (is (= "player-one" (serialize-player :player-one))))

(deftest test-valid-board?
  (is (not (valid-board? nil)))
  (is (not (valid-board? [])))
  (is (not (valid-board? [[]])))
  (is (not (valid-board? [[0 0 0 0 0 0 0]])))
  (is (not (valid-board? [[0 0 0 0 0 0]
                          [0 0 0 0 0 0]
                          [0 0 0 0 0 0]
                          [0 0 0 0 0 0]
                          [0 0 0 0 0 0]
                          [0 0 0 0 0 0]])))
  (is (not (valid-board? [[0 0 0 0 0 0 0]
                          [0 0 0 0 0 0 0]
                          [0 0 0 0 0 0 0]
                          [0 0 0 0 0 0 0]
                          [0 3 4 0 0 0 0]
                          [0 0 0 0 0 0 0]])))
  (is (valid-board? [[0 0 0 0 0 0 0]
                     [0 0 0 0 0 0 0]
                     [0 0 0 0 0 0 0]
                     [0 0 0 0 0 0 0]
                     [0 0 0 0 0 0 0]
                     [0 0 1 2 0 0 0]])))

(deftest test-update-status-for-piece
  (is (= (update-status-for-piece {:player nil :count 0} 1)
         {:player 1 :count 1}))
  (is (= (update-status-for-piece {:player 1 :count 1} 1)
         {:player 1 :count 2}))
  (is (= (update-status-for-piece {:player 1 :count 1} 2)
         {:player 2 :count 1})))

(deftest test-contains-winner?
  (is (contains-winner? [1 1 1 1 0 0 0]))
  (is (contains-winner? [0 1 1 1 1]))
  (is (contains-winner? [0 1 1 1 1 0 0]))
  (is (contains-winner? [0 0 2 2 2 2 0]))
  (is (not (contains-winner? [0 0 0 0 0 0]))))


(def valid-moves-board [
 [0 2 0 0 5 0 7]
 [1 2 0 0 5 0 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
])

(deftest test-valid-moves
  (is (= [{:column 0} {:column 2} {:column 3} {:column 5}]
         (valid-moves valid-moves-board))))

(def test-board-1 [
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
 [1 2 3 4 5 6 7]
])

(deftest test-get-horizontal-row
  (is (= [1 2 3 4 5 6 7] (get-horizontal-row test-board-1 0)))
  (is (= [1 2 3 4 5 6 7] (get-horizontal-row test-board-1 5))))

(deftest test-get-vertical-row
  (is (= [1 1 1 1 1 1] (get-vertical-row test-board-1 0)))
  (is (= [7 7 7 7 7 7] (get-vertical-row test-board-1 6))))

(def test-board-2 [
 [1 1 1 1 1 1 1]
 [2 2 2 2 2 2 2]
 [3 3 3 3 3 3 3]
 [4 4 4 4 4 4 4]
 [5 5 5 5 5 5 5]
 [6 6 6 6 6 6 6]
])

(deftest test-get-rising-row
  (is (= [1] (get-rising-row test-board-2 0)))
  (is (= [2 1] (get-rising-row test-board-2 1)))
  (is (= [3 2 1] (get-rising-row test-board-2 2)))
  (is (= [6 5 4 3 2 1] (get-rising-row test-board-2 5)))
  (is (= [6 5 4 3 2 1] (get-rising-row test-board-2 6)))
  (is (= [6 5 4 3 2] (get-rising-row test-board-2 7)))
  (is (= [6] (get-rising-row test-board-2 11))))

(deftest test-get-descending-row
  (is (= [6] (get-descending-row test-board-2 0)))
  (is (= [5 6] (get-descending-row test-board-2 1)))
  (is (= [4 5 6] (get-descending-row test-board-2 2)))
  (is (= [1 2 3 4 5 6] (get-descending-row test-board-2 5)))
  (is (= [1 2 3 4 5 6] (get-descending-row test-board-2 6)))
  (is (= [1 2 3 4 5] (get-descending-row test-board-2 7)))
  (is (= [1] (get-descending-row test-board-2 11))))

(deftest test-tied?
  (let [board [[2 1 2 2 1 2 2]
               [1 2 1 2 2 2 1]
               [2 1 2 1 2 1 2]
               [2 1 1 1 2 1 2]
               [1 2 1 2 1 1 1]
               [1 1 2 1 1 2 2]]]
    (is (tied? board)))
  (let [board [[0 0 0 0 0 0 0]
               [0 0 0 0 0 1 0]
               [0 0 0 0 0 1 1]
               [0 0 0 0 0 1 1]
               [0 0 0 0 0 1 1]
               [0 0 0 0 0 2 0]]]
      (is (not (tied? board)))))

(deftest test-won?
  (testing "vertical line"
    (let [board [[0 0 0 0 0 0 0]
                 [0 0 0 0 0 1 0]
                 [0 0 0 0 0 1 1]
                 [0 0 0 0 0 1 1]
                 [0 0 0 0 0 1 1]
                 [0 0 0 0 0 2 0]]]
      (is (won? board))))
  (testing "horizontal line"
    (let [board [[0 0 0 0 0 0 0]
                 [0 0 0 0 0 0 0]
                 [0 0 0 0 0 0 1]
                 [0 0 0 0 0 0 1]
                 [0 0 0 1 1 1 1]
                 [0 0 0 2 2 2 0]]]
      (is (won? board))))
  (testing "rising line"
    (let [board [[0 0 0 0 0 0 0]
                 [0 0 0 0 0 0 0]
                 [0 0 0 0 0 1 1]
                 [0 0 0 0 1 0 1]
                 [0 0 0 1 1 0 1]
                 [0 0 1 2 2 2 0]]]
      (is (won? board))))
  (testing "descending line"
    (let [board [[0 0 0 0 0 0 0]
                 [0 0 0 0 0 0 0]
                 [2 0 0 0 0 1 1]
                 [0 2 0 0 1 0 1]
                 [0 0 2 1 1 0 1]
                 [0 0 0 2 2 2 0]]]
      (is (won? board))))
  (testing "not quite"
    (let [board [[0 0 0 0 0 0 0]
                 [0 0 0 0 0 0 0]
                 [1 0 0 0 0 1 1]
                 [0 2 0 0 1 0 1]
                 [0 0 2 1 1 0 1]
                 [0 0 0 2 2 2 0]]]
      (is (not (won? board))))))

(deftest test-updates-for-move
  (let [board [[0 0 0 0 0 0 0]
               [0 0 0 0 0 1 1]
               [0 0 0 0 0 1 1]
               [0 0 0 1 0 1 1]
               [0 0 2 1 0 1 1]
               [0 1 2 1 0 1 1]]]

    (let [diff (updates-for-move board {:column 0} :player-one)]
      (is (= (:board diff) [[0 0 0 0 0 0 0]
                            [0 0 0 0 0 1 1]
                            [0 0 0 0 0 1 1]
                            [0 0 0 1 0 1 1]
                            [0 0 2 1 0 1 1]
                            [1 1 2 1 0 1 1]]))
      (is (= (:last-move diff)
             {:column 0 :row 5 :player :player-one})))

    (let [diff (updates-for-move board {:column 1} :player-two)]
      (is (= (:board diff) [[0 0 0 0 0 0 0]
                            [0 0 0 0 0 1 1]
                            [0 0 0 0 0 1 1]
                            [0 0 0 1 0 1 1]
                            [0 2 2 1 0 1 1]
                            [0 1 2 1 0 1 1]]))
      (is (= (:last-move diff)
             {:column 1 :row 4 :player :player-two}))
    )))

(deftest test-valid-and-invalid-move?
  (let [board [[0 0 0 0 0 0 1]
               [0 0 0 0 0 1 1]
               [0 0 0 0 0 1 1]
               [0 0 0 0 0 1 1]
               [0 0 0 0 0 1 1]
               [0 0 0 0 0 1 1]]]

    (testing "is invalid if column field is missing"
      (is (invalid-move? board {})))

    (testing "is invalid if column field is invalid"
      (is (invalid-move? board {:column :heyya}))
      (is (invalid-move? board {:column -1}))
      (is (invalid-move? board {:column 7})))

    (testing "is invalid if column is full"
      (is (invalid-move? board {:column 6})))

    (testing "is valid if column is valid and not full"
      (is (valid-move? board {:column 0}))
      (is (valid-move? board {:column 5})))))