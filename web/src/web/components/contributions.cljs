(ns web.components.contributions
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui Contributions
  Object
  (render [this]
    (dom/h2 nil "Contributions")))

(def contributions (om/factory Contributions))
