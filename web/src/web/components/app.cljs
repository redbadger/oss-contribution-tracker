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

(def labels
  {:interval/day "Day"
   :interval/week "Week"
   :interval/month "Month"})

(defn named
  [keyword]
  (subs (str keyword) 1))

(defn interval-option
  [[key label]]
  (dom/option #js {:key key
                   :value (named key)}
    label))

(defn user-contributions
  [from to interval index {u :user/name c :contribution/_user}]
  (contributions {:id index
                  :contributions c
                  :label u
                  :from from
                  :to to
                  :interval interval}))

(defn to-date
  [ms]
  (format/unparse
    (format/formatters :date)
    (coerce/from-long ms)))

(defn from-date
  [date]
  (coerce/to-long date))

(defn handle-change
  [e component key map]
  (let [{app :app} (om/props component)
        value (.. e -target -value)
        updated-app (assoc app key (map value))]
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
    '[{:app [:db/id :app/date-from :app/date-to :app/interval]}
      {:users [:user/name {:contribution/_user ?contributions-by-user}]}
      {:contributions ?contributions-list}])
  Object
  (render [this]
    (let [{c :contributions
           u :users
           {from :app/date-from to :app/date-to interval :app/interval} :app} (om/props this)
          label (get labels interval)]
      (dom/div #js {:style s-app}
        (dom/h1 #js {:style s-title} "OSS Contribution Tracker")
        (dom/div nil
          (dom/span nil "From ")
          (dom/input #js {:type "date"
                          :value (to-date from)
                          :onChange #(handle-change % this :app/date-from from-date)})
          (dom/span nil " To ")
          (dom/input #js {:type "date"
                          :value (to-date to)
                          :onChange #(handle-change % this :app/date-to from-date)})
          (dom/span nil " Interval ")
          (dom/select #js {:value (named interval)
                           :onChange #(handle-change % this :app/interval keyword)}
            (map interval-option (seq labels))))
        (contributions {:id 0
                        :contributions c
                        :label "Red Badger"
                        :from from
                        :to to
                        :interval interval})
        (dom/div nil (map-indexed (partial user-contributions from to interval) u))))))
