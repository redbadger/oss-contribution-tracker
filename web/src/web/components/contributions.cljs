(ns web.components.contributions
  (:require [cljs-time.coerce :as coerce]
            [cljs-time.core :as time]
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.components.chart :refer (chart)]))

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
      (dom/div nil
        (dom/h2 nil label)
        ; (dom/div nil (map contribution c))
        (chart {:points points})))))


(def contributions (om/factory Contributions))
