(ns connect-four.server.util
  (:require [clj-time.core :as t]))

(defn exec-and-wait [time-to-wait f & args]
  (let [earliest-end (-> time-to-wait t/millis t/from-now)
        result (apply f args)]
    (let [now (t/now)]
      (when (t/before? now earliest-end)
        (Thread/sleep (-> (t/interval now earliest-end) t/in-millis))))
    result))

(defmacro time-limited [ms error & body]
  `(let [f# (future ~@body)]
     (deref f# ~ms {:error ~error})))