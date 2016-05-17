(ns ^:figwheel-always web.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-all-tests]]
            [web.scale-test]
            [web.time-test]))

(enable-console-print!)

(defn run []
  (run-all-tests #"web.*-test"))
