(ns manners.utils)

(defmacro defalias
  "A simple macro to define an alias of another var with a doc string
  referencing the original var."
  [name-sym value]
  `(let [doc-str# (str "This is an alias of " (var ~value)\.)]
     (def ~name-sym ~value)
     (alter-meta! (var ~name-sym) assoc :doc doc-str#)))
