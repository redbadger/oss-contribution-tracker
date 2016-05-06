(ns scraper.github-client
  (:require [clojure.data.json :as json]))

(def github-auth ["charypar" (str "5bfd3ddc858b5ddfc" "51543f66f0a6dca765f44a4")]) ; can only read org members
(def github-base-path "https://api.github.com")

(defn request
  "run a github request"
  [http-get]
  (fn [req]
    (let [{:keys [path query]} req
          url (str github-base-path path)
          options {:query-params (or query {}) :basic-auth github-auth}
          {:keys [status headers body error]} @(http-get url options)]
      (println "GET" path query "..." status)
      (if error
        (println "Request failed" req "exception" error))
        (json/read-str body :key-fn keyword))))

(defn org-members
  "fetches members of an organisation"
  [gh]
  (fn [org]
    (let [res (gh {:path (str "/orgs/" org "/members") :query {:per_page 500}})]
      (into [] (map :login res)))))

(defn user-public-repos
  "fetches members of an organisation"
  [gh]
  (fn [user]
    (let [res (gh {:path (str "/users/" user "/repos") :query {:per_page 500}})]
      (into [] (map :name (filter #(not (or (:private %) (:fork %))) res))))))

(defn user-orgs
  "fetches members of an organisation"
  [gh]
  (fn [user]
    (let [res (gh {:path (str "/users/" user "/orgs") :query {:per_page 500}})]
      (into [] (map :login res)))))
