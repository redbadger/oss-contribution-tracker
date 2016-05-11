(defproject scraper "0.1.0-SNAPSHOT"
  :description "Scrapes open source contributions made by organisation members"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username [:gpg :env/my-datomic-username]
                                   :password [:gpg :env/my-datomic-password]}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [com.datomic/datomic-pro "0.9.5359"]
                 [http-kit "2.1.18"]
                 [org.clojure/data.json "0.2.6"]
                 [environ "1.0.3"]
                 [org.clojure/tools.logging "0.3.1"]]
  :main scraper.core)
