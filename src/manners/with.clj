(ns manners.with
  (:require [manners.victorian :as victorian]))

;; A dynamic var to put the etiquette bound by with-etiquette in.
(declare ^:private ^:dynamic *etiquette*)

;; Define all of the api functions for use inside of with-etiquette. This is a
;; bit ugly with the use of eval but it works.

(doseq [sym ['bad-manners 'rude? 'proper? 'avow! 'coach]]
  (let [func-sym (symbol (str 'victorian \/ sym))
        doc-string (str "Like a partial of manners."
                        func-sym
                        " to be used in with-etiquette so the etiquette"
                        " parameter is already defined.")]
    (eval `(defn ~sym
             ~doc-string
             [value#]
             (~func-sym *etiquette* value#)))))

(defmacro with-etiquette
  "Start a scope with the given etiquettes as the base."
  [etiquette & body]
  `(binding [*etiquette* ~etiquette]
     ~@body))
