(ns web.components.chart
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.scale :as scale]
            [web.styles :as styles]))

(def s-chart
  (clj->js {:backgroundColor "#EEE"
            :borderRadius "8px"}))

(def width 480)

(def height 160)

(defn rect
  [x-scale y-scale [x y]]
  (dom/rect #js {:key x
                 :width 5
                 :height (- height (y-scale y))
                 :x (x-scale x)
                 :y (y-scale y)
                 :fill styles/c-primary}))

(defui Chart
  Object
  (render [this]
    (let [{data :data
           from :from
           to :to} (om/props this)
          max-count (apply max 4 (map second data))
          y-scale (scale/linear 0 max-count height 0)
          x-scale (scale/linear from to 0 width)]
      (dom/svg #js {:width width :height height :style s-chart}
        (map (partial rect x-scale y-scale) data)))))

(def chart (om/factory Chart))
