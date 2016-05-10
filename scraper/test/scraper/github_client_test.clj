(ns scraper.github-client-test
  (:use clojure.test)
  (:require [scraper.github-client :as gh]
            [clojure.core.async :refer [>!! <!! thread alts!! timeout chan]]))

(deftest parses-link-header
  (let [{:keys [url query]} (gh/next-page "<https://foo.com/bar?page=3&per_page=10&hello=hi>; rel=\"next\", ")]
    (is (= url "https://foo.com/bar")
    (is (= query {:page "3" :per_page "10" :hello "hi"})))))

(defn basic-get [url options]
  (let [p (promise)]
    (deliver p {:status 200
                :headers {:foo "bar"}
                :body (str "{\"url\": \"" url "\", \"auth\": " (into [] (map str (:basic-auth options))) "}")})))


(deftest does-request
  "client sends requests and parses responses"
  (let [client (gh/request basic-get)
        res (client {:path "/foo" :query {:page 2}})]
    (is (= res {:url "https://api.github.com/foo" :auth gh/github-auth}))))

(def ten-minutes-from-now (+ (* 10 60) (quot (System/currentTimeMillis) 1000)))

(defn paged-get [url options]
  (let [page (:page (:query-params options))]
    (if (not (= page "2"))
      (let [p (promise)]
        (is (= url "https://api.github.com/foo"))
        (deliver p {:status 200
                    :headers {:Link "<https://api.github.com/bar?page=2>; rel=\"next\", <https://api.github.com/bar?page=2>; rel=\"last\""
                              :x-ratelimit-remaining (str (* 10 60 10)) ; 100 ms pause between requests
                              :x-ratelimit-reset (str ten-minutes-from-now)}
                    :body (str "[{\"url\": \"" url "\"}]")}))
      (let [p (promise)]
        (deliver p {:status 200
                    :headers {}
                    :body (str "[{\"url\": \"" url "\"}]")})))))

(deftest perform-request
  "client performs a single request returning body and relevant header information"
  (let [res (gh/perform-request paged-get :core "https://api.github.com/foo")]
    (is (= (:body res) [{:url "https://api.github.com/foo"}]))
    (is (= (:next-page res) {:url "https://api.github.com/bar" :query {:page "2"}}))
    (is (= (:rate-limit res) {:scope :core :remaining 6000 :reset ten-minutes-from-now}))))

(deftest follows-pagination
  "client follows pagination and augments the collection"
  (let [req {:path "/foo"}
        client (gh/request paged-get)
        res (client req)]
    (is (= res [{:url "https://api.github.com/foo"} {:url "https://api.github.com/bar"}]))))

(defn monitored-paged-get
  [chan]
  (fn [url options]
    (thread (>!! chan :paged-get)) ; notify the observer before calling paged-get
    (paged-get url options)))

(deftest runs-requests-at-the-limit-rate
  "client runs requests at the maximum allowed rate"
  (let [req {:path "/foo"}
        notif-ch (chan)
        gh (gh/request (monitored-paged-get notif-ch))
        res-ch (thread (gh req))
        timeout (timeout 90)] ; kick off the fetch asynchronously
    (is (= (<!! notif-ch) :paged-get)) ; read the first notification
    (let [[val ch] (alts!! [notif-ch timeout])] ; timeout should come before the second notification
      (println "Alts fired with val" val "on channel" ch)
      (is (= val nil) "Timeout should fire before the next request")
      (is (= ch timeout) "Timeout should fire before the next request")
      (<!! res-ch))))

(defn org-get [url options]
  (let [p (promise)]
    (is (= url "https://api.github.com/orgs/redbadger/members"))
    (deliver p {:status 200
                :headers {}
                :body (slurp "test/scraper/fixtures/org-members.json")})))

(deftest gets-org-members
  "client gets org members"
  (let [org-mem (gh/org-members (gh/request org-get))
        members (org-mem {:org "redbadger"})]
    (is (= members [{:user "ajcumine"} {:user "AmyBadger"}]))))

(defn user-repos [url options]
  (let [p (promise)]
    (is (= url "https://api.github.com/users/charypar/repos"))
    (deliver p {:status 200
                :headers {}
                :body (slurp "test/scraper/fixtures/user-repos.json")})))

(deftest gets-users-repos
  "client gets user's repos"
  (let [usr-rep (gh/user-public-repos (gh/request user-repos))
        repos (usr-rep {:user "charypar"})]
    (is (= repos [{:repo "charypar/comp" :user "charypar"} {:repo "charypar/cyclical" :user "charypar"}]))))

(defn user-orgs [url options]
  (let [p (promise)]
    (is (= url "https://api.github.com/users/kittens/orgs"))
    (deliver p {:status 200
                :headers {}
                :body (slurp "test/scraper/fixtures/user-orgs.json")})))

(deftest get-users-orgs
  "client gets user's organisations"
  (let [usr-orgs (gh/user-orgs (gh/request user-orgs))
        repos (usr-orgs {:user "kittens"})]
    (is (= repos [{:org "facebook" :user "kittens"}
                  {:org "reactjs" :user "kittens"}
                  {:org "babel" :user "kittens"}
                  {:org "koral" :user "kittens"}]))))

(defn org-repos [url options]
  (let [p (promise)]
    (is (= url "https://api.github.com/orgs/redbadger/repos"))
    (is (= (:query-params options) {:type "public" :per_page 100}))
    (deliver p {:status 200
                :headers {}
                :body (slurp "test/scraper/fixtures/org-repos.json")})))

(deftest gets-orgs-repos
  "client gets org's repos"
  (let [org-rep (gh/org-public-repos (gh/request org-repos))
        repos (org-rep {:org "redbadger" :user "charypar"})]
    (is (= repos [{:repo "redbadger/CloudFolderBackup" :user "charypar"}
                  {:repo "redbadger/ScriptDependencyResolver" :user "charypar"}]))))

(defn user-issues [url options]
  (let [p (promise)]
    (is (= url "https://api.github.com/search/issues"))
    (is (= (:query-params options) {:q "author:charypar" :per_page 100}))
    (deliver p {:status 200
                :headers {}
                :body (slurp "test/scraper/fixtures/user-issues.json")})))

(deftest gets-users-issues
  "client can get users's issues and pull reuqests"
  (let [usr-issues (gh/user-issues (gh/request user-issues))
        issues (usr-issues {:user "charypar"})
        expected [{:repo "redbadger/oss-contribution-tracker"
                   :user "charypar"
                   :issue {:type :pull-request
                           :title "[WIP] Github API client layer"
                           :date-created "2016-05-05T11:19:10Z"
                           :url "https://github.com/redbadger/oss-contribution-tracker/pull/1"}}
                  {:repo "dowjones/react-json-schema-proptypes"
                   :user "charypar"
                   :issue {:type :issue
                           :title "Add travis integration and readme badges"
                           :date-created "2016-04-15T18:04:12Z"
                           :url "https://github.com/dowjones/react-json-schema-proptypes/issues/2"}}]]
    (is (= issues expected))))

(defn repo-commits [url options]
  (let [p (promise)]
    (is (= url "https://api.github.com/repos/redbadger/oss-contribution-tracker/commits"))
    (is (= (:query-params options) {:author "charypar" :per_page 100}))
    (deliver p {:status 200
                :headers {}
                :body (slurp "test/scraper/fixtures/repo-commits.json")})))

(deftest gets-repos-master-commits-by-user
  "client can get commits in the default branch by a user"
  (let [repo-commits (gh/repo-commits (gh/request repo-commits))
        commits (repo-commits {:user "charypar" :repo "redbadger/oss-contribution-tracker"})
        expected [{:user "charypar"
                   :commit {:title "Initial commit with a scraper library skeleton"
                            :date-created "2016-05-03T09:27:22Z"
                            :url "https://github.com/redbadger/oss-contribution-tracker/commit/ce820f8f2b4a2829277fea52df73da055ba13ea1" }}]]
    (is (= commits expected))))
