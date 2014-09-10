(ns ^:no-doc fx-clj.enlive
  (:require
    [fx-clj.core.extensibility :refer [do-transform]]
    [fx-clj.core.pset :as pset]
    [fx-clj.core.run :refer [run!]]
    [fx-clj.core.transforms]))

(defmethod do-transform clojure.lang.PersistentArrayMap
           [node prop-map]
  (pset/do-pset!* node prop-map nil nil))

(defn- invoke-transform [node selector xform]
  (let [nodes (.lookupAll node selector)]
    (doseq [n nodes]
      (do-transform n xform))))

(defn at!
"An enlive-like transformation function. Takes a context node and pairs of
css selector strings and transform arguments.  If a single transform argument
is supplied after node (instead of sel-xform-pairs), that transformation will
be applied to the provided node.

Available transforms include:
- the property-map transform: a map that could be passed to [[pset!]]
- [[add-class!]]
- [[remove-class!]]

Examples:

```clojure
(at! my-node \"#my-element\" {:text \"Some text\"})
(at! my-node \"#my-element\" (add-class! \"some-class\"))
(at! my-node {:text \"Some text\"})
```

Note: Additional transforms can be registered (see the
fx-clj.core.extensibility namespace for details)."
  {:doc/format :markdown}
  [node & sel-xform-pairs]
  (if (= 1 (count sel-xform-pairs))
    (do-transform node (first sel-xform-pairs))
    (doseq [[selector xform] (partition 2 sel-xform-pairs)]
      (invoke-transform node selector xform))))

