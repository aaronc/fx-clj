(ns fx-clj.core
  (:require
    [potemkin :refer [import-vars]]
    [fx-clj.core.run]
    [fx-clj.core.pset]
    [fx-clj.hiccup]
    [fx-clj.enlive]
    [fx-clj.elements]
    [fx-clj.css]
    [fx-clj.core.i18n]
    [fx-clj.util]
    [fx-clj.sandbox]))

(import-vars
  [fx-clj.core.run run! run<! run<!!]
  [fx-clj.core.pset pset!]
  [fx-clj.hiccup compile-fx]
  [fx-clj.enlive at]
  [fx-clj.sandbox sandbox]
  [fx-clj.css set-global-css!]
  [fx-clj.core.i18n with-locale with-resource-bundle
   *locale* *resource-bundle*]
  [fx-clj.util event-handler callback])

(eval
  `(potemkin/import-vars [fx-clj.elements
      ~@(keys (ns-publics (find-ns 'fx-clj.elements)))]))

