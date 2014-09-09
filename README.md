# fx-clj

WORK IN PROGRESS. Not ready yet!

A Clojure library for JavaFX with the following goals:

- Provide convenience functions for creating and modifying JavaFX
  objects without attempting to completely hide the JavaFX API
- Work with **core.async** out of the box
- Provide support for creating JavaFX functions with both a function
  based - `(fx/h-box (fx/button "Hello World"))` - and hiccup-like API -
  `(fx/compile-fx [:h-box [:button "Hello World"]])`.
- Allow for setting JavaFX CSS from code and integrate with the garden CSS 
  library

## Quick Start

Leiningen dependeny information:

```clojure
[fx-clj "0.1.0-SNAPSHOT"]
```


Two minute example:
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

Two minute core.async example:
```clojure
(ns example2
  (:require [fx-clj.core :as fx])
  (:require [clojure.core.async :refer [chan go <! >!]]))

(defn create-view []
  (let [click-ch (chan)
        btn (fx/button {:on-action click-ch ;; You can bind a core.async channel directly to an event
                        :text "Click Me!"})]
    (go
      (<! click-ch)
      (println "Clicked the first time")
      (<! click-ch)
      (println "Clicked again")
      (fx/pset<! btn {:text "Done"})
      (println "Done listening to clicks"))

      (fx/h-box btn)))

(fx/sandbox #'create-view)
```

## Usage

To get all of fx-clj into your namespace quickly use a namespace
declaration like this:
```clojure
(ns my-ns
  (:require [fx-clj.core :as fx]))
```

To use fx-clj and core.async together, use something like this:
```clojure
(ns my-ns
  (:require [fx-clj.core :as fx])
  (:require [clojure.core.async :as async :refer [go go-loop chan <! >!])))
```

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

The pset macros are used to modify JavaFX objects. Like the run
macros, there are 3 variants: `pset!`, `pset<!`, and `pset<!!` which
have the same async behavior. Note: In the case of the pset macros,
this behavior is more for coordination between
processes rather than for getting any actual return value (these
macros return the value they were passed except that when `pset!` is called
from a thread other than the JavaFX application thread it runs
asynchronously without blocking the caller and returns `nil`).

TODO: maybe pset should always return `nil`??

The signature for `pset!` is the following:

```clojure
(defn pset! id-class-keyword? property-map? children-content*)
```

`id-class-keyword?`: an *optional* keyword parameter taking the form
of :#my-id.my-class.my-class1 that will set the node's id and
styleClass properties to the specified values.

`property-map?`: an *optional* map of properties to set on the object.

`children-content*`: 0 or more items to be set on the objects
DefaultProperty. If the default property is a List property (like
children or items) there can be any number of elements. If it is a
property taking a singular value (like content), there can be only one element.

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

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
