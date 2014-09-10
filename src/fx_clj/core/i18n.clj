(ns fx-clj.core.i18n
  (:require
    [fx-clj.core.convert :refer [convert-arg]])
  (:import (javafx.fxml FXMLLoader)
           (java.util ResourceBundle Locale)))

(def ^:dynamic ^ResourceBundle *resource-bundle* nil)

(def ^:dynamic ^Locale *locale* nil)

(defn get-bundle [bundle]
  (if (instance? ResourceBundle bundle)
    bundle
    (ResourceBundle/getBundle
      bundle (or *locale* (Locale/getDefault))) ))

(defn with-resources* [bundle f]
  (binding [*resource-bundle* (get-bundle bundle)]
    (f)))

(defmacro with-resources [res-bundle & body]
  `(fx-clj.core.i18n/with-resources*
     ~res-bundle
     (fn [] ~@body)))

(defn get-locale [locale]
  (if (instance? Locale locale)
    locale
    (Locale/forLanguageTag locale)))

(defn with-locale* [locale f]
  (binding [*locale* (get-locale locale)]
    (f)))

(defmacro with-locale [locale & body]
  (fx-clj.core.i18n/with-locale*
    ~locale
    (fn [] ~@body)))

(defmethod convert-arg [String String] [_ s _]
  (if (and (.startsWith s FXMLLoader/RESOURCE_KEY_PREFIX)
           *resource-bundle*)
    (.getString *resource-bundle* (.substring s 1))
    s))
