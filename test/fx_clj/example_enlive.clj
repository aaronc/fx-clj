(ns fx-clj.example-enlive
  (:require
    [fx-clj.core :as fx]
    [clojure.java.io :as io])
  (:import (javafx.fxml FXMLLoader)))

(defn test-view []
  (let [view (FXMLLoader/load
               (io/resource "fx_clj/example_enlive.fxml"))]
    (fx/at view
           "#my-btn"
           {:on-action
             (fn [e] (println "Got clicked!"))})
    view))

(fx/sandbox #'test-view)
