(ns web.core
  (:require [cljs-time.coerce :as time]
            [goog.dom :as gdom]
            [goog.string :refer (format)]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [web.components.page :refer (Page)]))

(enable-console-print!)

(defn mock-contribution
  [seed]
  (let [date (-> seed (* 1000) (* 60) (* 60) (* 24) (+ 1450000000000))]
    { :id seed
      :user (format "Test User %d" seed)
      :repository (format "Test Repository %d" seed)
      :dateCreated (time/from-long date)
      :datePublic (time/from-long date)
      :type :contribution/commit
      :languages [ "Javascript" ]
      :url "https://github.com/redbadger/oss-contribution-tracker.git" }))

(def app-state
  (atom {:contributions (map mock-contribution (range 0 5))}))

(def reconciler
  (om/reconciler {:state app-state}))

(om/add-root! reconciler
  Page (gdom/getElement "app"))
