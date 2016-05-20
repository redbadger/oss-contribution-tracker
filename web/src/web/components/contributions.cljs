(ns web.components.contributions
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.components.chart :refer (chart)]
            [web.scale :as scale]
            [web.styles :as styles]
            [web.time :as time]))

(def s-container
  (clj->js {:width "640px"
            :display "flex"}))

(def s-title-container
  (clj->js {:width "160px"
            :display "flex"
            :alignItems "center"}))

(def s-title
  (clj->js (merge styles/f3
                  {:margin "0 0 1rem"})))

(def s-chart
  (clj->js {:width "480px"}))

(defn by
  [interval {date :contribution/date-created}]
  (interval date))

(def intervals
  {:interval/day [time/floor-day time/day-range]
   :interval/week [time/floor-week time/week-range]
   :interval/month [time/floor-month time/month-range]})

(defn counter
  [[time contributions]]
  [time (count contributions)])

(defui Contributions
  static om/IQuery
  (query [this]
    {:contributions [:contribution/id
                     :contribution/date-created]})
  Object
  (render [this]
    (let [{c :contributions
           label :label
           from :from
           to :to
           interval :interval} (om/props this)
          [floor range] (get intervals interval)
          data (map counter (group-by (partial by floor) c))
          max-count (apply max 4 (map second data))
          x-range (range from to)
          x-count (count x-range)
          x-scale (partial scale/ordinal x-range)
          y-scale (partial scale/linear 0 max-count)]
      (dom/div #js {:style s-container}
        (dom/div #js {:style s-title-container}
          (dom/h2 #js {:style s-title} label))
        (dom/div #js {:style s-chart}
          (chart {:data data
                  :x-scale x-scale
                  :y-scale y-scale
                  :x-count x-count}))))))

(def contributions (om/factory Contributions {:keyfn :id}))
