(ns fx-clj.example2
  (:require [fx-clj.core :as fx])
  (:require [clojure.core.async :refer [chan go <! >!]]))

(defn create-view []
  (let [click-ch (chan)
        btn (fx/button :#my-btn {:on-action click-ch ;; You can bind a core.async channel directly to an event
                        :text "Click Me!"})]
    (go
      (<! click-ch)
      (println "Clicked the first time")
      (<! click-ch)
      (println "Clicked again")
      (fx/pset<! btn {:text "Done"})
      (println "Done listening to clicks"))

    (fx/h-box btn)))

(def s0 (fx/sandbox #'create-view))
