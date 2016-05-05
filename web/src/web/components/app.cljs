(ns web.components.app
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.components.contributions :refer (contributions)]))

(def app-styles
  #js {:color "#333"})

(defui App
  Object
  (render [this]
    (let [{c :contributions} (om/props this)]
      (dom/div {:class :app}
        (dom/h1 #js {:style app-styles} "OSS Contribution Tracker")
        (contributions {:contributions c
                        :label "Red Badger Contributions"})))))
