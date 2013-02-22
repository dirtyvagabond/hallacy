(defproject hallacy "0.0.1"
  :description "Webservice for Chris Hallacy's Factual Hackathon project"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [factual/factual-clojure-driver "1.3.1"]
                 [factual/sosueme "0.0.14"]
                 [cheshire "4.0.1"]
                 [ring/ring-jetty-adapter "1.1.0"]]
  ;;:ring {:handler hallacy.core/handler}
  ;;:plugins [[lein-ring "0.8.2"]]
  )