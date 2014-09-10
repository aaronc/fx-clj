(ns fx-clj.enlive
  (:require
    [fx-clj.core.pset :as pset]
    [fx-clj.core.run :refer [run!]]))

(defmulti do-transform (fn [node xform] (type xform)))

(defmethod do-transform clojure.lang.PersistentArrayMap
           [node prop-map]
  (pset/do-pset!* node prop-map nil nil))

(defn- invoke-transform [node selector xform]
  (let [nodes (.lookupAll node selector)]
    (doseq [n nodes]
      (do-transform n xform))))

(defn at [node & sel-xform-pairs]
  (run!
    (doseq [[selector xform] (partition 2 sel-xform-pairs)]
      (invoke-transform node selector xform))))

(comment
  (defmacro at [node & sel-xform-pairs]
    `(fx-clj.core.run/run!
       (fx-clj.enlive/at))))
