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
  {:user/name {:db/unique :db.unique/identity}
   :user/contributions {:db/cardinality :db.cardinality/many}
   :contribution/id {:db/unique :db.unique/identity}
   :contribution/user {:db/type :db.type/ref}
   :contribution/languages {:db/cardinality :db.cardinality/many}})

(def conn (d/create-conn schema))

(defn add-db-id
  [id item]
  (assoc item :db/id (* (+ id 1) -1)))

(defn to-db-user
  [name]
  {:user/name name})

(def initial-users
  (map-indexed add-db-id (map to-db-user fixtures/users)))

(def initial-contributions
  (map-indexed add-db-id
    (into [] (gen/sample fixtures/contribution 50))))

; Set up initial database with users
(d/transact! conn
  initial-users)

; Set up initial database with contributions
(d/transact! conn
  initial-contributions)

(defmulti read om/dispatch)

(defmethod read :contributions/list
  [{:keys [state query] :as env} key params]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :contribution/id]]
            (d/db state) query)})

(defmethod read :users/list
  [{:keys [state query] :as env} key params]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :user/name]]
            (d/db state) query)})

(def parser (om/parser {:read read}))

(def reconciler
  (om/reconciler
    {:state conn
     :parser parser}))

(om/add-root! reconciler
  App (gdom/getElement "app"))
