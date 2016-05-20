(ns web.fixtures
  (:require [cljs-time.coerce :as coerce]
            [clojure.test.check.generators :as gen]))

(def users
  ["User A", "User B", "User C", "User D", "User E"])

(defn to-user
  [name]
  [:user/name name])

(def date-generator
  (gen/choose
    (coerce/to-long "2015-09-01")
    (coerce/to-long "2016-01-01")))

(def contribution
  "Contribution generator"
  (gen/hash-map
    :contribution/id gen/uuid
    :contribution/user (gen/fmap to-user (gen/elements users))
    :contribution/repository gen/string-ascii
    :contribution/date-created date-generator
    :contribution/date-public date-generator
    :contribution/type (gen/elements [:contribution/commit :contribution/issue :contribution/pr])
    :contribution/languages (gen/elements [ "Clojure", "CSS", "Html", "Javascript", "Ruby" ])
    :contribution/url gen/string-ascii))
