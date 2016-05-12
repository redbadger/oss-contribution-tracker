(ns scraper.storage-test
  (:use clojure.test)
  (:require [scraper.storage :as db]
            [datomic.api :as d]))

(deftest tx-data-commit
  "Maps a commit contribution into a datomic insertion"
  (let [commit {:type :commit
                :repo "foo/bar"
                :user "bob"
                :date "2016-05-01T12:30:21Z"
                :title "Hi"
                :url "http://example.com/hello"}
        tx-data (db/contribution-tx-data commit 0)
        expected [{:db/id #db/id[:db.part/user -1]
                   :author/username "bob"}
                  {:db/id #db/id[:db.part/user -2]
                   :repository/name "foo/bar"}
                  {:db/id #db/id[:db.part/user -3]
                   :contribution/type :contribution.type/commit
                   :contribution/author #db/id[:db.part/user -1]
                   :contribution/repository #db/id[:db.part/user -2]
                   :contribution/date  #inst "2016-05-01T12:30:21.000000Z"
                   :contribution/title "Hi"
                   :contribution/url "http://example.com/hello"}]]
    (doseq [[datum exp] (map vector tx-data expected)]
      (doseq [key (keys datum)]
        (is (or (= (datum key) (exp key))
                (= 0 (compare (datum key) (exp key))))
            (str (datum key) " should be " (exp key)))))))

(deftest tx-data-issue
  "Maps an issue contribution into a datomic insertion"
  (let [commit {:type :issue
                :repo "foo/bar"
                :user "bob"
                :date "2016-05-01T12:30:21Z"
                :title "Hi"
                :url "http://example.com/hello"}
        tx-data (db/contribution-tx-data commit 0)
        expected [{:db/id #db/id[:db.part/user -1]
                   :author/username "bob"}
                  {:db/id #db/id[:db.part/user -2]
                   :repository/name "foo/bar"}
                  {:db/id #db/id[:db.part/user -3]
                   :contribution/type :contribution.type/issue
                   :contribution/author #db/id[:db.part/user -1]
                   :contribution/repository #db/id[:db.part/user -2]
                   :contribution/date  #inst "2016-05-01T12:30:21.000000Z"
                   :contribution/title "Hi"
                   :contribution/url "http://example.com/hello"}]]
    (doseq [[datum exp] (map vector tx-data expected)]
      (doseq [key (keys datum)]
        (is (or (= (datum key) (exp key))
                (= 0 (compare (datum key) (exp key))))
            (str (datum key) " should be " (exp key)))))))

(deftest tx-data-pr
  "Maps a PR contribution into a datomic insertion"
  (let [commit {:type :pull-request
                :repo "foo/bar"
                :user "bob"
                :date "2016-05-01T12:30:21Z"
                :title "Hi"
                :url "http://example.com/hello"}
        tx-data (db/contribution-tx-data commit 0)
        expected  [{:db/id #db/id[:db.part/user -1]
                    :author/username "bob"}
                   {:db/id #db/id[:db.part/user -2]
                    :repository/name "foo/bar"}
                   {:db/id #db/id[:db.part/user -3]
                    :contribution/type :contribution.type/pull-request
                    :contribution/author #db/id[:db.part/user -1]
                    :contribution/repository #db/id[:db.part/user -2]
                    :contribution/date  #inst "2016-05-01T12:30:21.000000Z"
                    :contribution/title "Hi"
                    :contribution/url "http://example.com/hello"}]]
    (doseq [[datum exp] (map vector tx-data expected)]
      (doseq [key (keys datum)]
        (is (or (= (datum key) (exp key))
                (= 0 (compare (datum key) (exp key))))
            (str (datum key) " should be " (exp key)))))))
