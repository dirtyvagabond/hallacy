(ns hallacy.elevations
  (:import [hallacy.utils Elevation]))

(def ELEV (Elevation.))

(defn- lat-lon-str [place]
  (str (place :latitude) "," (place :longitude)))

(defn with-elevations
  "Returns

   Expects each place record to be a Factual record. Specifically, it
   must have:
     :latitude
     :longitude"
  [places]
  (let [arr  (java.util.ArrayList. (map lat-lon-str places))
        elvs (.getElevations ELEV arr)]
    (map
     (fn [place]
       (assoc place :elevation
              (get elvs (lat-lon-str place))))
     places)))
