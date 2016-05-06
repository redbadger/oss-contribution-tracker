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
  "fetches members of an organisation"
  [gh]
  (fn [user]
    (let [res (gh {:path (str "/users/" user "/repos") :query {:per_page 100}})]
      (into [] (map :name (filter #(not (or (:private %) (:fork %))) res))))))

(defn user-orgs
  "fetches members of an organisation"
  [gh]
  (fn [user]
    (let [res (gh {:path (str "/users/" user "/orgs") :query {:per_page 100}})]
      (into [] (map :login res)))))
