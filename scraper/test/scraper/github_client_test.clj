(ns scraper.github-client-test
  (:use clojure.test)
  (:require [scraper.github-client :as gh]))

(defn basic-get [url options]
  (let [p (promise)]
    (deliver p {:status 200
                :headers {:foo "bar"}
                :body (str "{\"url\": \"" url "\", \"auth\": " (str (:basic-auth options)) "}")})))

(deftest does-request
  "client sends requests and parses responses"
  (let [req {:method :get :path "/foo" :query {:page 2}}
        client (gh/request basic-get)
        res (client req)]
    (is (= res {:url "https://api.github.com/foo" :auth gh/github-auth}))))

(defn org-get [url options]
  (let [p (promise)]
    (deliver p {:status 200
                :headers {}
                :body (slurp "test/scraper/fixtures/org-members.json")})))

(deftest gets-org-members
  "client gets org members"
  (let [org-mem (gh/org-members org-get)
        members (org-mem "redbadger")]
    (is (= members ["ajcumine" "AmyBadger"]))))

(defn user-repos [url options]
  (let [p (promise)]
    (deliver p {:status 200
                :headers {}
                :body (slurp "test/scraper/fixtures/user-repos.json")})))

(deftest gets-users-repos
  "client gets user's repos"
  (let [usr-rep (gh/user-public-repos user-repos)
        repos (usr-rep "charypar")]
    (is (= repos ["comp" "cyclical"]))))
