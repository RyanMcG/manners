(ns manners.with
  "A namespace for defining the with-etiquette macro for scoped, etiquette
  specific, manners."
  (:require manners.victorian))

(defn- symbol->dynamic-partial [dynamic-var sym]
  (let [func-sym (symbol (str 'manners.victorian \/ sym))
        doc-string (str "Like a partial of manners."
                        func-sym
                        " to be used in with-etiquette so the etiquette"
                        " parameter is already defined.")]
    `(defn ~sym
       ~doc-string
       [value#] (~func-sym ~dynamic-var value#))))

(defmacro ^:private def-dynamic-partials
  "Define partial versions of the given symbol"
  [dynamic-var & symbols]
  (let [defns (map (partial symbol->dynamic-partial dynamic-var) symbols)]
    `(do ~@defns)))

;; A dynamic var to put the etiquette bound by with-etiquette in.
(declare ^:private ^:dynamic *etiquette*)

(def-dynamic-partials *etiquette* bad-manners rude? proper? avow)

(defmacro with-etiquette
  "Start a scope with the given etiquettes as the base."
  [etiquette & body]
  `(binding [*etiquette* ~etiquette]
     ~@body))
