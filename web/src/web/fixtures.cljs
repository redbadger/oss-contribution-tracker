(ns web.fixtures
  (:require [cljs-time.coerce :as time]
            [clojure.test.check.generators :as gen]
            [goog.string :refer (format)]))

(def contribution
  "Contribution generator"
  (gen/hash-map
    :contribution/id gen/uuid
    :contribution/user (gen/elements [ "User A", "User B", "User C", "User D", "User E" ])
    :contribution/repository gen/string-ascii
    :contribution/date-created (gen/fmap time/from-long (gen/choose 1449000000000 1450000000000))
    :contribution/date-public (gen/fmap time/from-long (gen/choose 1449000000000 1450000000000))
    :contribution/type (gen/elements [:contribution/commit :contribution/issue :contribution/pr])
    :contribution/languages (gen/elements [ "Clojure", "CSS", "Html", "Javascript", "Ruby" ])
    :contribution/url gen/string-ascii))
