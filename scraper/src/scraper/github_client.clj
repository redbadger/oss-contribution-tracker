(ns scraper.github-client
  (:require [clojure.data.json :as json]
            [clojure.string :refer [split]]))

(def github-auth ["charypar" (str "5bfd3ddc858b5ddfc" "51543f66f0a6dca765f44a4")]) ; can only read org members
(def github-base-path "https://api.github.com")

(defn qs-pair
  [pair-str]
  (let [[key value] (split pair-str #"=")]
    [(keyword key) value]))

(defn next-page
  "get the next page url from a Link header"
  [link-header]
  (if link-header
    (let [[_ url] (re-matches #"<([^>]+)>; ?rel=\"next\".*" link-header)
           [url qs] (split url #"\?")
           query (apply hash-map (mapcat qs-pair (split qs #"&")))]
      {:url url :query query})))

(defn perform-request
  "run a single github request"
  [http-get req]
  (let [{:keys [url query]} req
        options {:query-params (or query {}) :basic-auth github-auth}
        {:keys [status headers body error]} @(http-get url options)]
    (println "GET" url query "..." status)
    (if error
      (println "Request failed" req "exception" error))
      {:body (json/read-str body :key-fn keyword)
       :next-page (next-page (:Link headers))}))

(defn request
  "run a github request and follow pagination if necessary"
  [http-get]
  (fn [initial-req]
    (let [{path :path} initial-req
          url (str github-base-path path)
          {:keys [body next-page]} (perform-request http-get (assoc initial-req :url url))]
      (if (not next-page)
        body
        (loop [items body
               req (merge initial-req next-page)]
          (let [{:keys [body next-page]} (perform-request http-get req)
                new-items (into [] (concat items body))]
            (if (not next-page)
              new-items
              (recur new-items (merge req next-page)))))))))

(defn org-members
  "fetches members of an organisation"
  [gh]
  (fn [org]
    (let [res (gh {:path (str "/orgs/" org "/members") :query {:per_page 100}})]
      (into [] (map :login res)))))

(defn user-public-repos
  "fetches user's public repos"
  [gh]
  (fn [user]
    (let [res (gh {:path (str "/users/" user "/repos") :query {:per_page 100}})]
      (into [] (map :full_name (filter #(not (or (:private %) (:fork %))) res))))))

(defn user-orgs
  "fetches organisation a user publicly belongs to"
  [gh]
  (fn [user]
    (let [res (gh {:path (str "/users/" user "/orgs") :query {:per_page 100}})]
      (into [] (map :login res)))))

(defn org-public-repos
  "fetches public repos of an organisation"
  [gh]
  (fn [org]
    (let [res (gh {:path (str "/orgs/" org "/repos") :query {:per_page 100 :type "public"}})]
      (into [] (map :full_name (filter #(not (:fork %)) res))))))

(defn repo-from-url
  [url]
  (clojure.string/join "/"
    (-> url
      (split #"/")
      (reverse)
      (vec)
      (subvec 0 2)
      (reverse))))

(defn parse-issue
  [issue]
  (let [repo (repo-from-url (:repository_url issue))
        pr? (:pull_request issue)
        type (if pr? :pull_request :issue)
        url (if pr? (:html_url (:pull_request issue)) (:html_url issue))]
    {:repo repo :issue { :type type :url url}}))

(defn user-issues
  "fetches user's public issues and PRs across github"
  [gh]
  (fn [users]
    (let [query (clojure.string/join " " (map #(str "author:" %) users))
          res (gh {:path (str "/search/issues") :query {:q query :per_page 100}})
          ]
      (into [] (map parse-issue (:items res))))))
