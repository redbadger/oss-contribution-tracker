(ns web.components.chart
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.styles :as styles]))

(def s-chart
  (clj->js {:backgroundColor "#EEE"
            :borderRadius "8px"}))

(def width 480)

(def height 160)

(def gutter 1)

(defn rect
  [x-scale y-scale item-width [x y]]
  (dom/rect #js {:key x
                 :data-test x
                 :width (- item-width (* gutter 2))
                 :height (- height (y-scale y))
                 :x (+ (x-scale x) gutter)
                 :y (y-scale y)
                 :fill styles/c-primary}))

(defui Chart
  Object
  (render [this]
    (let [{data :data
           x :x-scale
           y :y-scale
           count :x-count} (om/props this)
          item-width (/ width count)
          x-scale (x 0 (- width item-width))
          y-scale (y height 0)]
      (dom/svg #js {:width width
                    :height height
                    :style s-chart}
        (map (partial rect x-scale y-scale item-width) data)))))

(def chart (om/factory Chart))
