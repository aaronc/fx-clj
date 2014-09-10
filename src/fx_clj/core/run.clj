(ns ^:no-doc fx-clj.core.run
  (:require
    [fx-clj.impl.bootstrap]
    [clojure.core.async :refer [go put! chan <!]])
  (:import (javafx.application Platform)))

(defn run!* [f]
  (if (Platform/isFxApplicationThread)
    (f)
    (Platform/runLater f)))

(defmacro run! [& body]
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

(defmacro run<! [& body]
  (run<* 'clojure.core.async/<! body))

(defmacro run<!! [& body]
  (run<* 'clojure.core.async/<!! body))