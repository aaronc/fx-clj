(ns ^:no-doc fx-clj.core.transforms
  (:require
    [fx-clj.core.extensibility :refer
     [def-transform-tag do-transform convert-arg]]))

;(def-transform-tag append! ::append
;  "Appends an element or list elements to the DefaultProperty
;of the target node if a node is specified or to the end of a
;List is a list is specified."
;  [& items])

;(defmethod do-transform ::append [node [& items]]
;  (or
;    (when-let [def-property (fx-clj.core.pset/lookup-default-property-closure
;                              (class node))]
;      (def-property node items)
;      )

(def-transform-tag
  add-class! ::add-class!
  "A transform for use with at!. Adds CSS class(es) to the selected node."
  [& classes])

(defmethod do-transform ::add-class! [node [& classes]]
  (.addAll (.getStyleClass node) (into-array String classes)))

(def-transform-tag
  remove-class! ::remove-class!
  "A transform for use with at!. Removes CSS class(es) from the selected node."
  [& classes])

(defmethod do-transform ::remove-class! [node [& classes]]
  (.removeAll (.getStyleClass node) (into-array String classes)))
