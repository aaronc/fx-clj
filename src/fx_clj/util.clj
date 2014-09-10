(ns ^:no-doc fx-clj.util
  (:require
    [fx-clj.impl.bootstrap]
    [fx-clj.impl.util :refer [javadoc-link]]))

;; From https://gist.github.com/zilti/6286307:
(defn event-handler*
  [f]
  (reify javafx.event.EventHandler
    (handle [this e] (f e))))

;; From https://gist.github.com/zilti/6286307:
(defmacro event-handler
  {:doc (str "Wraps a " (javadoc-link javafx.event.EventHandler) ": (event-handler \\[e\\] (do-something))")
   :doc/format :markdown}
  [arg & body]
  `(event-handler* (fn ~arg ~@body)))

(defn callback*
  [f]
  (reify javafx.util.Callback
    (call [this e] (f e))))

(defmacro callback
  {:doc (str "Wraps a " (javadoc-link javafx.util.Callback) ": (callback  \\[p\\] (do-something))")
   :doc/format :markdown}
  [arg & body]
  `(callback* (fn ~arg ~@body)))
