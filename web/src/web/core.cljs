(ns web.core
  (:require [goog.dom :as gdom]
            [web.components.page :as page]))

(js/ReactDOM.render (page/page) (gdom/getElement "app"))
