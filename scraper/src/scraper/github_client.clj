(ns scraper.github-client
  (:require [environ.core :refer [env]]
            [clojure.data.json :as json]
            [clojure.string :refer [split]]
            [clojure.core.async :refer [chan <!! >!! thread timeout]]
            [clojure.tools.logging :as log]))

(def github-auth [(env :github-username) (env :github-token)])
(def github-base-path "https://api.github.com")

(defn- qs-pair
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

(defn- rate-limit-scope
  [url]
  (let [[_ scope] (re-matches #"https://[^/]+/([^/]+)/.*" url)]
    (if (= "search" scope)
      :search
      :core)))

(defn- rate-limit
  [scope headers]
  (if (and (:x-ratelimit-remaining headers) (:x-ratelimit-reset headers))
    {:scope scope
     :remaining (Integer/parseInt (:x-ratelimit-remaining headers))
     :reset (Integer/parseInt (:x-ratelimit-reset headers))}
    {}))

(defn perform-request
  "run a single github request"
  [http-get scope url & [query]]
  (let [options {:query-params (or query {}) :basic-auth github-auth}
        {:keys [status headers body error]} @(http-get url options)]
    (if error
      (log/info "Request failed" url "exception" error))
    {:status status
     :body (json/read-str body :key-fn keyword)
     :next-page (next-page (:Link headers))
     :rate-limit (rate-limit scope headers)}))

(defn- time-now []
  (System/currentTimeMillis))

(defn- calculate-delay
  "Calculate delay necessary to stay within the rate limit"
  [scope now rate-limit]
  (let [{:keys [remaining reset]} (rate-limit scope)]
    (if (and now remaining reset)
      (max 0 (quot (- reset now) remaining))
      0)))

(defn- update-rate-limit
  [current response]
  (if (:reset response)
    (assoc current (:scope response) {:remaining (:remaining response) :reset (* 1000 (:reset response))})
    {}))

(defn with-rate-limiting
  "Makes a function asynchronous by running it on a separate thread passing inputs and outputs
  over async channels. Assumes the function returns a map with a :rate-limit key containing
  the current rate limits. NOTE The resulting function is thread blocking!"
  [fun]
  (let [input-channel (chan)]
    (thread ; spin off the worker thread
      (loop [{:keys [args ch]} (<!! input-channel)
             limits {}]
        (if args ; stop when the channel closes
          (let [[_ scope] args
                delay (calculate-delay scope (time-now) limits)]
            (if delay
              (do
                (log/info (str "  Pausing for " delay " ms to fit the rate limit in scope " scope". (" limits ")..."))
                (<!! (timeout delay))))
            (let [{rate-limit-info :rate-limit :as ret-val} (apply fun args)
                  new-rate-limit-info (update-rate-limit limits rate-limit-info)]
              (>!! ch ret-val)
              (recur (<!! input-channel) new-rate-limit-info))))))
    (fn [& args] ; original function proxy on the current thread
      (let [out-ch (chan)]
        (>!! input-channel {:args args :ch out-ch})
        (<!! out-ch)))))

(defn request
  "run a github request while respecting the rate limit and follow pagination if necessary"
  [http-get]
  (let [run-req (with-rate-limiting perform-request)]
    (fn [initial-req]
      (log/info "> GET" (str github-base-path (:path initial-req)) (:query initial-req) "...")
      (let [{path :path query :query} initial-req
            url (str github-base-path path)
            scope (rate-limit-scope url)
            {:keys [body next-page status]} (run-req http-get scope url query)]
        (log/info "<" status "(next page" next-page ")" "\n")
        (if (not next-page)
          body
          (loop [items body
                 {:keys [url query]} next-page]
            (log/info "> GET" url query "...")
            (let [{:keys [body next-page status]} (run-req http-get scope url query)
                  new-items (into [] (concat items body))]
              (log/info "<" status "\n")
              (if (not next-page)
                new-items
                (recur new-items next-page)))))))))

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

(defn- parse-repos
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

(defn- repo-from-url
  [url]
  (clojure.string/join "/"
    (-> url
      (split #"/")
      (reverse)
      (vec)
      (subvec 0 2)
      (reverse))))

(defn- parse-issue
  [issue]
  (let [repo (repo-from-url (:repository_url issue))
        user (:login (:user issue))
        {date :created_at title :title} issue
        pr? (:pull_request issue)
        type (if pr? :pull-request :issue)
        url (if pr? (:html_url (:pull_request issue)) (:html_url issue))]
    {:repo repo :user user :issue {:type type :repo repo :title title :date date :url url}}))

(defn user-issues
  "fetches user's public issues and PRs across github"
  [gh]
  (fn [{user :user :as record}]
    (let [query (str "author:" user)
          res (gh {:path (str "/search/issues") :query {:q query :per_page 100}})]
      (into [] (map parse-issue (:items res))))))

(defn- parse-commit
  [user repo commit]
  {:user user
   :commit {:title (:message (:commit commit))
            :repo repo
            :date (:date (:author (:commit commit)))
            :url (:html_url commit)}})

(defn repo-commits
  "fetches user's public issues and PRs across github"
  [gh]
  (fn [{user :user repo :repo :as record}]
    (let [query (str "author:" user)
          res (gh {:path (str "/repos/" repo "/commits") :query {:author user :per_page 100}})]
      (if (map? res)
        []
        (into [] (map (partial parse-commit user repo) res))))))
