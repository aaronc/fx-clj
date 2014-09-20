(ns fx-clj.core.binding
  (:require
    [fx-clj.core.pset :as pset])
  (:import
    [fx_clj.binding ObservableValueRef]))

(defn property-ref [target property-name]
  (let [prop (pset/get-property target property-name)]
    (ObservableValueRef. prop)))