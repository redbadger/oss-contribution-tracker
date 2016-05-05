(ns scraper.core
  (:require [clojure.core.async :refer [thread chan close! <!! >!!]]
            [org.httpkit.client :as http]
            [scraper.github-client :as github]))

(defn -main []
  (let [org-members (github/org-members http/get)
        org-to-members (chan 1 (mapcat org-members))]
    (thread
      (>!! org-to-members "redbadger")
      (close! org-to-members))
    (loop []
      (let [member (<!! org-to-members)]
        (when member
          (println "member:" member)
          (recur))))))
