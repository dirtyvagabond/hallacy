(ns hallacy.core
  (:require [factql.core :as facts]
            [cheshire.core :as json])
  (:use
   [ring.adapter.jetty :only [run-jetty]]
   [ring.middleware.params]))

(facts/init!)

(defn get-places [lat lon]
  (facts/select places
                (around {:lat lat :lon lon :miles 1})))

(defn translate-place
  "Translate a place rec from Factual into something usable by mixair"
  [place]
  {"id" (place :factual_id)
   "lat" (place :latitude)
   "lng" (place :longitude)
   "elevation" "0"
   "title"           (place :name)
   ;;"distance"        "3"
   "webpage"         (place :website)
   "has_detail_page" (if (place :website) "1" "0")})

(defn translate [places]
  (map translate-place places))

(defn get-places-body [{lat :lat lon :lon}]
  ;;JSON
  (let [places (get-places lat lon)
        json (json/generate-string
              {:status "OK"
               :num_results (count places)
               :results (translate places)})]
    json))

(defn handler [{params :params :as req}]
  (println "REQ:" req)
  (println "HANDLER PARAMS:" params)
  {:status  200
   :headers {"Content-Type" (if (params "plain")
                              "text/plain"
                              "application/mixare-json")}
   :body    (get-places-body {:lat (params "latitude")
                              :lon (params "longitude")})})

#_(defn app [req]
  (println "REQ:" req)
  (println "OUT:" JSON)
  (let [lat (:query-string req)])
  {:status 200
   :headers {"Content-Type" "application/mixare-json"}
   :body JSON})

(def app
  (wrap-params handler))

(defn -main [port]
  (run-jetty app {:port (Integer. port)}))