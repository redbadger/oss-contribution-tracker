(ns web.scale-test
  (:require [cljs.test :refer-macros [deftest is]]
            [web.scale :as scale]))


(deftest linear
  "maps domain scale to range scale"
  (let [simple (scale/linear 0 1 0 100)
        inverse (scale/linear 0 1 100 0)]
    (is (= (simple 1) 100))
    (is (= (simple 0.5) 50))
    (is (= (simple 0) 0))
    (is (= (inverse 1) 0))
    (is (= (inverse 0.5) 50))
    (is (= (inverse 0) 100))))


(deftest ordinal
  "maps values to range scale"
  (let [s (scale/ordinal ["a" "b" "c" "d" "e"] 0 100)]
    (is (= (s "a") 0))
    (is (= (s "b") 25))
    (is (= (s "c") 50))
    (is (= (s "d") 75))
    (is (= (s "e") 100))))
