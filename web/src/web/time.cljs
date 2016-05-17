(ns ^:figwheel-always web.time
  (:require [cljs-time.coerce :as coerce]
            [cljs-time.core :as time]
            [cljs-time.periodic :as p]))

(defn day-range
  "return a vector of times between a given range, rounded to the time at midnight"
  [rmin rmax]
  (map coerce/to-long
    (p/periodic-seq
      (time/at-midnight (coerce/from-long rmin))
      (time/at-midnight (coerce/from-long rmax))
      (time/days 1))))

(defn week-range
  "return a vector of times between a given range, rounded to the nearest week"
  [rmin rmax]
  (let [dmin (coerce/from-long rmin)
        min (time/minus dmin (time/days (time/day-of-week dmin)))
        dmax (coerce/from-long rmax)
        max (time/minus dmax (time/days (time/day-of-week dmax)))]
        (map coerce/to-long
          (p/periodic-seq
            (time/at-midnight min)
            (time/at-midnight max)
            (time/weeks 1)))))


(defn month-range
  "return a vector of times between a given range, rounded to the nearest month"
  [rmin rmax]
  (map coerce/to-long
    (p/periodic-seq
      (time/first-day-of-the-month (coerce/from-long rmin))
      (time/first-day-of-the-month (coerce/from-long rmax))
      (time/months 1))))
