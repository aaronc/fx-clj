(defproject fx-clj "0.2.0-SNAPSHOT"
  :description "A Clojure library for JavaFX"
  :url "https://github.com/aaronc/fx-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha3"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [garden "1.2.1"]
                 [potemkin "0.3.8"]
                 [org.tobereplaced/lettercase "1.0.0"]
                 [freactive "0.1.0-SNAPSHOT"]]
  :java-source-paths ["src"]
  :javac-options ["-Xlint:unchecked"]
  :profiles
  {:dev
    {:plugins
      [[codox "0.8.10"]]}})
