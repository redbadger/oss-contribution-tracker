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
  (fn [{org :org :as record}]
    (let [res (gh {:path (str "/orgs/" org "/members") :query {:per_page 100}})]
      (into [] (map (fn [it] {:user (:login it)}) res)))))

(defn user-orgs
  "fetches organisation a user publicly belongs to"
  [gh]
  (fn [{user :user :as record}]
    (let [res (gh {:path (str "/users/" user "/orgs") :query {:per_page 100}})]
      (into [] (map (fn [it] {:org (:login it) :user user}) res)))))

(defn parse-repos
  [user repos]
  (let [tx (comp
             (filter #(not (or (:private %) (:fork %))))
             (map (fn [repo] {:repo (:full_name repo) :user user})))]
    (into [] (sequence tx repos))))

(defn user-public-repos
  "fetches user's public repos"
  [gh]
  (fn [{user :user :as record}]
    (let [res (gh {:path (str "/users/" user "/repos") :query {:per_page 100}})]
      (parse-repos user res))))

(defn org-public-repos
  "fetches public repos of an organisation"
  [gh]
  (fn [{org :org user :user :as record}]
    (let [res (gh {:path (str "/orgs/" org "/repos") :query {:per_page 100 :type "public"}})]
      (parse-repos user res))))

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
        user (:login (:user issue))
        pr? (:pull_request issue)
        type (if pr? :pull-request :issue)
        url (if pr? (:html_url (:pull_request issue)) (:html_url issue))]
    {:repo repo :user user :issue {:type type :url url}}))

(defn user-issues
  "fetches user's public issues and PRs across github"
  [gh]
  (fn [{user :user :as record}]
    (let [query (str "author:" user)
          res (gh {:path (str "/search/issues") :query {:q query :per_page 100}})
          ]
      (into [] (map parse-issue (:items res))))))

(defn parse-commit
  [user commit]
  {:user user
   :commit {:message (:message (:commit commit)) :url (:html_url commit)}})

(defn repo-commits
  "fetches user's public issues and PRs across github"
  [gh]
  (fn [{user :user repo :repo :as record}]
    (let [query (str "author:" user)
          res (gh {:path (str "/repos/" repo "/commits") :query {:author user :per_page 100}})
          ]
      (into [] (map (partial parse-commit user) res)))))
