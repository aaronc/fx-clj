(ns fx-clj.css
  (:require
    [fx-clj.core.run :refer [run!]])
  (:import (javafx.application Application)
           (com.sun.javafx.css StyleManager)
           (java.net URL URLStreamHandlerFactory URLStreamHandler URLConnection)
           (java.io ByteArrayInputStream FileNotFoundException)))

(defonce ^:private auto-inc (atom 0))

(def ^:private prefix "fx-clj-global")

(defonce ^:private css-strings
         (atom {}))

(def ^:private url-stream-handler
  (proxy [URLStreamHandler] []
    (openConnection [url]
      (if-let [css (get @css-strings url)]
        (proxy [URLConnection] [url]
          (connect [])
          (getInputStream []
            (println "getInputStream")
            (ByteArrayInputStream. (.getBytes css))))
        (throw (FileNotFoundException.))))))

(def ^:private url-stream-handler-factory
  (reify
    URLStreamHandlerFactory
    (createURLStreamHandler [this protocol]
      (when (= protocol prefix)
        (println "createURLSTreamHandler")
        url-stream-handler))))

(defonce ^:private init (URL/setURLStreamHandlerFactory url-stream-handler-factory))

(defn remove-global-stylesheet! [url]
  (run!
    (.removeUserAgentStylesheet (StyleManager/getInstance) (str url))))

(defn set-global-stylesheet! [url]
  (run!
    (Application/setUserAgentStylesheet url)
    ;;(.addUserAgentStylesheet (StyleManager/getInstance) (str url))
    ))

(def ^:private global-url-re (re-pattern (str prefix ":global-\\d*")))

(defn set-global-css! [css]
  (let [url (URL. (str prefix ":global-" (swap! auto-inc inc)))
        to-remove (filter #(re-matches global-url-re (str %)) (keys @css-strings))]
    (swap! css-strings (fn [x] (apply dissoc x to-remove)))
    ;;(doseq [x to-remove] (remove-global-stylesheet! x))
    (swap! css-strings assoc url css)
    (set-global-stylesheet! url)))
