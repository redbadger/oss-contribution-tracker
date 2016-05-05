(ns web.core
  (:require [clojure.test.check.generators :as gen]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [web.components.app :refer (App)]
            [web.fixtures :as fixtures]))

(enable-console-print!)

(def app-state
  (atom {:contributions (gen/sample fixtures/contribution 25)}))

(def reconciler
  (om/reconciler {:state app-state}))

(om/add-root! reconciler
  App (gdom/getElement "app"))
