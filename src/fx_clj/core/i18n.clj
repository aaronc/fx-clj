(ns ^:no-doc fx-clj.core.i18n
  (:require
    [fx-clj.core.extensibility :refer [convert-arg]])
  (:import (javafx.fxml FXMLLoader)
           (java.util ResourceBundle Locale)))

(def ^:private core-ns
  (clojure.lang.Namespace/findOrCreate 'fx-clj.core))

(def ^:private locale-var
  (clojure.lang.Var/intern core-ns '*locale* nil))
(.setDynamic locale-var)
(alter-meta!
  locale-var
  merge
  {:tag        Locale
   :doc/format :markdown
   :doc        "A java.util.Locale. See [[with-locale]]"})

(def ^:private res-bundle-var
  (clojure.lang.Var/intern core-ns '*resource-bundle* nil))
(.setDynamic res-bundle-var)
(alter-meta!
  res-bundle-var
  merge
  {:tag        ResourceBundle
   :doc/format :markdown
   :doc        "A java.util.ResourceBundle. See [[with-resource-bundle]]."})

(defn get-locale
  "Coerces the provided arguments to a java.util.Locale. Can be a
  java.util.Locale instance or a string or keyword representing a well-formed
  IETF (BCP  47) language tag. Use of [[with-locale]] (which calls this is
  recommended."
  {:doc/format :markdown}
  [locale]
  (if (instance? Locale locale)
    locale
    (Locale/forLanguageTag (name locale))))

(defn with-locale* [locale f]
  (binding [fx-clj.core/*locale* (get-locale locale)]
    (f)))

(defmacro with-locale
  "Evaluates body with the specified locale bound to [[*locale*]] . Uses
  [[get-locale]] to convert the locale argument to a java.util.Locale. For
  use in coordination with [[with-resource-bundle]]."
  {:doc/format :markdown}
  [locale & body]
  `(fx-clj.core.i18n/with-locale*
     ~locale
     (fn [] ~@body)))

(defn get-resource-bundle
  "Coerces the specified arguments to a java.util.ResourceBundle. Can be a
  java.util.ResourceBundle instance or a string or keyword representing a
  resource bundle name. locale, if provided, is coerced using the [[get-locale]]
  function. Use of [[with-resource-bundle]] (which calls this) is recommended."
  {:doc/format :markdown}
  ([bundle]
   (get-resource-bundle
     bundle (or fx-clj.core/*locale* (Locale/getDefault))))
  ([bundle locale]
   (if (instance? ResourceBundle bundle)
     bundle
     (let [^String bname (name bundle)
           ^Locale locale (get-locale locale)]
       (ResourceBundle/getBundle bname locale)))))

(defn with-resource-bundle* [bundle f]
  (binding [fx-clj.core/*resource-bundle* (get-resource-bundle bundle)]
    (f)))

(defmacro with-resource-bundle
  "Evaluates body with the resource bundle specfiied by res-bundle bound to
  [[*resource-bundle*]].  Uses [[get-resource-bundle]] to convert the first
  argument to a java.util.ResourceBundle. Use [[with-locale]] to bind a
  locale in this context. Once a resource bundle is bound,
  strings passed as arguments to [[pset!]] property maps that begin with the `%`
  character will be looked up as keys in the resource bundle (this mirrors
  the behavior the FXML uses for internationalization). [[get-resource]] can
  also be used to get a resource from the bundle.

  Example usage:

  ```clojure
  (with-locale \"es\"
    (with-resource-bundle \"my-resources\"
      (println (get-resource \"some-text\"))
      (pset! my-node {:text \"%some-text\"})))
  ```
  "
  {:doc/format :markdown}
  [res-bundle & body]
  `(fx-clj.core.i18n/with-resource-bundle*
     ~res-bundle
     (fn [] ~@body)))

(defn get-resource
  "Takes a string or keyword used as a key to get a resource from the
  java.util.Resource bound to [[*resource-bundle*]]."
  {:doc/format :markdown}
  [res-name]
  (.getObject fx-clj.core/*resource-bundle* (name res-name)))

(defmethod convert-arg [String String] [_ s _]
  (if (and (.startsWith s FXMLLoader/RESOURCE_KEY_PREFIX)
           fx-clj.core/*resource-bundle*)
    (.getString fx-clj.core/*resource-bundle* (.substring s 1))
    s))
