(ns schema.0-initial
  (:require [environ.core :refer [env]]
            [datomic.api :as d]))

(def db-uri (env :datomic-db-uri))

(d/create-database db-uri)
(def conn (d/connect db-uri))


(def schema
  [{:db/id #db/id[:db.part/db]
    :db/ident :contribution/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of the contribution"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :contribution/url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The html URL of the contribution"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :contribution/date
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "The instant the contribution was made"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :contribution/type
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "The contribution type"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/user]
    :db/ident :contribution.type/commit}

   {:db/id #db/id[:db.part/user]
    :db/ident :contribution.type/issue}

   {:db/id #db/id[:db.part/user]
    :db/ident :contribution.type/pull-request}

   {:db/id #db/id[:db.part/db]
    :db/ident :contribution/author
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Person who made the contribution"
    :db.install/_attribute :db.part/db}

   {:db/id #db/id[:db.part/db]
    :db/ident :contribution/repository
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Repository the contribution was made into"
    :db.install/_attribute :db.part/db}

  ; author

   {:db/id #db/id[:db.part/db]
    :db/ident :author/username
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "Username of a person"
    :db.install/_attribute :db.part/db}

  ; repository

   {:db/id #db/id[:db.part/db]
    :db/ident :repository/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "Name of a repository"
    :db.install/_attribute :db.part/db}])

(defn define-schema
  [con sch]
  (println "Defining schema...")
  (d/transact con sch)
  (println "Done."))

(defn -main []
  (define-schema conn schema))
