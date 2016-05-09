(ns scraper.core
  (:require [org.httpkit.client :as http]
            [scraper.github-client :as github]))

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
  (if commit
    (merge {:user user :type :commit } commit)
    (merge {:user user :type :issue } issue)))

(def tx-stack
  (comp
    (mapcat (github/org-members gh))
    (filter identity)
    (take 2)
    (mapcat process-user)
    (mapcat (pass-through :org (github/org-public-repos gh)))
    (mapcat (pass-through :issue split-issue))
    (mapcat (pass-through :repo (github/repo-commits gh)))
    (map process-contribution)))

(defn -main []
  (doseq [contribs (sequence tx-stack [{:org "redbadger"}])]
    (clojure.pprint/pprint contribs)))
