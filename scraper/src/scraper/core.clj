(ns scraper.core
  (:require [org.httpkit.client :as http]
            [scraper.github-client :as github]
            [schema.0-initial :as schema]
            [scraper.storage :as db]))

(def gh (github/request http/get))

(defn process-user
  [user]
  (let [direct ((github/user-public-repos gh) user)
        orgs ((github/user-orgs gh) user)
        issues ((github/user-issues gh) user)]
    (concat direct orgs issues)))

(defn pass-through
  "returns a transform that applies a mapping-fn to a key of a map if the map has that key, otherwise
  leaves it alone"
  [map-key mapping-fn]
  (fn [item]
    (if (contains? item map-key)
      (mapping-fn item)
      [item])))

(defn split-issue
  [{:keys [user issue repo]}]
  [{:user user :issue issue}
   {:user user :repo repo}])

(defn process-contribution
  [{:keys [user issue commit]}]
  (cond
    commit (merge {:user user :type :commit } commit)
    issue (merge {:user user :type :issue } issue)
    :else nil))

(def tx-stack
  (comp
    (mapcat (github/org-members gh))
    (filter identity)
    (take 3)
    (mapcat process-user)
    (mapcat (pass-through :org (github/org-public-repos gh)))
    (mapcat (pass-through :issue split-issue))
    (distinct) ; dedupe repo requests
    (mapcat (pass-through :repo (github/repo-commits gh)))
    (map process-contribution)
    (filter identity)))

(defn -main []
  (let [db-transact (db/make-transactor db/conn)
        contributions (transduce tx-stack conj [{:org "redbadger"}])]
    (db-transact schema/schema)
    (db/insert-contributions db-transact contributions)
    (println "Done")))
