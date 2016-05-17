(defproject web "0.1.0-SNAPSHOT"
  :description "Red Badger OSS Contribution Tracker"
  :dependencies [[com.andrewmcveigh/cljs-time "0.4.0"]
                 [datascript "0.15.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [org.clojure/test.check "0.9.0"]
                 [org.omcljs/om "1.0.0-alpha34"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-SNAPSHOT"]]

  :source-paths  ["src"]

  :test-paths ["test"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src" "test"]
              :figwheel true
              :compiler {
                :main web.core
                :asset-path "js"
                :output-to "resources/public/js/main.js"
                :output-dir "resources/public/js"
                :source-map true
                :verbose true}}]}

  :figwheel {
    :server-port 8080})
