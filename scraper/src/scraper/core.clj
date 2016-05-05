(ns scraper.core
  (:require [clojure.core.async :refer [thread go chan close! <! >! <!! >!!]]
            [org.httpkit.client :as http]
            [scraper.github-client :as github]))

(defn -main []
  (let [org-members (github/org-members http/get)
        usr-repos (github/user-public-repos http/get)
        org-to-members (chan 1 (comp (filter identity) (mapcat org-members)))
        user-to-repos (chan 1 (mapcat usr-repos))]
    (go
      (>! org-to-members "redbadger")
      (close! org-to-members))
    (go (while true
      (>! user-to-repos (<! org-to-members)))) ; every day I'm shoveling
    (loop []
      (let [repo (<!! user-to-repos)]
        (when repo
          (println "repo:" repo)
          (recur))))))
