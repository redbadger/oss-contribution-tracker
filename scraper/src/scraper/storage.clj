(ns scraper.storage
  (:require [environ.core :refer [env]]
            [clojure.edn :as edn]
            [datomic.api :as d]
            [clojure.instant :refer [read-instant-timestamp]]))

(def db-uri (env :datomic-db-uri))

(defn connect []
  (d/connect db-uri))

(defn make-transactor
  [conn]
  (fn [data]
    (d/transact conn data)))

(def contribution-types
  {:commit :contribution.type/commit
   :issue :contribution.type/issue
   :pull-request :contribution.type/pull-request})

(defn contribution-tx-data
  [contribution id-base]
  [{:db/id (d/tempid :db.part/user (- (+ id-base 1)))
    :author/username (:user contribution)}
   {:db/id (d/tempid :db.part/user (- (+ id-base 2)))
    :repository/name (:repo contribution)}
   {:db/id (d/tempid :db.part/user (- (+ id-base 3)))
    :contribution/type (contribution-types (:type contribution))
    :contribution/author (d/tempid :db.part/user (- (+ id-base 1)))
    :contribution/repository (d/tempid :db.part/user (- (+ id-base 2)))
    :contribution/date (read-instant-timestamp (:date contribution))
    :contribution/title (:title contribution)
    :contribution/url (:url contribution)}])

(defn insert-contributions
  "Inserts contributions into the database"
  [transact contributions]
  (let [step (fn [total chunk]
                 (let [len (count chunk)
                       id-bases (map (partial * 3) (range len))
                       tx-data (distinct (mapcat contribution-tx-data chunk id-bases))]
                   (transact tx-data)
                   (+ total len)))]
    (println "Processing" (count contributions) "contributions")
    (doseq [total (drop 1 (reductions step 0 (partition-all 12 contributions)))]
      (println total "contributions stored..."))))
