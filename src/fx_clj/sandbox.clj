(ns fx-clj.sandbox
  (:import (javafx.scene.input KeyCode)
           (javafx.stage Modality))
  (:require
    [fx-clj.elements :as fx]
    [fx-clj.core.run :refer [run<!!]]
    [fx-clj.core.pset :refer [pset!]]))

(defn sandbox [refresh-fn]
  (run<!!
    (let [scene (fx/scene (refresh-fn))
          stage (fx/stage {:scene scene})]
      (pset! scene
             {:on-key-pressed
               (fn [e]
                 (when (= KeyCode/F5 (.getCode e))
                   (pset! scene {:root (refresh-fn)})))})
      (.initModality stage Modality/NONE)
      (.show stage)
      stage)))
