(ns fx-clj.i18n
  (:require [fx-clj.core :as fx]))

(defn make-hello []
  (fx/with-resource-bundle
    "TestResources"
    (fx/text "%hello")))

(defn make-view []
  (fx/v-box
    (fx/text "Default:")
    (make-hello)
    (fx/text "In Spanish:")
    (fx/with-locale
      "es"
      (make-hello))))

(fx/sandbox #'make-view)
