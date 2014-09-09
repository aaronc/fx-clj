(ns fx-clj.core
  (:require [clojure.core.async :refer [put! go <! >!]]))

;; TODO: use java.thread uncaught ex handler
(defn set-fx-exception-handler! [f])

(defn run!* [f])

(defn- async-run-wrapper [f ch]
  (let [res
        (try
          (f)
          (catch Throwable ex
            (put! ch (ex-info (str "Error on JavaFX application thread") {} ex))
            (throw ex) ;; Should this be rethrown??
            ))]
    (put! ch res)))

(defn- run<** [f cb]
  (let [ch (chan)]
    (run!* f ch)
    (let [res (cb ch)]
      (when (instace? Throwable res)
        (throw res)))))

(defn run<!* [f] (run<** f <!))

(defn run<!!* [f] (run<** f <!!))

(defmacro run! [& body])

(defmacro run<! [& body])

(defmacro run<!! [& body])

(defn pset! [])

(defn pset<! [])

(defn pset<!! [])

(defn compile [form])
