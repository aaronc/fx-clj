(ns fx-clj.core
  (:require
    [potemkin :refer [import-vars]]
    [fx-clj.core.run]
    [fx-clj.core.pset]
    [fx-clj.hiccup]
    [fx-clj.elements]
    [fx-clj.sandbox]))

(import-vars
  [fx-clj.core.run run! run<! run<!!]
  [fx-clj.core.pset pset! pset<! pset<!!]
  [fx-clj.hiccup compile-fx]
  [fx-clj.sandbox sandbox])

(eval
  `(potemkin/import-vars [fx-clj.elements
      ~@(keys (ns-publics (find-ns 'fx-clj.elements)))]))

