(ns ^:no-doc fx-clj.hiccup
  (:require
    [fx-clj.core.pset :as pset]
    [fx-clj.impl.elements :refer [element-factories]]
    [fx-clj.core.extensibility :refer [convert-arg]]
    [camel-snake-kebab.core :as csk]))

(def ^{:doc "Regular expression that parses a CSS-style id and class from an element name.
             From hiccup.compiler: https://github.com/weavejester/hiccup/blob/master/src/hiccup/compiler.clj
             EPL license."
       :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(def ^:private get-elem-factory
  (memoize
    (fn [elem-name]
      (get @element-factories (csk/->CamelCase elem-name)))))

(defn hiccup-construct [elem-kw]
  (assert (keyword? elem-kw) (str elem-kw " must be a keyword when using hiccup style construction"))
  (let [[_ elem-name id classes] (re-matches re-tag (name elem-kw))
        class-list (when classes (into [] (.split classes "\\.")))
        node ((get-elem-factory elem-name))]
    (pset/set-id+classes node id class-list)
    node))

(defn compile-fx
  "A hiccup-style element creation function.
  Takes a vector of the form [:tag-name#my-id.my-class property-map? content-or-children*]
  property-map? and content-or-children* are as in pset!.
  This function has more or less the same exact syntax as hiccup does for HTML,
  except for JavaFX."
  [[elem-kw & args]]
  (pset/pset!** (fx-clj.hiccup/hiccup-construct elem-kw) args))

(defmethod convert-arg [Object clojure.lang.PersistentVector] [_ v opts]
  (compile-fx v))

