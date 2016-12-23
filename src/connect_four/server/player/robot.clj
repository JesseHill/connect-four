(ns connect-four.server.player.robot
  (:require [connect-four.server.player :as p]))

(defrecord Player [player moves]
  p/Player
  (initialize [this]
    {:player (:player this)
     :type :robot})
  (get-move [this state]
    (let [moves (:moves this)
          move (first @moves)]
      ; (Thread/sleep (rand-int 10))
      (swap! moves rest)
      (if (= :timeout move)
        (Thread/sleep 1000000)
        {:column move}))))