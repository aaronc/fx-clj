(ns ^:no-doc fx-clj.sandbox
  (:import (javafx.scene.input KeyCode)
           (javafx.stage Modality))
  (:require
    [fx-clj.elements :as fx]
    [fx-clj.core.run :refer [run<!!]]
    [fx-clj.core.pset :refer [pset!]]))

(defn sandbox [refresh-fn]
  (run<!!
    (let [scene (fx/scene (refresh-fn))
          stage (fx/stage)]
      (pset! scene
             {:on-key-pressed
               (fn do-sandbox-refresh [e]
                 (when (= KeyCode/F5 (.getCode e))
                   (pset! scene {:root (refresh-fn)})))})
      (.setScene stage scene)
      (.initModality stage Modality/NONE)
      (.show stage)
      stage)))
