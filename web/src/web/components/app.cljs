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

(defn user-to-contributions
  [index {u :user/name c :contribution/_user}]
  (contributions {:id index
                  :contributions c
                  :label u}))

(defui App
  static om/IQueryParams
  (params [this]
    (let [{c :contributions} (om/get-query Contributions)]
      {:contributions-list c
       :contributions-by-user c}))
  static om/IQuery
  (query [this]
    '[{:users/list [:user/name {:contribution/_user ?contributions-by-user}]}
      {:contributions/list ?contributions-list}])
  Object
  (render [this]
    (let [{c :contributions/list u :users/list} (om/props this)]
      (dom/div #js {:style s-app}
        (dom/h1 #js {:style s-title} "OSS Contribution Tracker")
        (contributions {:id 0
                        :contributions c
                        :label "Red Badger"})
        (dom/div nil (map-indexed user-to-contributions u))))))
