(ns fx-clj.core
  (:require
    [fx-clj.impl.bootstrap]
    [fx-clj.util :as util]
    [clojure.core.async :refer [chan put! go <! >! <!! >!!]]
    [camel-snake-kebab.core :refer [->SNAKE_CASE_STRING ->camelCaseString
                                    ->CamelCaseString]])
  (:import (javafx.event EventHandler)
           (javafx.util Callback)
           (javafx.collections ObservableList FXCollections)
           (javafx.scene Node)
           (javax.swing JComponent)
           (javafx.embed.swing SwingNode)
           (clojure.core.async.impl.channels ManyToManyChannel)))

;; TODO: use java.thread uncaught ex handler
(defn set-fx-exception-handler! [f])

(defn run!* [f])

(defn- async-run-wrapper [f ch]
  (let [res
        (try
          (f)
          (catch Throwable ex
            (put! ch (ex-info (str "Error on JavaFX application thread") {} ex))
            (throw ex) ;; Should this be rethrown??
            ))]
    (put! ch res)))

(defn- run<** [f cb]
  (let [ch (chan)]
    (run!* f ch)
    (let [res (cb ch)]
      (when (instance? Throwable res)
        (throw res)))))

(defn run<!* [f] (run<** f <!))

(defn run<!!* [f] (run<** f <!!))

(defmacro run! [& body])

(defmacro run<! [& body])

(defmacro run<!! [& body])

(defmulti convert-arg (fn [requested-type value opts] [requested-type (type
                                                                    value)]))

(defmethod convert-arg :default [_ v] v)

(defmethod convert-arg [EventHandler clojure.lang.IFn] [_ f _]
  (util/event-handler* f))

(defmethod convert-arg [EventHandler ManyToManyChannel] [_ ch _]
  (util/event-handler [e] (put! ch e)))

(defmethod convert-arg [Callback clojure.lang.IFn] [_ f _]
  (util/callback* f))

(defmethod convert-arg [Enum clojure.lang.Keyword] [enum kw _]
  (Enum/valueOf enum (->SNAKE_CASE_STRING kw)))

(defmethod convert-arg [ObservableList clojure.lang.Sequential]
           [_ v {:keys [element-type]}]
  (FXCollections/observableList (into [] v)))

(defmethod convert-arg [Node JComponent] [nt jc _]
  (doto (SwingNode.)
    (.setContent jc)))

(defn- get-generic-interfaces [cls]
  (loop [ifaces #{}
         cls cls]
    (if cls
      (recur (apply conj ifaces (.getGenericInterfaces cls))
             (.getSuperclass cls))
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
                    (str (->camelCaseString pname) "Property")
                    nil)
        (catch Exception ex
          nil)))))

(defn- property-closure-fn [pmethod ptype target value]
  (let [prop (.invoke pmethod target nil)
        value (convert-arg ptype value)]
    (cond
      (instance? ObservableValue value) (.bind prop value)
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
                    (str "get" (->CamelCaseString pname))
                    nil)
        (catch Exception ex
          nil)))))

(defn- list-property-closure-fn [pmethod ptype target value]
  (cond
    (sequential? value)
    (let [list-value (.invoke pmethod target nil)]
      (doseq [v value]
        (let [v (convert-arg ptype v)]
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

(def ^:private lookup-default-property-closure
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

(defn- do-bind! [node property-map & children]
  (when-not (nil? property-map)
    (assert (map? property-map)
            (str "Cannot bind! to " property-map ", expected a property map"))
    (doseq [[p v] property-map]
      ((lookup-property-closure (class node) p) node v))
    (when children
      ((lookup-default-property-closure (class node)) node children)))
  node)

(defn bind! [node property-map]
  (if (Platform/isFxApplicationThread)
    (do-bind! node property-map)
    (run-now (do-bind! node property-map))))

(defonce ^:private registered-classes (atom {}))

(defn register-class [cls]
  (swap! registered-classes assoc (.getSimpleName cls) cls))

(defn- lookup-class [cls-name] (get @registered-classes cls-name))

(defn- find-default-ctr [cls]
  (let [ctrs (.getConstructors cls)
        min-params (apply min (map #(.getParameterCount %) ctrs))
        default-ctrs (filter #(= min-params (.getParameterCount %)) ctrs)]
    (if (= (count default-ctrs) 1)
      (first default-ctrs)
      (throw (ex-info (str "Can't infer default constructor for class " cls
                           " from amongst: " default-ctrs))))) )

(def find-factory-fn
  (memoize
    (fn [cls]
      (if (string? cls)
        (find-factory-fn (lookup-class (->CamelCaseString cls)))

        (let [ctr (find-default-ctr cls)
              param-count (.getParameterCount ctr)
              param-types (.getParameterTypes ctr)]
          (fn [args]
            (let [[ctr-args more] (split-at param-count args)]
              [(.newInstance ctr
                             (when ctr-args (into-array
                                              Object
                                              (for [i (range param-count)]
                                                (convert-arg (nth param-types i)
                                                             (nth ctr-args i))))))
               more])))))))

(defn- set-id+classes [node id class-list]
  (when id
    (.setId node id))
  (when class-list
    (doseq [cls class-list]
      (.add (.getStyleClass node) cls))))

(def ^{:doc "Regular expression that parses a CSS-style id and class from an element name.
             From hiccup.compiler: https://github.com/weavejester/hiccup/blob/master/src/hiccup/compiler.clj
             EPL license."
       :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(defn pset! [])

(defn pset<! [])

(defn pset<!! [])

;; (defn compile [form])

