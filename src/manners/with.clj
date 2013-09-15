(ns manners.with
  (:require [manners.modern :as modern]
            [manners.victorian :as victorian]))

;; A dynamic var to put the etiquette bound by with-etiquette in.
(declare ^:private ^:dynamic *etiquette*)

;; Define all of the api functions for use inside of with-etiquette. This is a
;; bit ugly with the use of eval but it works.
(doseq [[dialect syms]
        {'victorian ['bad-manners 'rude? 'proper? 'avow! 'coach]
         'modern ['errors 'invalid? 'valid? 'validate!  'validator]}]
  (doseq [sym syms]
    (let [func-sym (symbol (str dialect \/ sym))
          doc-string (str "Like a partial of manners."
                          func-sym
                          " to be used in with-etiquette so the etiquette"
                          " parameter is already defined.")]
      (eval `(defn ~sym
               ~doc-string
               [value#]
               (~func-sym *etiquette* value#))))))

(defmacro with-etiquette
  "Start a scope with the given etiquettes as the base."
  [etiquette & body]
  `(binding [*etiquette* ~etiquette]
     ~@body))
