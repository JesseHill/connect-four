(ns connect-four.server.remote-server-test
  (:require [connect-four.server.remote-server :refer :all]
            [connect-four.server.transport :as t]
            [connect-four.server.player :as p]
            [connect-four.server.player.local :as l]
            [org.httpkit.server :as http]
            [clojure.test :refer :all]
            [org.senatehouse.expect-call :refer :all]))

(def board [
  [0 1 1 1 1 1 1]
  [1 1 1 1 1 1 1]
  [1 1 1 1 1 1 1]
  [1 1 1 1 1 1 1]
  [1 1 1 1 1 1 1]
  [1 1 1 1 1 1 1]
])

(deftest test-handle-request
  (let [data {:board board
              :current-player :player-one
              :max-turn-time 1000}
        serialized (t/serialize data)
        request {:body serialized}]
  (expect-call [(l/->Player [:player-one {:location :exe}] :player)
                (p/get-move [:player _] {:column 1})]
    (let [response (handle-request :exe request)
          deserialized (t/deserialize (:body response))]
      (is (= 200 (:status response)))
      (is (= {:column 1} deserialized))))))

(deftest test-build-api
  (expect-call (partial [handle-request :exe])
    (build-api :exe)))

(deftest test-start-server
  (expect-call [(build-api [:exe] :fn)
                (http/run-server [:fn {:port :remote-port}])]
    (start-server {:remote-server :exe :remote-server-port :remote-port})))