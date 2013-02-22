(ns hallacy.elevations
  (:import [hallacy.utils Elevation]))

(def ELEV (Elevation.))

(defn- lat-lon-str [place]
  (str (place :latitude) "," (place :longitude)))

(defn get-elevations
  "Given a collection of lat/long String pairs, returns the result
   Map from the Elevation library"
  [elvs]
  (println "get-elevations (slow version)")
  (.getElevations ELEV
                  (java.util.ArrayList. elvs)))

(def get-elevations-memoized (memoize get-elevations))

(defn with-elevations
  "Returns places with :elevation added.
   Expects each place record to be a Factual record. Specifically, it
   must have:
     :latitude
     :longitude"
  [places]
  (let [elvs (get-elevations-memoized (sort (distinct (map lat-lon-str places))))]
    (map
     (fn [place]
       (assoc place :elevation
              (get elvs (lat-lon-str place))))
     places)))
