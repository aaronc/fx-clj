(ns fx-clj.example2
  (:require [fx-clj.core :as fx])
  (:require [clojure.core.async :refer [chan go <! >!]]))

(defn create-view []
  (let [click-ch (chan)
        btn (fx/button :#my-btn {:on-action click-ch ;; You can bind a core.async channel directly to an event
                        :text "Next"})

        txt (fx/text "Initial text")
        view (fx/v-box txt btn)]
    (go
      (<! click-ch)
      (fx/run<! (fx/pset! txt "Next text"))
      (<! click-ch)
      (fx/run<!
        (fx/pset! txt "Last text")
        (fx/pset! btn {:text "Done"}))
      (println "Done listening to clicks"))

    view))

(def s0 (fx/sandbox #'create-view))
