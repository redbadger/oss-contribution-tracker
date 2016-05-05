(ns web.components.chart
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(def width 320)

(def height 160)

(defn toSVGPoints
  [width height total x {y :value}]
  (let [itemGutterX (/ width total)
        offsetX (/ itemGutterX 2)
        itemGutterY (/ height 5)
        offsetY (/ itemGutterY 2)]
    (dom/circle #js {:key x
                     :cx (+ offsetX (* x itemGutterX))
                     :cy (- height (+ offsetY (* y itemGutterY)))
                     :r 5
                     :fill "white"
                     :stroke "grey"
                     :strokeWidth "2"})))

(defui Chart
  Object
  (render [this]
    (let [{points :points} (om/props this)]
      (dom/svg #js {:width width :height height}
        (dom/g nil (map-indexed (partial toSVGPoints width height (count points)) points))))))

(def chart (om/factory Chart))
