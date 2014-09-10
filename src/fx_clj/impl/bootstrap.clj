(ns ^:no-doc fx-clj.impl.bootstrap)

;; From clojurefx.core:
(defonce force-toolkit-init (javafx.embed.swing.JFXPanel.))

(javafx.application.Platform/setImplicitExit false)
