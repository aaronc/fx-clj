(ns ^:no-doc fx-clj.core.pset
  (:require
    [fx-clj.core.run :as run]
    [fx-clj.core.convert]
    [fx-clj.core.extensibility :refer [convert-arg]]
    [camel-snake-kebab.core :as csk]
    [clojure.string :as str])
  (:import (java.lang.reflect TypeVariable ParameterizedType)
           (javafx.beans.value ObservableValue)
           (java.util Collection)
           (javafx.beans DefaultProperty)
           (javafx.application Platform)
           [clojure.lang IInvalidates IReactiveRef IRef]
           [fx_clj.binding ReactiveRefObservable RefObservable]))

(defn- get-generic-interfaces [cls]
  (loop [ifaces #{}
         cls cls]
    (if cls
      (let [new-ifaces (.getGenericInterfaces cls)
            new-ifaces (if (seq new-ifaces) (apply conj ifaces new-ifaces) ifaces)]
        (recur new-ifaces (.getSuperclass cls)))
      ifaces)))

(def ^:private get-property-type
  (memoize
    (fn [property-method]
      (let [rt (.getGenericReturnType property-method)
            gt (if (instance? Class rt)
                 (first (.getActualTypeArguments
                          (first (filter
                                   #(re-matches #".*javafx\.beans\.property\.Property.*" (.toString %))
                                   (get-generic-interfaces rt)))))
                 (first (.getActualTypeArguments rt)))]
        (cond
          (instance? Class gt)
          gt

          (instance? TypeVariable gt)
          Object

          (instance? ParameterizedType gt)
          (.getRawType gt)

          :default
          Object)))))

(def ^:private lookup-property-method
  (memoize
    (fn [target-type pname]
      (try
        (.getMethod target-type
                    (str (csk/->camelCaseString pname) "Property")
                    nil)
        (catch Exception ex
          nil)))))

(defn get-property [target pname]
  (when-let [pmethod (lookup-property-method (class target) pname)]
    (.invoke pmethod target nil)))

(defn- property-closure-fn [pmethod ptype target value]
  (let [prop (.invoke pmethod target nil)
        value (convert-arg ptype value nil)]
    (cond
      (instance? ObservableValue value) (.bind prop value)
      (instance? IReactiveRef value) (.bind prop (ReactiveRefObservable. value))
      (instance? IRef value) (.bind prop (RefObservable. value))
      :default (.setValue prop value))))

(defn- make-property-closure [target-type pname]
  (when-let [pmethod (lookup-property-method target-type pname)]
    (let [ptype (get-property-type pmethod)]
      (with-meta
        (fn [target value]
          (property-closure-fn pmethod ptype target value))
        {:property-type ptype
         :type ::property-closure}))))

(def ^:private lookup-list-property-method
  (memoize
    (fn [target-type pname]
      (try
        (.getMethod target-type
                    (str "get" (csk/->CamelCaseString pname))
                    nil)
        (catch Exception ex
          nil)))))

(defn- list-property-closure-fn [pmethod ptype target value]
  (cond
    (sequential? value)
    (let [list-value (.invoke pmethod target nil)]
      (doseq [v value]
        (let [v (convert-arg ptype v nil)]
          (.add list-value v))))

    :default
    (throw (ex-info (str "Don't know how to handle value " value " for  method "
                         pmethod " on " target)
                    {:method pmethod :target target :value value}))))

(defn- make-list-property-closure [target-type pname]
  (when-let [pmethod (lookup-list-property-method target-type pname)]
    (let [ltype (.getReturnType pmethod)
          ptype (get-property-type pmethod)]
      (with-meta
        (fn [target value]
          (list-property-closure-fn pmethod ptype target value))
        {:property-type ltype
         :type ::property-list-closure
         :element-type ptype}))))

(def ^:private lookup-property-closure
  (memoize
    (fn [target-type pname]
      (or
        (make-property-closure target-type pname)
        (make-list-property-closure target-type pname)
        (throw (ex-info (str "Can't find property " pname " for type " target-type)
                        {:target-type target-type
                         :property-name pname}))))))

(def lookup-default-property-closure
  (memoize
    (fn [cls]
      (when-let [default-prop-ann
                 (first (filter #(.equals (.annotationType %) DefaultProperty) (.getAnnotations cls)))]
        (let [prop-name (.value default-prop-ann)
              prop-closure (lookup-property-closure cls prop-name)
              prop-type (:property-type (meta prop-closure))]
          (if (.isAssignableFrom Collection prop-type)
            (with-meta
              (fn [target args] (prop-closure target args))
              {:property-type prop-type
               :type ::default-list-property-closure})
            (with-meta
              (fn [target args]
                (assert (= 1 (count args))
                        (str "Don't know how to bind sequence of args " args " to default property of type " prop-type
                             " on " target))
                (prop-closure target (first args)))
              {:property-type prop-type
               :type ::default-content-property-closure})))))))

(defn set-id+classes [node id class-list]
  (when id
    (.setId node id))
  (when class-list
    (doseq [cls class-list]
      (.add (.getStyleClass node) cls))))

;; Extracted from hiccup:
(def ^:private id-cls-kw-re
  #"(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn- apply-id-cls-kw [node id-cls-kw]
  (let [[_ id classes] (re-matches id-cls-kw-re (name id-cls-kw))
        class-list (when classes (str/split classes #"\."))]
    (set-id+classes node id class-list)))

(defn do-pset!* [node prop-map children default-prop-closure]
  (when prop-map
    (doseq [[p v] prop-map]
      ((lookup-property-closure (class node) p) node v)))
  (when children
    (assert default-prop-closure (str "No default property for: " node))
    (default-prop-closure node children))
  node)

(defn pset!**
  ([node args]
   (pset!** node args (lookup-default-property-closure (class node))))
  ([node args default-prop-closure]
   (let [prop-map? (first args)
         prop-map (when (map? prop-map?) prop-map?)
         children (if prop-map (next args) args)]
     (do-pset!* node prop-map children default-prop-closure))))

(defn pset!*
  ([node args default-prop-closure]
   (let [id-cls-kw? (first args)
         id-cls-kw (when (keyword? id-cls-kw?) id-cls-kw?)
         more (if id-cls-kw (next args) args)]
     (when id-cls-kw
       (apply-id-cls-kw node id-cls-kw))
     (pset!** node more default-prop-closure)) )
  ([node args]
   (pset!* node args (lookup-default-property-closure (class node)))))

(defn pset!
  "Sets properties on elements.

  id-class-kw? (optional): a keyword representing a hiccup style ID and
  classes (i.e. :#some-id.some-class.another-class).

  property-map? (optional): a map of property keys and setters. Keys can be
  kebab-case keywords corresponding to JavaFX bean properties. Values are
  converted using clojurefx.core.convert/convert-arg. If a value is an
  instance of ObservableValue (or is converted to one),
  it will be bound to the property.

  content-or-children* (zero or more): element or elements to be bound to the
  JavaFX element's DefaultProperty. If the DefaultProperty is a list property
  then multiple children elements can be bound, otherwise only a single
  'content' element or can be bound."
  {:arglists '([element id-class-kw? property-map? & content-or-children*])}
  [element & args]
  (run/run! (pset!* element args)))
