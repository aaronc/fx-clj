(ns fx-clj.core.extensibility
  "Hooks for extending fx-clj functionality."
  (:require
    [fx-clj.impl.bootstrap]))

(defmulti convert-arg
  "Converts arguments sent to the pset! function and
  other functions which take similar arguments.

  Implementing methods take three parameters:
  the requested type, the value to be converted, and an optional options map.
  Methods dispatch on a vector of two Java Class instances:
  the requested type to be converted to and the type to convert from."
  (fn [requested-type value opts] [requested-type (type value)]))

(def defined-transforms
  "An atom with the set of defined transform vars
  (usually defined by def-transform-tag)."
  (atom #{}))

(defmacro def-transform-tag
  "Defines a transform tag to be used as an argument to
  to do-transform. The actual transform will be taken care of
  by defining a do-transform method for the tag. The function
  defined by def-transform-tag will simply return a vector
  of the args passed in and the :type metadata set to tag.

  Example usage:

  ```clojure
  (def-transform-tag
    add-class! ::add-class!
    \"A transform for use with at!. Adds CSS class(es) to the selected node.\"
    [& classes])

  (defmethod do-transform ::add-class! [node [& classes]]
    (.addAll (.getStyleClass node) (into-array String classes)))
  ```
  "
  {:doc/format :markdown}
  [name tag doc args]
  (let [vararg (some #{'&} args)
        fdef
        (if vararg
          `(defn ~name ~doc
             {:arglists '([& args])}
             [& args#]
             (clojure.core/with-meta
               (clojure.core/vec args#) {:type ~tag}))
          `(clojure.core/defn ~name ~doc ~args
             (clojure.core/with-meta
               ~args {:type ~tag})))]
  `(let [v# ~fdef]
     (clojure.core/swap!
       fx-clj.core.extensibility/defined-transforms
       clojure.core/conj v#)
     v#)))


(defmulti do-transform
  "Applies an enlive-like transformation to a node.
  Takes two arguments: a node and a requested transform.
  Dispatches on the type of the transform."
  (fn [node transform] (type transform)))
