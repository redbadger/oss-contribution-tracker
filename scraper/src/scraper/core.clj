(ns scraper.core
  (:require [org.httpkit.client :as http]
            [scraper.github-client :as github]))

(def gh (github/request http/get))

(def tx-stack
  (comp
    (mapcat (github/org-members gh))
    (filter identity)
    (mapcat (github/user-public-repos gh))))

(defn -main []
  (doseq [repo (sequence tx-stack ["redbadger"])]
    (println "repo:" repo)))
