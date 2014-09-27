(ns fx-clj.core.binding
  (:require
    [fx-clj.core.pset :as pset]
    [freactive.core :as r])
  (:import
    [fx_clj.binding ObservableValueRef ReactiveRefObservable]
    (javafx.beans.value ObservableValue)
    (clojure.lang IDeref IReactiveLookup)))

(defn property-ref [target property-name]
  (let [prop (pset/get-property target property-name)]
    (ObservableValueRef. prop)))

(defn observable-property [target getter]
  (let [getter (cond
                 (number? getter) (fn [x] (get x getter))
                 (string? getter) (fn [x] (get x getter))
                 :default getter)
        cur-value (getter target)]
    (if (instance? ObservableValue cur-value)
      cur-value
      (ReactiveRefObservable.
        (cond
          (instance? IReactiveLookup target)
          (r/reactive (getter target))

          (instance? IDeref target)
          (r/reactive (getter @target))

          :default
          (r/reactive (getter target)))))))

