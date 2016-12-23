(ns connect-four.server.transport-test
  (:require [clojure.test :refer :all]
            [connect-four.server.transport :refer :all]))

(deftest test-serialize
  (is (= "[\"^ \",\"~:a\",[1,2]]"
         (serialize {:a [1 2]}))))

(deftest test-deserialize
  (is (= {:a [1 2]}
         (deserialize "[\"^ \",\"~:a\",[1,2]]"))))