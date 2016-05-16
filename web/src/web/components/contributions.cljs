(ns web.components.contributions
  (:require [cljs-time.coerce :as coerce]
            [cljs-time.core :as time]
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.components.chart :refer (chart)]
            [web.styles :as styles]))

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

(def day
  (* 24 (* 60 (* 60 1000))))

(def week
  (* 7 day))

(def month
  (* 31 day))

(defn by
  [interval {date :contribution/date-created}]
  (* interval (quot date interval)))

(defn counter
  [[day contributions]]
  [day (count contributions)])

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
           to :to} (om/props this)
          data (map counter (group-by (partial by week) c))]
      (dom/div #js {:style s-container}
        (dom/div #js {:style s-title-container}
          (dom/h2 #js {:style s-title} label))
        (dom/div #js {:style s-chart}
          (chart {:data data
                  :from from
                  :to to}))))))

(def contributions (om/factory Contributions {:keyfn :id}))
