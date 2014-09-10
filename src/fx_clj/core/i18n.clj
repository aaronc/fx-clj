(ns ^:no-doc fx-clj.core.i18n
  (:require
    [fx-clj.core.extensibility :refer [convert-arg]])
  (:import (javafx.fxml FXMLLoader)
           (java.util ResourceBundle Locale)))

(clojure.lang.Var/intern 'fx-clj.core
                         (with-meta '*resource-bundle*
                                    {:tag ResourceBundle
                                     :dynamic true}))

(clojure.lang.Var/intern 'fx-clj.core
                         (with-meta '*locale*
                                    {:tag Locale
                                     :dynamic true}))

(defn get-resource-bundle
  ([bundle]
   (get-resource-bundle
     bundle (or fx-clj.core/*locale* (Locale/getDefault))))
  ([bundle locale]
   (if (instance? ResourceBundle bundle)
     bundle (ResourceBundle/getBundle (name bundle) locale))))

(defn with-resource-bundle* [bundle f]
  (binding [fx-clj.core/*resource-bundle* (get-resource-bundle bundle)]
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
    (Locale/forLanguageTag (name locale))))

(defn with-locale* [locale f]
  (binding [fx-clj.core/*locale* (get-locale locale)]
    (f)))

(defmacro with-locale
  "Evaluates body with the specified locale bound to the *locale* var. locale
   can either be an instance of java.util.Locale or a well-formed IEFT (BCP  47)
   language string."
  [locale & body]
  (fx-clj.core.i18n/with-locale*
    ~locale
    (fn [] ~@body)))

(defn get-resource [res-name]
  (.getObject fx-clj.core/*resource-bundle* (name res-name)))

(defmethod convert-arg [String String] [_ s _]
  (if (and (.startsWith s FXMLLoader/RESOURCE_KEY_PREFIX)
           fx-clj.core/*resource-bundle*)
    (.getString fx-clj.core/*resource-bundle* (.substring s 1))
    s))
