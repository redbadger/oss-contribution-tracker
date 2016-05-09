(ns web.components.app
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.components.contributions :refer (Contributions contributions)]
            [web.styles :as styles]))

(def s-app
  (clj->js (merge styles/sans-serif
                  {:color styles/c-typography
                   :width "640px"
                   :margin "0px auto"})))

(def s-title
  (clj->js styles/f2))

(defui App
  static om/IQueryParams
  (params [this]
    {:contributions (om/get-query Contributions)}
    )
  static om/IQuery
  (query [this]
    '?contributions)
  Object
  (render [this]
    (let [{c :contributions/list} (om/props this)]
      (dom/div #js {:style s-app}
        (dom/h1 #js {:style s-title} "OSS Contribution Tracker")
        (contributions {:contributions/list c
                        :label "Red Badger"})))))
