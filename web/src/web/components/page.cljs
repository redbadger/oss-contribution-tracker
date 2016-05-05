(ns web.components.page
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [web.components.contributions :refer (contributions)]))

(defui Page
  Object
  (render [this]
    (let [{c :contributions} (om/props this)]
      (dom/div nil
        (dom/h1 nil "OSS Contribution Tracker")
        (contributions {:contributions c
                        :label "Red Badger Contributions"})))))
