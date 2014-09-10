(ns ^:no-doc fx-clj.core.i18n
  (:require
    [fx-clj.core.convert :refer [convert-arg]])
  (:import (javafx.fxml FXMLLoader)
           (java.util ResourceBundle Locale)))

(def ^:dynamic ^ResourceBundle *resource-bundle* nil)

(def ^:dynamic ^Locale *locale* nil)

(defn get-resource-bundle [bundle]
  (if (instance? ResourceBundle bundle)
    bundle
    (ResourceBundle/getBundle
      bundle (or *locale* (Locale/getDefault))) ))

(defn with-resource-bundle* [bundle f]
  (binding [*resource-bundle* (get-resource-bundle bundle)]
    (f)))

(defmacro with-resource-bundle
  "Evaluates body with the resource bundle specfiied by res-bundle bound to
  *resource-bundle*.  res-bundle can  either be an instance of
  java.util.ResourceBundle or a String referring to a named ResourceBundle.
  In the case the a String is provided the ResourceBundle is resolved using
  the locale bound to the *locale* var or the default locale."
  [res-bundle & body]
  `(fx-clj.core.i18n/with-resource-bundle*
     ~res-bundle
     (fn [] ~@body)))

(defn get-locale [locale]
  (if (instance? Locale locale)
    locale
    (Locale/forLanguageTag locale)))

(defn with-locale* [locale f]
  (binding [*locale* (get-locale locale)]
    (f)))

(defmacro with-locale
  "Evaluates body with the specified locale bound to the *locale* var. locale
   can either be an instance of java.util.Locale or a well-formed IEFT (BCP  47)
   language string."
  [locale & body]
  (fx-clj.core.i18n/with-locale*
    ~locale
    (fn [] ~@body)))

(defmethod convert-arg [String String] [_ s _]
  (if (and (.startsWith s FXMLLoader/RESOURCE_KEY_PREFIX)
           *resource-bundle*)
    (.getString *resource-bundle* (.substring s 1))
    s))
