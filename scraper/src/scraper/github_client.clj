(ns scraper.github-client
  (:require [clojure.core.async :refer [go <! >!]]
            [clojure.data.json :as json]))

(defn request
  "Starts a go block that takes requests on a channel and responds to the channel
  in the request"
  [requests-channel http-functions]
  (go (while true
        (let [req (<! requests-channel)
              {:keys [method path query resp]} req
              http-func (http-functions method)
              options {:query-params query}]
          (http-func path options
            (fn [{:keys [status headers body error]}]
              (if error
                (println "Request failed" req "exception" error))
              (if resp
                (go (>! resp (json/read-str body :key-fn keyword))))))))))
