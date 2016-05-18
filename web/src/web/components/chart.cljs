(ns web.components.chart
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
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
           x :x-scale
           y :y-scale} (om/props this)
          x-scale (x 0 width)
          y-scale (y height 0)]
      (dom/svg #js {:width width
                    :height height
                    :style s-chart}
        (map (partial rect x-scale y-scale) data)))))

(def chart (om/factory Chart))
