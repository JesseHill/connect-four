(ns connect-four.server.core
  (:require [mount.core :as mount]
            [connect-four.server.state]
            [connect-four.server.http-server]
            [connect-four.server.text-ui]
            [connect-four.server.remote-server :as remote]
            [connect-four.server.game]
            [connect-four.server.options :as options])
  (:gen-class))

(defn selected-states [options]
  (if (:remote-server options)
    (mount/only #{#'remote/server})
    (mount/except [#'remote/server])))

(defn -main
  [& args]
  (let [options (options/parse-or-exit args)]
    (-> (selected-states options)
        (mount/with-args options)
        mount/start)))