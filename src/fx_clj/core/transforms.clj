(ns fx-clj.core.transforms
  (:require
    [fx-clj.core.extensibility :refer
     [def-transform-tag do-transform convert-arg]]))

(def-transform-tag append! ::append
  "Appends an element or list elements to the DefaultProperty
of the target node if a node is specified or to the end of a
List is a list is specified."
  [& items])

;(defmethod do-transform ::append [node [& items]]
;  (or
;    (when-let [def-property (fx-clj.core.pset/lookup-default-property-closure
;                              (class node))]
;      (def-property node items)
;      )
