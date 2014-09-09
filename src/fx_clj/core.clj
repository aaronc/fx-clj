(ns fx-clj.core
  (:require
    [fx-clj.impl.bootstrap]
    [fx-clj.util :as util]
    [clojure.core.async :refer [chan put! go <! >! <!! >!!]]
    [camel-snake-kebab.core :refer [->SNAKE_CASE_STRING ->camelCaseString
                                    ->CamelCaseString]])
  (:import (javafx.event EventHandler)
           (javafx.util Callback)
           (javafx.collections ObservableList FXCollections)
           (javafx.scene Node)
           (javax.swing JComponent)
           (javafx.embed.swing SwingNode)
           (clojure.core.async.impl.channels ManyToManyChannel)))


;; (defn compile [form])

