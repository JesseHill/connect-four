(ns connect-four.server.state-test
  (:require [clojure.test :refer :all]
            [connect-four.server.state :refer :all]
            [clojure.tools.logging :as log]
            [mount.core :as mount]
            [org.senatehouse.expect-call :refer :all]))

(deftest test-player-name
  (-> (mount/only #{#'state})
    (mount/with-args {:player-one {:name "Pistolero"} :player-two {:name "Bluefinger"}})
    mount/start)
  (try
    (is (= "Pistolero" (player-name @state :player-one)))
    (is (= "Bluefinger" (player-name @state :player-two)))
    (finally
      (mount/stop))))

(deftest test-current-player-name
  (let [state {:player-one-name "Pistolero"
               :player-two-name "Bluefinger"
               :current-player :player-two}]
    (is (= "Bluefinger" (current-player-name state)))))

(deftest test-player-timer-key
  (is (= :player-one-timer (player-timer-key :player-one)))
  (is (= :player-two-timer (player-timer-key :player-two))))

(deftest test-player-timer
  (is (= 10 (player-timer {:player-one-timer 10} :player-one)))
  (is (= 20 (player-timer {:player-two-timer 20} :player-two))))

(deftest test-different?
  (let [state-1 {:winner nil}
        state-2 {:winner :player-one}]
    (is (different? :winner state-1 state-2))))

(deftest test-client-connected
  (let [state (atom {:clients []})]
    (client-connected state :client)
    (is (= [:client] (:clients @state)))))

(deftest test-client-disconnected
  (let [state (atom {:clients [:client]})]
    (client-disconnected state :client)
    (is (= [] (:clients @state)))))

(deftest test-game-starting
  (let [state (atom {:wait-for-client true})]
    (expect-call [(clients-connected [_] 0)
                  (wait [])
                  (clients-connected [_] 1)
                  (:never wait)]
      (game-starting state))
    (is (= @state {:wait-for-client true
                   :status "game.started"
                   :running true
                   :current-player :player-one}))))