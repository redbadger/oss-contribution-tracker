(ns web.core
  (:require [clojure.test.check.generators :as gen]
            [datascript.core :as d]
            [goog.dom :as gdom]
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [web.components.app :refer (App)]
            [web.fixtures :as fixtures]))

(enable-console-print!)

(def schema
  {:contributions/user {:db/cardinality :db.cardinality/many}
   :contribution/languages {:db/cardinality :db.cardinality/many}})

(def conn (d/create-conn schema))

(defn add-db-id
  [id item]
  (assoc item :db/id (* (+ id 1) -1)))

(def initial-contributions
  (map-indexed add-db-id
    (into [] (gen/sample fixtures/contribution 50))))

; Set up initial database with contributions
(d/transact! conn
  initial-contributions)

(defmulti read om/dispatch)

(def contibution-query
  '[:find [(pull ?e ?selector) ...]
    :in $ ?selector ?user
    :where [?e :contribution/id]
           [?e :contribution/user ?user]])

(defmethod read :contributions/list
  [{:keys [state query] :as env} key {:keys [user] :as params}]
  (let [q-args [contibution-query (d/db state) query]
        query-args (if user (conj q-args user) q-args)]
    {:value (apply d/q query-args)}))

(def parser (om/parser {:read read}))

(def reconciler
  (om/reconciler
    {:state conn
     :parser parser}))

(om/add-root! reconciler
  App (gdom/getElement "app"))
