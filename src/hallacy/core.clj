(ns hallacy.core
  (:require [factual.api :as facts]
            [cheshire.core :as json]
            [sosueme.conf :as conf])
  (:use [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.params]))

(let [{:keys [key secret]} (conf/dot-factual "factual-auth.yaml")]
       (facts/factual! key secret))

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
  (println "get-places-body Q:" query)
  (let [places (get-places query)]
    (json/generate-string
     {:status "OK"
      :num_results (count places)
      :results (translate places)})))

;;Factual: longitude=-118.418249&latitude=34.060106
(defn params->query
  "Translates raw query params map to a Factual query map"
  [params]
  (merge
   {:geo {:$circle {:$center [(params "latitude"), (params "longitude")]
                    :$meters 5000}}
    :table      "restaurants-us"}
  (when-let [v (params "category")] {:filters {:category_ids {:$eq  v}}})
  (when-let [v (params "table")]    {:table v})
  (when-let [v (params "search")]   {:q v})))

(defn handler [{params :params :as req}]
  (println "REQ:" req)
  (println "HANDLER PARAMS:" params)
  {:status  200
   :headers {"Content-Type" (if (params "plain")
                              "text/plain"
                              "application/mixare-json")}
   :body    (get-places-body (params->query params))})

(def app
  (wrap-params handler))

(defn -main [port]
  (run-jetty app {:port (Integer. port)}))
