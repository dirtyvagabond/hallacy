(ns hallacy.core
  (:import [hallacy.utils Elevation])
  (:require [factual.api :as facts]
            [cheshire.core :as json]
            [sosueme.conf :as conf])
  (:use [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.params]))

(defn secrets []
  (println "ENV/FACT_KEY:" (System/getenv "FACT_KEY"))
  (if (empty? (System/getenv "FACT_KEY"))
    (conf/dot-factual "factual-auth.yaml")
    {:key (System/getenv "FACT_KEY")
     :secret (System/getenv "FACT_SECRET")}))


(def ^:dynamic *secrets* nil)

(defn init! []
  (when (nil? *secrets*)
    (let [{:keys [key secret]} (secrets)]
      (facts/factual! key secret))
    (println "INIT'd!")
    (def ^:dynamic *secrets* true)))

(defn get-places [query]
  (facts/fetch query))

(defn translate-place
  "Translate a place rec from Factual into something usable by mixair"
  [place]
  {"id"              (place :factual_id)
   "lat"             (place :latitude)
   "lng"             (place :longitude)
   "elevation"       "0"
   "title"           (place :name)
   "webpage"         (place :website)
   "has_detail_page" (if (place :website) "1" "0")})

(defn translate [places]
  (map translate-place places))

(defn get-places-body [query]
  (println "get-places-body: Q:" query)
  (let [places (get-places query)]
    (println "get-places-body: count:" (count places))
    (json/generate-string
     {:status      "OK"
      :num_results (count places)
      :results     (translate places)})))

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
  (init!)
  (println "REQ:" req)
  (println "HANDLER PARAMS:" params)
  {:status  200
   :headers {"Content-Type" (if (params "plain")
                              "text/plain"
                              "application/mixare-json")}
   :body
   ;;(slurp "brandon.json")
   (get-places-body (params->query params))
   })

(defn handler [{params :params :as req}]
  (println "HANDLER params:" params)
  (println "HANDLER req:" req)
  (if (and params (params "latitude") (params "longitude"))
    (respond-with-places req)
    {:status  200
     :headers {"Content-Type" "text/plain"}
     :body "Hello! Please send in a latitude and longitude via the query string! version a"}))

(def app
  (wrap-params handler))

(defn -main [port]
  (run-jetty app {:port (Integer. port)}))
