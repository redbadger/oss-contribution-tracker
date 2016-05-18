(ns web.time-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs-time.core :as core]
            [cljs-time.coerce :as coerce]
            [web.time :as time]))

(defn date
  [& args]
  (coerce/to-long (apply core/date-time args)))

(deftest floor-day
  "round to the time at midnight"
  (let [actual-day (time/floor-day (date 2016 1 1 5 43 21))]
    (is (= (date 2016 1 1) actual-day))))

(deftest floor-week
  "round to sunday of the week"
  (let [actual-week (time/floor-week (date 2016 1 1 5 43 21))]
    (is (= (date 2015 12 27) actual-week))))

(deftest floor-month
  "round to the start of the month"
  (let [actual-month (time/floor-month (date 2016 1 12 5 43 21))]
    (is (= (date 2016 1 1) actual-month))))

(def expected-days
  [(date 2016 1 1)
   (date 2016 1 2)
   (date 2016 1 3)
   (date 2016 1 4)
   (date 2016 1 5)
   (date 2016 1 6)
   (date 2016 1 7)])

(deftest day-range
  "return a vector of times between a given range, rounded to midnight"
  (let [from (date 2016 1 1 5 43 21)
        to (date 2016 1 8 5 43 21)
        actual-days (time/day-range from to)]
    (is (= expected-days actual-days))))

(def expected-weeks
  [(date 2015 12 27)
   (date 2016 1 3)
   (date 2016 1 10)])

(deftest week-range
  "return a vector of times between a given range, rounded to sunday of the week"
  (let [from (date 2016 1 1 5 43 21)
        to (date 2016 1 21 5 43 21)
        actual-weeks (time/week-range from to)]
    (is (= expected-weeks actual-weeks))))

(def expected-months
  [(date 2016 1 1)
   (date 2016 2 1)
   (date 2016 3 1)])

(deftest month-range
  "return a vector of times between a given range, rounded to the start of the month"
  (let [from (date 2016 1 5 5 43 21)
        to (date 2016 4 10 5 43 21)
        actual-months (time/month-range from to)]
    (is (= expected-months actual-months))))
