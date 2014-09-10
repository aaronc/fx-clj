(ns ^:no-doc fx-clj.core.run
  (:require
    [fx-clj.impl.bootstrap]
    [clojure.core.async :refer [go put! chan <!]])
  (:refer-clojure :exclude [run!])
  (:import (javafx.application Platform)))

(defn run!* [f]
  (if (Platform/isFxApplicationThread)
    (f)
    (Platform/runLater f)))

(defmacro run!
  "Runs the enclosed body asynchronously on the JavaFX application thread
  (if caller is not already on this thread). Does not block the calling thread."
  [& body]
  `(fx-clj.core.run/run!* (fn [] ~@body)))

(defn async-run-wrapper [f ch]
  (fn []
    (let [res
          (try
            (f)
            (catch Throwable ex
              (put! ch (ex-info (str "Error on JavaFX application thread") {:cause ex} ex))
              (throw ex)                                    ;; Should this be rethrown??
              ))]
      (put! ch (if (nil? res) ::nil res)))))

(defn process-async-res [res]
  (cond
    (instance? Throwable res)
    (throw res)

    (= ::nil res)
    nil

    :default
    res))

(defn- run<* [take-fn body]
 `(if (javafx.application.Platform/isFxApplicationThread)
    (do ~@body)
    (let [ch# (clojure.core.async/chan)]
      (javafx.application.Platform/runLater
        (fx-clj.core.run/async-run-wrapper (fn [] ~@body) ch#))
      (fx-clj.core.run/process-async-res (~take-fn ch#)))))

(defmacro run<!
  "Runs the enclosed body asynchronously on the JavaFX application thread
  from within a core.async go block. Returns the value of the evaluated body
  using a core.async chan and the <! function. Must be called from within a
  core.async go block!"
  [& body]
  (run<* 'clojure.core.async/<! body))

(defmacro run<!!
  "Runs the enclosed body asynchronously on the JavaFX application thread.
  Blocks the calling thread until asynchronous execution is complete and
  returns the result of the evaluated block to the caller."
  [& body]
  (run<* 'clojure.core.async/<!! body))