(ns scraper.github-client-test
  (:use clojure.test)
  (:require [clojure.core.async :refer [go >! <! <!! chan timeout close! alts!]]
            [scraper.github-client :as gh]))

(defn test-async
  "Asynchronous test awaiting ch to produce a value or close."
  [ch]
  (<!! ch))

(defn test-within
  "Asserts that ch does not close or produce a value within ms. Returns a
  channel from which the value can be taken."
  [ms ch]
  (go (let [t (timeout ms)
            [v ch] (alts! [ch t])]
        (is (not= ch t)
            (str "Test should have finished within " ms "ms."))
        v)))

(defn my-get
  [path options cb]
  (cb {:status 200 :headers {:foo "bar"} :body "{\"oy\": \"yo\"}"}))

(def mock-http-functions
  {:get my-get
   :post my-get})


(deftest gh-client-does-requests
  "Github Client runs a request"
  (let [req-chan (chan)
        res-chan (chan)
        req {:method :get :path "/foo" :query {:page 2} :resp res-chan}
        gh (gh/request req-chan mock-http-functions)]
    (go (>! req-chan req))
    (test-async
      (test-within 5000
        (go (is (= (<! res-chan) {:oy "yo"})))))))
