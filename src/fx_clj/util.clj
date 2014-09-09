(ns fx-clj.util
  (:require [fx-clj.impl.bootstrap]))

;; From https://gist.github.com/zilti/6286307:
(defn event-handler*
  [f]
  (reify javafx.event.EventHandler
    (handle [this e] (f e))))

;; From https://gist.github.com/zilti/6286307:
(defmacro event-handler [arg & body]
  `(event-handler* (fn ~arg ~@body)))

(defn callback*
  [f]
  (reify javafx.util.Callback
    (call [this e] (f e))))

(defmacro callback [arg & body]
  `(callback* (fn ~arg ~@body)))

;; TODO: use java.thread uncaught ex handler
(defn set-fx-exception-handler! [f])

