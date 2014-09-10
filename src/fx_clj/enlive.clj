(ns ^:no-doc fx-clj.enlive
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

(defn at
  "An enlive-like transformation function.
  Takes a context node and pairs of css selector strings
  and transform arguments. Currently the only supported transform
  is the property map transform (a property map that could be passed to pset!.

  Example: (at my-node \"#my-element\" {:text \"Some text\"})

  Note: additional transforms can be registered using the
  fx-clj.enlive/do-transform multimethod."
  [node & sel-xform-pairs]
  (run!
    (doseq [[selector xform] (partition 2 sel-xform-pairs)]
      (invoke-transform node selector xform))))

(comment
  (defmacro at [node & sel-xform-pairs]
    `(fx-clj.core.run/run!
       (fx-clj.enlive/at))))
