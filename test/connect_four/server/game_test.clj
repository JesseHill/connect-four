(ns connect-four.server.game-test
  (:require [clojure.test :refer :all]
            [connect-four.server.game :refer :all]
            [connect-four.server.state :as st]
            [connect-four.server.util :as util]
            [connect-four.server.player :as player]
            [connect-four.server.board :as board]
            [org.senatehouse.expect-call :refer :all]))

(deftest test-alternate-player
  (is (= :player-two (alternate-player (atom {:current-player :player-one}))))
  (is (= :player-one (alternate-player (atom {:current-player :player-two})))))

(deftest test-updates-for-current-player
  (is (= {:current-player :player-two}
         (updates-for-current-player (atom {:current-player :player-one})))))

(deftest test-updates-for-game-won
  (is (= {:status "player.won"
          :running false
          :winner :player-one}
         (updates-for-game-won (atom {:current-player :player-one})))))

(deftest test-updates-for-game-tied
  (is (= {:status "game.tied"
          :running false
          :winner :tie}
         (updates-for-game-tied (atom {})))))

(deftest test-updates-for-player-error
  (is (= {:status "player.error"
          :error "Some error"
          :running false
          :winner :player-two}
         (updates-for-player-error (atom {:current-player :player-one}) "Some error"))))

(deftest test-updates-for-state
  (expect-call [(board/won? [:board] true)
                (updates-for-game-won [:state] :updates)]
    (is (= :updates (updates-for-state :board :state))))
  (expect-call [(board/won? [:board] false)
                (board/tied? [:board] true)
                (updates-for-game-tied [:state] :updates)]
    (is (= :updates (updates-for-state :board :state))))
  (expect-call [(board/won? [:board] false)
                (board/tied? [:board] false)
                (updates-for-current-player [:state] :updates)]
    (is (= :updates (updates-for-state :board :state)))))

(deftest test-updates-for-timers
  (is (= {:player-one-timer 20}
         (updates-for-timers (atom {:player-one-timer 10}) {:time-elapsed 10} :player-one))))

(deftest test-check-for-error
  (let [state (atom {:board :board})]
    (is (= :some-error (check-for-error {} {:error :some-error})))
    (expect-call [(board/invalid-move? [:board {:column 1}] true)]
      (is (= :invalid-move (check-for-error state {:column 1}))))
    (expect-call [(board/invalid-move? [:board {:column 1}] nil)]
      (is (= nil (check-for-error state {:column 1}))))))

(deftest test-update-state-for-error
  (let [state (atom {:board :boardy :current-player :player-one})]
    (update-state-for-error state :timed-out)
    (is (= {:status "player.error"
            :error :timed-out
            :running false
            :winner :player-two
            :board :boardy
            :current-player :player-one}
           @state))))

(deftest test-update-state-for-valid-move
  (testing "updates timers"
    (let [state (atom {:board :boardy :current-player :player-one :player-one-timer 10})]
      (expect-call [(board/updates-for-move [:boardy {} :player-one] {:board :boardy1})
                    (updates-for-timers [state {:time-elapsed 10} :player-one] {:player-one-timer 20})
                    (updates-for-state [:boardy1 state] {:current-player :player-two})]
        (update-state-for-valid-move state {:time-elapsed 10})
        (is (= {:board :boardy1
                :current-player :player-two
                :player-one-timer 20}
               @state)))))
  (testing "with a winning move"
    (let [state (atom {:board :boardy :current-player :player-one :player-one-timer 0})]
      (expect-call [(board/updates-for-move [:boardy {} :player-one] {:board :boardy1})
                    (updates-for-timers [state {:time-elapsed 10} :player-one] {:player-one-timer 10})
                    (updates-for-state [:boardy1 state] {:running false})
                    (:never updates-for-current-player)]
        (update-state-for-valid-move state {:time-elapsed 10})
        (is (= {:board :boardy1
                :running false
                :player-one-timer 10
                :current-player :player-one}
               @state))))))

(deftest test-update-state
  (testing "with error"
    (let [state (atom {:board :boardy :current-player :player-one})]
      (expect-call [(check-for-error [state {:error :timed-out}] :timed-out)
                    (update-state-for-error [state :timed-out])]
        (update-state state {:error :timed-out}))))
  (testing "with valid move"
    (let [state (atom {:board :boardy :current-player :player-one})]
      (expect-call [(check-for-error [state :valid-move] nil)
                    (update-state-for-valid-move [state :valid-move])]
        (update-state state :valid-move)))))

(deftest test-get-player-move
  (let [state (atom {:max-turn-time 1000})]
    (expect-call (player/get-move [:p1 state])
      (get-player-move state :p1))))

(deftest test-get-player-move-timeout
  (let [state (atom {:max-turn-time 1000})]
    (expect-call (player/get-move [:p1 state])
      (get-player-move state :p1))))

(deftest test-get-next-move
  (let [state (atom {:min-turn-time 1000})]
    (expect-call (util/exec-and-wait [1000 get-timed-player-move state :player-one])
      (get-next-move state :player-one))))

(deftest test-get-current-player
  (let [state (atom {:current-player :player-one})]
    (is (= :p1 (get-current-player state :p1 :p2))))
  (let [state (atom {:current-player :player-two})]
    (is (= :p2 (get-current-player state :p1 :p2)))))

(deftest test-running?
  (is (running? (atom {:running true})))
  (is (not (running? (atom {:running false})))))

(deftest test-start-game
  (testing "start-game behaves as expected"
    (let [state (atom {})]
      (expect-call [(:do st/game-starting [state])
                    (running? [state] true)
                    (get-current-player [state :player-one :player-two] :player-one)
                    (get-next-move [state :player-one] {:column 2})
                    (update-state [state {:column 2}])
                    (running? [state] false)]
        (start-game state :player-one :player-two)))))