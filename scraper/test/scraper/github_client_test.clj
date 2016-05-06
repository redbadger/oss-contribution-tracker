(ns scraper.github-client-test
  (:use clojure.test)
  (:require [scraper.github-client :as gh]))

(deftest parses-link-header
  (let [{:keys [url query]} (gh/next-page "<https://foo.com/bar?page=3&per_page=10&hello=hi>; rel=\"next\", ")]
    (is (= url "https://foo.com/bar")
    (is (= query {:page "3" :per_page "10" :hello "hi"})))))

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

(defn paged-get [url options]
  (let [page (:page (:query-params options))]
    (if (not (= page "2"))
      (let [p (promise)]
        (is (= url "https://api.github.com/foo"))
        (deliver p {:status 200
                    :headers {:Link "<https://api.github.com/bar?page=2>; rel=\"next\", <https://api.github.com/bar?page=2>; rel=\"last\""}
                    :body (str "[{\"url\": \""url"\"}]")}))
      (let [p (promise)]
        (deliver p {:status 200
                    :headers {}
                    :body (str "[{\"url\": \""url"\"}]")})))))

(deftest follows-pagination
  "client follows pagination and augments the collection"
  (let [req {:method :get :path "/foo"}
        client (gh/request paged-get)
        res (client req)]
    (is (= res [{:url "https://api.github.com/foo"} {:url "https://api.github.com/bar"}]))))

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
                   :issue {:type :pull_request, :url "https://github.com/redbadger/oss-contribution-tracker/pull/1"}}
                  {:repo "dowjones/react-json-schema-proptypes"
                   :user "charypar"
                   :issue {:type :issue, :url "https://github.com/dowjones/react-json-schema-proptypes/issues/2"}}]]
    (is (= issues expected))))

; (deftest gets-repos-master-commits-by-user
;   "client can get commits in the default branch by a user"
;   (let [repo-commits (gh/repo-commits (gh/request repo-commits))
;         commits (repo-commits "charypar")]))
