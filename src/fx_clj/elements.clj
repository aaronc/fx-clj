(ns ^:no-doc fx-clj.elements
  (:import (javafx.scene Scene))
  (:require
    [fx-clj.impl.elements :refer [element-factories]]
    [fx-clj.core.pset :as pset]
    [fx-clj.core.run :as run]
    [fx-clj.impl.util :refer [javadoc-link]]
    [camel-snake-kebab.core :as csk]))

(def ^:private
  default-arglists '([id-class-kw? property-map? & content-or-children*]))

(defn- create-element-closure [cls factory]
  (let [def-prop-closure
        (pset/lookup-default-property-closure cls)]
    (fn create-element [& args]
      (run/run<!!
        (pset/pset!* (factory) args def-prop-closure)))))

(doseq [[ename factory] @element-factories]
  (let [cls
        (:fx-clj.impl.elements/class
         (meta factory))

        doc-string
        (str "Creates an instance of " (javadoc-link cls)
             ".\nArguments processed as in [[pset!]].")

        closure (create-element-closure cls factory)

        sym (csk/->kebab-case-symbol ename)

        sym (with-meta sym
                       {:doc doc-string
                        :doc/format :markdown
                        :arglists default-arglists
                        :name sym
                        :ns *ns*})]

    (intern *ns* sym closure)))

(defn scene
  {:doc (str "Creates an instance of " (javadoc-link Scene) " with the specified root node.")
   :doc/format :markdown}
  ([root] (Scene. root)))
