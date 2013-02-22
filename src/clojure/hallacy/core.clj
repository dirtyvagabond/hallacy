(ns hallacy.core
  (:require [factual.api :as facts]
            [cheshire.core :as json]
            [sosueme.conf :as conf])
  (:use [hallacy.elevations]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.params]
        [clojure.pprint]))


(defn secrets []
  (if (empty? (System/getenv "FACT_KEY"))
    (conf/dot-factual "factual-auth.yaml")
    {:key (System/getenv "FACT_KEY")
     :secret (System/getenv "FACT_SECRET")}))

(defn init! []
  (let [{:keys [key secret]} (secrets)]
    (facts/factual! key secret)
    (println "init!'d!")))

(defn get-places [query]
  (facts/fetch query))

(defn translate-place
  "Translate a place rec from Factual into something usable by mixair"
  [place]
  {"id"              (place :factual_id)
   "lat"             (place :latitude)
   "lng"             (place :longitude)
   "elevation"       (place :elevation)
   "title"           (place :name)
   "webpage"         (place :website)
   "has_detail_page" (if (place :website) "1" "0")
   "payload"         place})

(defn get-body-struct [query]
  (let [places (with-elevations (get-places query))]
    {:status      "OK"
     :num_results (count places)
     :results     (map translate-place places)}))

;;Factual: longitude=-118.418249&latitude=34.060106
(defn params->query
  "Translates raw query params map to a Factual query map"
  [params]
  (merge
   {:geo    {:$circle {:$center [(params "latitude"), (params "longitude")]
                       :$meters 1000}}
    :table  "restaurants-us"}
  (when-let [v (params "category")] {:filters {:category_ids {:$eq  v}}})
  (when-let [v (params "table")]    {:table v})
  (when-let [v (params "search")]   {:q v})))

(defn respond-with-places [{params :params :as req}]
  (let [body-struct (get-body-struct (params->query params))
        test? (contains? params "plain")]
    {:status  200
     :headers {"Content-Type" (if test? "text/plain" "application/mixare-json")}
     :body    (if test?
                (with-out-str (pprint body-struct))
                (json/generate-string body-struct))}))

(defn handler [{params :params :as req}]
  (println "HANDLER params:" params)
  (println "HANDLER req:" req)
  (if (and params (params "latitude") (params "longitude"))
    (respond-with-places req)
    {:status  200
     :headers {"Content-Type" "text/plain"}
     :body "Hello! Please send in a latitude and longitude via the query string!\nrel: memo-elvs"}))

(def app
  (wrap-params handler))

(defn -main [port]
  (init!)
  (run-jetty app {:port (Integer. port)}))
