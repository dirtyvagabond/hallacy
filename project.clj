(defproject hallacy "0.0.1"
  :description "Webservice for Chris Hallacy's Factual Hackathon project"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [factual/factual-clojure-driver "1.3.1"]
                 [factual/sosueme "0.0.14"]
                 [cheshire "4.0.1"]
                 [ring "1.1.6"]
                 [org.codehaus.jackson/jackson-core-asl "1.9.9"]
                 [org.codehaus.jackson/jackson-mapper-asl "1.9.9"]
                 [org.apache.httpcomponents/httpcore "4.2.2"]
                 [commons-codec "1.6"]
                 [commons-logging "1.1.1"]
                 [commons-httpclient "3.1"]
                 [org.apache.httpcomponents/httpclient "4.2.3"]
                 [ring/ring-jetty-adapter "1.1.6"]]
  :ring {:handler hallacy.core/handler}
  :plugins [[lein-ring "0.8.2"]]
  :main hallacy.core
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  )
