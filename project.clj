(defproject connect-four "0.1.0-SNAPSHOT"
  :description "This is a game engine to run connect four AI bots. It was written for the Atomic Games 2017."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha8"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/core.async "0.2.385"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.omcljs/om "1.0.0-alpha36"]

                 [mount "0.1.10"]
                 [environ "1.0.0"]
                 [com.cognitect/transit-clj "0.8.285"]
                 [clj-time "0.12.0"]

                 [http-kit "2.1.18"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-servlet "1.5.0" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-logger "0.7.6"]
                 [figwheel-sidecar "0.5.4-3"]
                 [compojure "1.5.1"]

                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]]
  :plugins [[lein-figwheel "0.5.4-3"]
            [lein-cljsbuild "1.1.3"]
            [lein-checkall "0.1.1"]
            [lein-cljfmt "0.5.3"]
            [lein-auto "0.1.2"]
            [lein-autoreload "0.1.0"]]
  :cljsbuild {
    :builds [{
        ; The path to the top-level ClojureScript source directory:
        :source-paths ["src"]
        ; The standard ClojureScript compiler options:
        ; (See the ClojureScript compiler documentation for details.)
        :compiler {
          :output-to "resources/public/js/main.js"  ; default: target/cljsbuild-main.js
          :optimizations :whitespace
          :pretty-print true}}]}
  :hooks [leiningen.cljsbuild]
  :main ^:skip-aot connect-four.server.core
  :target-path "target/%s"
  :aliases {"figwheel" ["run" "-m" "clojure.main" "script/figwheel.clj"]
            "fw" "figwheel"}
  :profiles {:uberjar {:aot :all}
             :test {:env {:in-test-mode true}
                    :source-paths ["test/resources"]
                    :dependencies [[org.senatehouse/expect-call "0.1.0"]]}
             :dev {:env {}
                   :source-paths ["dev/resources"]
                   :dependencies [; SLF4J takes over the many logging APIs and will redirect all logs to the centrally configured logback (above)
                                  [org.slf4j/slf4j-api "1.7.21"]
                                  [org.slf4j/jul-to-slf4j "1.7.21"]
                                  [org.slf4j/jcl-over-slf4j "1.7.21"]
                                  [org.slf4j/log4j-over-slf4j "1.7.21"]
                                  [figwheel-sidecar "0.5.4-7"]]}})
