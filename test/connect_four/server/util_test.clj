(ns connect-four.server.util-test
  (:require [clojure.test :refer :all]
            [connect-four.server.util :refer :all]
            [clj-time.core :as t]
            [org.senatehouse.expect-call :refer :all]))

(defn wait [millis] (Thread/sleep millis))

(deftest test-exec-and-wait
  (let [now (t/now)
        expected-end (t/interval (t/plus now (t/millis 100))
                               (t/plus now (t/millis 120)))]
    (expect-call (:do wait [50])
      (exec-and-wait 100 wait 50))
    (is (t/within? expected-end (t/now)))))