(ns scraper.github-client
  (:require [clojure.data.json :as json]))

(def github-auth ["charypar" "c43b49351006803d00ee0da0ecf550d06eb59793"]) ; can only read org members
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
  [http-get]
  (let [gh (request http-get)]
    (fn [org]
      (let [res (gh {:path (str "/orgs/" org "/members") :query {:per_page 500}})]
        (into [] (map :login res))))))

; (defn request
;   "Starts a go block that takes requests on a channel and responds to the channel
;   in the request"
;   [input-channel http-functions]
;   (println "Github API client starting...")
;   (go (while true
;         (let [req (<! input-channel)]
;           (if req
;             (let [{:keys [method path query resp]} req
;                   http-func (http-functions method)
;                   options {:query-params (or query {}) :basic-auth github-auth}]
;               (println ">" method path (str query))
;               (http-func (str github-base-path path) options
;                 (fn [{:keys [status headers body error]}]
;                   (println "<" method path status)
;                   (if error
;                     (println "Request failed" req "exception" error))
;                   (if resp
;                     (go (>! resp (json/read-str body :key-fn keyword))))))))))))
;
; (defn org-members
;   "Fetches members of an organisation passed on a channel and outputs users into another channel"
;   [input-channel output-channel request-channel]
;   (let [resp (chan)]
;     (println "Org members starting...")
;     (go (while true
;           (let [org-name (<! input-channel)]
;             (if org-name
;               (let [path (str "/orgs/" org-name "/members")
;                     req {:method :get :path path :resp resp}]
;                 (println "Fetching members of" org-name "req" (str req))
;                 (>! request-channel req)
;                 (let [members (<! resp)
;                       names (map :login members)]
;                   (println "got" members "names" names)
;                   (>! output-channel names)
;                   (println "names written to output")))
;             (close! resp)))))))
