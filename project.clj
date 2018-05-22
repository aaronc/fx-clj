(defproject fx-clj "0.2.1-SNAPSHOT"
  :description "A Clojure library for JavaFX"
  :url "https://github.com/aaronc/fx-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [garden "1.3.5"]
                 [potemkin "0.4.5"]
                 [org.tobereplaced/lettercase "1.0.0"]
                 [freactive.core "0.2.1-SNAPSHOT"]]
  :java-source-paths ["src"]
  :javac-options ["-Xlint:unchecked"]
  :profiles
  {:dev
    {:plugins
      [[codox "0.8.10"]]}})
