(ns ^:no-doc fx-clj.impl.util)

(defn javadoc-link [cls]
  (str "[" (.getName cls) "]"
       "(http://docs.oracle.com/javase/8/javafx/api/"
       (.replace (.getName cls) \. \/)
       ".html)"))

