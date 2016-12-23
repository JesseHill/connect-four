(ns connect-four.server.env
  (:require [environ.core :as e]))

(def env
  (merge
    {}
    e/env))
