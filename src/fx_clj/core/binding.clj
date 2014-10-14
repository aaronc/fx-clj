(ns fx-clj.core.binding
  (:require
    [fx-clj.core.pset :as pset]
    [fx-clj.core.extensibility :refer [convert-arg]]
    [freactive.core :as r])
  (:import
    [fx_clj.binding ObservableValueRef ReactiveRefObservable]
    (javafx.beans.value ObservableValue)
    (clojure.lang IDeref IReactiveLookup)
    (javafx.beans.property Property)))

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

(defn bind<-
  ([^Property lhs rhs]
   (let [rhs (convert-arg ObservableValue rhs nil)]
     (.bind lhs rhs)))
  ([rhs]
   (pset/->BindingClosure (fn [lhs] (bind<- lhs rhs)))))

(defn bind<->
  ([^Property lhs rhs]
   (let [rhs (convert-arg Property rhs nil)]
     (.bindBidirectional lhs rhs)))
  ([rhs]
   (pset/->BindingClosure (fn [lhs] (bind<-> lhs rhs)))))

(defn bind->
  ([^ObservableValue lhs rhs]
   (let [rhs (convert-arg Property rhs nil)]
     (.bind rhs lhs)))
  ([rhs]
   (pset/->BindingClosure (fn [lhs] (bind-> lhs rhs)))))

