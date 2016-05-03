(ns web.components.page
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defui Page
  Object
  (render [this]
    (dom/h1 nil "OSS Contribution Tracker")))

(def page (om/factory Page))
