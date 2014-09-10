(ns fx-clj.example1
  (:require [fx-clj.core :as fx]))

(defn create-view []
  (fx/h-box
    (fx/button {:on-action (fn [e] (println "Hello World!"))
                :text "Click Me!"})))

(fx/sandbox #'create-view) ;; Creates a "sandbox" JavaFX window to
    ;; show the view. Clicking F5 in this
    ;; window will refresh the view allowing the
    ;; create-view function to be updated at the REPL

