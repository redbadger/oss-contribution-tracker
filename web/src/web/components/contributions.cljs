(ns web.components.contributions
  (:require [cljs-time.coerce :as coerce]
            [cljs-time.core :as time]
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.components.chart :refer (chart)]
            [web.styles :as styles]))

(def s-container
  (clj->js {:width "640px"
            :display "flex"}))

(def s-title-container
  (clj->js {:width "160px"
            :display "flex"
            :alignItems "center"}))

(def s-title
  (clj->js (merge styles/f3
                  {:margin "0 0 1rem"})))

(def s-chart
  (clj->js {:width "480px"}))


(def day
  (* 24 (* 60 (* 60 1000))))

(defn days
  [{date :dateCreated id :id}]
  (int (Math/floor (/ (coerce/to-long date) day))))

(defn toValues
  [[key value]]
  {:value (count value) :label key })

(defn contribution
  [{dateCreated :dateCreated uuid :id user :user}]
  (let [id (str uuid)
        date (coerce/to-string dateCreated)]
    (dom/ul #js {:key id}
      (dom/li nil id)
      (dom/li nil date)
      (dom/li nil user))))

(defui Contributions
  Object
  (render [this]
    (let [{c :contributions label :label} (om/props this)
          points (sort-by :label (map toValues (seq (group-by days c))))]
      (dom/div #js {:style s-container}
        (dom/div #js {:style s-title-container}
          (dom/h2 #js {:style s-title} label))
        ; (dom/div nil (map contribution c))
        (dom/div #js {:style s-chart}
          (chart {:points points}))))))


(def contributions (om/factory Contributions))
