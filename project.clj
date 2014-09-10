(defproject fx-clj "0.2.0-SNAPSHOT"
  :description "A Clojure library for JavaFX"
  :url "https://github.com/aaronc/fx-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [camel-snake-kebab "0.2.4"]
                 [garden "1.2.1"]
                 [potemkin "0.3.8"]]
  :profiles
  {:dev
    {:plugins
      [[codox "0.8.10"]]}})
