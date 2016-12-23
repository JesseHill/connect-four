(ns connect-four.server.player.state-test
  (:require [clojure.test :refer :all]
            [mount.core :as mount]
            [connect-four.server.player :refer :all]
            [connect-four.server.player.state :refer :all]))

(deftest build-player-test
  (testing "correct player is created"
    (let [one (build-player :player-one {})
          two (build-player :player-two {})]
      (is (= :player-one (:player one)))
      (is (= :player-two (:player two)))))

  (testing "correct type of player is created"
    (let [local (build-player :player-one {:type :local})
          remote (build-player :player-one {:type :remote})
          robot (build-player :player-one {:type :robot})
          random (build-player :player-one {})]
      (is (= :local (:type (initialize local))))
      (is (= :remote (:type (initialize remote))))
      (is (= :robot (:type (initialize robot))))
      (is (= :random (:type (initialize random)))))))

(deftest player-state-test
  (testing "options are handled correctly"
    (-> (mount/only #{#'player-one #'player-two})
      (mount/with-args {:player-one {:type :local} :player-two {:type :random}})
      mount/start)
    (try
      (is (= :local (:type (initialize player-one))))
      (is (= :random (:type (initialize player-two))))
      (finally
        (mount/stop)))))