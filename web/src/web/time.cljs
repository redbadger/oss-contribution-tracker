(ns web.time
  (:require [cljs-time.coerce :as coerce]
            [cljs-time.core :as time]
            [cljs-time.periodic :as p]))

(defn- dt-floor-day
  [dt]
  (time/at-midnight dt))

(defn floor-day
  "round to the time at midnight"
  [t]
  (coerce/to-long (dt-floor-day (coerce/from-long t))))

(defn- dt-floor-week
  [dt]
  (let [day (mod (time/day-of-week dt) 7)]
    (dt-floor-day (time/minus dt (time/days day)))))

(defn floor-week
  "round to the sunday of the current week"
  [t]
  (coerce/to-long (dt-floor-week (coerce/from-long t))))

(defn- dt-floor-month
  [dt]
  (time/first-day-of-the-month dt))

(defn floor-month
  "round to the first of the current month"
  [t]
  (coerce/to-long (dt-floor-month (coerce/from-long t))))

(defn day-range
  "return a vector of times between a given range, rounded to the time at midnight"
  [rmin rmax]
  (let [min (coerce/from-long rmin)
        max (coerce/from-long rmax)]
    (map coerce/to-long
      (p/periodic-seq
        (dt-floor-day min)
        max
        (time/days 1)))))

(defn week-range
  "return a vector of times between a given range, rounded to the sunday of the current week"
  [rmin rmax]
  (let [min (coerce/from-long rmin)
        max (coerce/from-long rmax)]
    (map coerce/to-long
      (p/periodic-seq
        (dt-floor-week min)
        max
        (time/weeks 1)))))

(defn month-range
  "return a vector of times between a given range, rounded to the nearest month"
  [rmin rmax]
  (let [min (coerce/from-long rmin)
        max (coerce/from-long rmax)]
    (map coerce/to-long
      (p/periodic-seq
        (dt-floor-month min)
        max
        (time/months 1)))))
