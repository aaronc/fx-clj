(ns fx-clj.core
  (:refer-clojure :exclude [run!])
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
    [fx-clj.sandbox]
    [fx-clj.core.transforms]
    [fx-clj.core.extensibility]
    [fx-clj.core.binding]
    [clojure.string :as str]))

(import-vars
  [fx-clj.core.run run! run<! run<!!]
  [fx-clj.core.pset pset!]
  [fx-clj.hiccup compile-fx build]
  [fx-clj.enlive at!]
  [fx-clj.sandbox sandbox]
  [fx-clj.css set-global-css!]
  [fx-clj.core.i18n with-locale with-resource-bundle
   get-resource-bundle get-locale get-resource]
  [fx-clj.util event-handler callback lookup]
  [fx-clj.core.binding property-ref observable-property bind<- bind<-> bind->])

(defn import-all [ns-sym]
  (eval
    `(potemkin/import-vars
       [~ns-sym
       ~@(keys (ns-publics (find-ns ns-sym)))])))

(import-all 'fx-clj.elements)
(import-all 'fx-clj.core.transforms)

(comment
  (defn available-transforms
    "Prints information on available transform functions for use
    primarily in the [[at!]] function."
    {:doc/format :markdown}
    []
    (doseq [xform @fx-clj.core.extensibility/defined-transforms]
      (let [{:keys [doc]} (meta xform)]
        (println xform)
        (println doc)
        (println))))

  (alter-meta!
    #'fx-clj.core/at!
    update-in [:doc]
    (fn [doc]
      (str doc
           (str/join
             "\n"
             (for [xform @fx-clj.core.extensibility/defined-transforms]
               (let [{:keys [ns name]} (meta xform)]
                 (str "  [[" name "]]"))))))))
