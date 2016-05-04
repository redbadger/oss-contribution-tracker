(ns web.components.contributions
  (:require [cljs-time.coerce :as time]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn contribution
  [{dateCreated :dateCreated id :id user :user}]
  (dom/ul #js {:key id}
    (dom/li nil id)
    (dom/li nil (time/to-string dateCreated))
    (dom/li nil user)))

(defui Contributions
  Object
  (render [this]
    (let [{c :contributions} (om/props this)]
      (dom/div nil
        (dom/h2 nil "Contributions")
        (dom/div nil (map contribution c))))))

(def contributions (om/factory Contributions))
