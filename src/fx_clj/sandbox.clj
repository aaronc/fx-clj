(ns ^:no-doc fx-clj.sandbox
  (:import (javafx.scene.input KeyCode)
           (javafx.stage Modality))
  (:require
    [fx-clj.elements :as fx]
    [fx-clj.core.run :refer [run<!!]]
    [fx-clj.core.pset :refer [pset!]]))

(def ^:private auto-inc (atom 0))

(defn sandbox
  "Creates a JavaFX stage with the root element of the stage's scene set to
  the result of evaluating refresh-fn. If F5 is pressed within the stage,
  refresh-fn will be re-evaluated and its new result will be bound to as the
  root of the scene. This can be very useful for prototyping.

  Suggested usage:

  (defn my-refresh-fn [] (do-create-view....))
  (sandbox #'my-refresh-fn)
  ;; By binding to a var,  my-refresh-fn can be  easily updated and reloaded
  ;; at the REPL"

  [refresh-fn & {:keys [title maximized accelerators]
                 :or {title (str "Sandbox" (swap! auto-inc inc))}}]
  (run<!!
    (let [scene (fx/scene (refresh-fn))
          stage (fx/stage)]
      (pset! scene
             {:on-key-pressed
               (fn do-sandbox-refresh [e]
                 (when (= KeyCode/F5 (.getCode e))
                   (pset! scene {:root (refresh-fn)})))})
      (doseq [[kc r] accelerators]
        (.put (.getAccelerators scene) kc (fn [] (r scene))))
      (.setScene stage scene)
      (.initModality stage Modality/NONE)
      (pset! stage {:title title})
      (when maximized (.setMaximized stage true))
      (.show stage)
      stage)))
