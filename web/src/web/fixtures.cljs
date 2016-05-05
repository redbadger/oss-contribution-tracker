(ns web.fixtures
  (:require [cljs-time.coerce :as time]
            [clojure.test.check.generators :as gen]
            [goog.string :refer (format)]))

(def contribution
  "Contribution generator"
  (gen/hash-map
    :id gen/uuid
    :user (gen/elements [ "User A", "User B", "User C", "User D", "User E" ])
    :repository gen/string-ascii
    :dateCreated (gen/fmap time/from-long (gen/choose 1449000000000 1450000000000))
    :datePublic (gen/fmap time/from-long (gen/choose 1449000000000 1450000000000))
    :type (gen/elements [:contribution/commit :contribution/issue :contribution/pr])
    :languages (gen/elements [ "Clojure", "CSS", "Html", "Javascript", "Ruby" ])
    :url gen/string-ascii))
