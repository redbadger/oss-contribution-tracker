(ns web.components.app
  (:require [cljs-time.coerce :as coerce]
            [cljs-time.format :as format]
            [om.dom :as dom]
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

(defn user-contributions
  [from to index {u :user/name c :contribution/_user}]
  (contributions {:id index
                  :contributions c
                  :label u
                  :from from
                  :to to}))

(defn to-date
  [ms]
  (format/unparse
    (format/formatters :date)
    (coerce/from-long ms)))

(defn from-date
  [date]
  (coerce/to-long date))

(defn handle-date-change
  [e component key]
  (let [{app :app} (om/props component)
        value (.. e -target -value)
        updated-app (assoc app key (from-date value))]
    (om/transact! component
      `[(app/update ~updated-app)])))

(defui App
  static om/IQueryParams
  (params [this]
    (let [{c :contributions} (om/get-query Contributions)]
      {:contributions-list c
       :contributions-by-user c}))
  static om/IQuery
  (query [this]
    '[{:app [:db/id :app/date-from :app/date-to]}
      {:users [:user/name {:contribution/_user ?contributions-by-user}]}
      {:contributions ?contributions-list}])
  Object
  (render [this]
    (let [{c :contributions
           u :users
           {from :app/date-from to :app/date-to} :app} (om/props this)]
      (dom/div #js {:style s-app}
        (dom/h1 #js {:style s-title} "OSS Contribution Tracker")
        (dom/div nil
          (dom/span nil "From ")
          (dom/input #js {:type "date"
                          :value (to-date from)
                          :onChange #(handle-date-change % this :app/date-from)})
          (dom/span nil " To ")
          (dom/input #js {:type "date"
                          :value (to-date to)
                          :onChange #(handle-date-change % this :app/date-to)}))
        (contributions {:id 0
                        :contributions c
                        :label "Red Badger"
                        :from from
                        :to to})
        (dom/div nil (map-indexed (partial user-contributions from to) u))))))
