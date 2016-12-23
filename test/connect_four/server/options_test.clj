(ns connect-four.server.options-test
  (:require [clojure.test :refer :all]
            [connect-four.server.options :refer :all]
            [org.senatehouse.expect-call :refer :all]))

(deftest options-test
  (testing "player one options are handled correctly"
    (let [parsed (parse-options ["--p1-name" "Yo" "--p1-location" "/bin/echo"])]
      (is (= (-> parsed :options :player-one)
             {:type :local
              :location "/bin/echo"
              :name "Yo"})))
     (let [parsed (parse-options ["--p1-location" "nope"])]
       (is (some? (:errors parsed))))
     (let [parsed (parse-options ["--p1-location" "[1 2]"])]
       (is (= :robot (-> parsed :options :player-one :type)))
       (is (= [1 2] (-> parsed :options :player-one :location)))))

  (testing "player two options are handled correctly"
    (let [parsed (parse-options ["--p2-location" "http://some:address"])]
      (is (= (-> parsed :options :player-two)
             {:type :remote
              :location "http://some:address"
              :name "Player Two"})))
     (let [parsed (parse-options [])]
       (is (= (-> parsed :options :player-two)
              {:type :random
               :name "Player Two"
               :location nil}))))

  (testing "min turn times get parsed"
    (let [parsed1 (parse ["-m" "fred"])
          parsed2 (parse ["-m" "-1"])
          parsed3 (parse ["-m" "2000"])]
      (is (some? (:errors parsed1)))
      (is (some? (:errors parsed2)))
      (is (nil? (:errors parsed3)))
      (is (= 2000 (-> parsed3 :options :min-turn-time)))))

  (testing "max turn times get parsed"
    (let [parsed1 (parse ["-x" "fred"])
          parsed2 (parse ["-x" "-1"])
          parsed3 (parse ["-x" "2000"])]
      (is (some? (:errors parsed1)))
      (is (some? (:errors parsed2)))
      (is (nil? (:errors parsed3)))
      (is (= 2000 (-> parsed3 :options :max-turn-time)))))

  (testing "remote server option works"
    (let [parsed1 (parse ["-r"])
          parsed2 (parse ["-r" "/bin/echo"])
          parsed3 (parse ["--p1-location" "/bin/echo"])]
      (is (some? (:errors parsed1)))
      (is (nil? (:errors parsed2)))
      (is (-> parsed2 :options :remote-server))
      (is (= "/bin/echo" (-> parsed2 :options :remote-server)))
      (is (nil? (:errors parsed3)))
      (is (not (-> parsed3 :options :remote-server)))))

  (testing "wait for client option works"
    (let [parsed1 (parse ["-w"])
          parsed2 (parse ["--wait-for-client"])
          parsed3 (parse ["--p1-location" "/bin/echo"])]
      (is (nil? (:errors parsed1)))
      (is (-> parsed1 :options :wait-for-client))
      (is (nil? (:errors parsed2)))
      (is (-> parsed2 :options :wait-for-client))
      (is (nil? (:errors parsed3)))
      (is (not (-> parsed3 :options :wait-for-client)))))

  (testing "parse-or-exit behaves"
    (is (= :random (-> (parse-or-exit []) :player-one :type)))
    (with-expect-call (exit [0 _])
      (parse-or-exit ["-h"]))
    (with-expect-call (exit [1 _])
      (parse-or-exit ["-1" "{}"]))))