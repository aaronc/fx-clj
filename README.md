**[Guide](http://documentup.com/aaronc/fx-clj)** | **[API docs](http://aaronc.github.io/fx-clj/)** | **[Source](http://github.com/aaronc/fx-clj)** | **[License](https://raw.githubusercontent.com/aaronc/fx-clj/master/LICENSE)** | **[CHANGELOG](https://github.com/aaronc/fx-clj/releases)**

[![Clojars Project](http://clojars.org/fx-clj/latest-version.svg)](http://clojars.org/fx-clj)

Alpha quality: there may be lots of breaking changes. JDK 8 required.

## Overview

A Clojure library for JavaFX 8 with the following goals:

- Provide convenience functions for creating and modifying JavaFX
  objects without attempting to completely hide the JavaFX API
- Work with **core.async** out of the box
- Provide support for creating JavaFX objects with both a function
  based - `(fx/h-box (fx/button "Hello World"))` - and **hiccup-like** API -
  `(fx/compile-fx [:h-box [:button "Hello World"]])`.
- Provide an **enlive-like** API for modifying nodes (**for interacting with
  FXML resources**)
- Allow for setting JavaFX CSS from code and integrate with the **garden CSS**
  library
- Helper functions for **i18n**

## Quick Start

Make sure you have installed [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and have lein configured to use it. See the leiningen [sample.project.clj](https://github.com/technomancy/leiningen/blob/master/sample.project.clj) and search for `LEIN_JAVA_CMD`, `:java-cmd` and `JAVA_CMD` to see different ways to do this.

Add the leiningen dependency to your project listed above and a namespace declaration similar to the following to your code:

```clojure
(ns my-ns
  (:require [fx-clj.core :as fx]))
```

A "hello world" example:
```clojure
(ns example
  (:require [fx-clj.core :as fx]))

(defn create-view []
  (fx/h-box
    (fx/button {:on-action (fn [e] (println "Hello World!"))
                :text "Click Me!"})))

(fx/sandbox #'create-view) ;; Creates a "sandbox" JavaFX window to
                           ;; show the view. Clicking F5 in this
                           ;; window will refresh the view allowing the
                           ;; create-view function to be updated at the REPL

```

A quick example for integrating `fx-clj` and `core.async`:
```clojure
(ns example2
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

(fx/sandbox #'create-view)
```

## Usage

### Interacting with the JavaFX application thread

There are three macros for interacting with the JavaFX application
thread - each providing slightly different asynchronous behavior:
`run!`, `run<!` and `run<!!`. For those familiar with core.async, these
correspond to the behavior of `put!`, `<!` and `<!!`
respectively.

`run!` send a block of code to be run asynchronously on the JavaFX
application thread without blocking the caller. (It is effectively a
thin wrapper around javafx.application.Platform/runLater.)

```clojure
(run! (do-something)) ;; run asynchously without blocking
```

`run<!` *can only be used in a core.async* `go` *block!* It uses a
core.async channel and `<!` to return the value of the code executed
on the JavaFX application thread to the caller in `go` block. (This
blocks the `go` block, but does not block a thread.)

```clojure
(go
    (let [res (run<! (do-something))] ;; Go block paused
        (println res)))
```

`run<!!` uses a core.async channel and `<!!` to return the value of
the code executed on the JavaFX application thread. It blocks the
calling thread to return its value.

```clojure
(let [res (run<!! (do-something))] ;; Calling thread blocked
    (println res)))
```

### Modifying JavaFX objects

The pset! function is used to modify JavaFX objects.

The signature for `pset!` is the following:

```clojure
(defn pset! [id-class-keyword? property-map? content-or-children*])
```

`id-class-kw?` (optional): a keyword representing a hiccup style ID and
classes (i.e. `:#some-id.some-class.another-class`).

`property-map?` (optional): a map of property keys and setters. Keys can be
kebab-case keywords corresponding to JavaFX bean properties. Values are
converted using `clojurefx.core.convert/convert-arg`. If a value is an
instance of ObservableValue (or is converted to one),
it will be bound to the property.

`content-or-children*` (zero or more): element or elements to be bound to the
JavaFX element's DefaultProperty. If the DefaultProperty is a list property
then multiple children elements can be bound, otherwise only a single
'content' element can be bound.

### Creating JavaFX objects

There is both a function based and hiccup-style API for creating
JavaFX objects.

See the API documentation for `fx-clj.elements` for a list of
supported JavaFX objects.

The syntax for all object creation functions and the hiccup like
vectors, is almost identical to the pset syntax. It is basically a matter of 
which style you prefer. All of the following are equivalent:

```clojure
(fx/pset! (Button.) :#my-btn.my-class {:on-action (fn [] (println "Clicked"))} "Click Me")

(fx/button :#my-btn.my-class {:on-action (fn [] (println "Clicked"))} "Click Me")

(fx/compile-fx [:button#my-btn.my-class {:on-action (fn [] (println "Clicked"))}] "Click Me")
```


Because the DefaultProperty of Button is `text`, it can be set as the
argument after the property map.

## License

Copyright © 2014 Aaron Craelius

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
