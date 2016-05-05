(ns web.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [web.components.page :refer (Page)]
            [web.fixtures :as fixtures]
            [clojure.test.check.generators :as gen]))

(enable-console-print!)

(def hour (* 60 (* 60 1000)))

(def day (* 24 hour))

(def app-state
  (atom {:contributions (gen/sample fixtures/contribution 25)}))

(def reconciler
  (om/reconciler {:state app-state}))

(om/add-root! reconciler
  Page (gdom/getElement "app"))
