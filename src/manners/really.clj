(ns manners.really
  "A namespace containing macros for defining manners where the function to be
  used as part of the predicate is part of the message."
  (:require [manners.victorian :refer [manner]]
            [clojure.string :as s]))

(defn- really-helper [create-msg params]
  `(let [args# [~@params]
         [prefixes# [f# & more#]] (split-with (complement fn?) args#)
         [f-expr# & params#] (drop (count prefixes#) '~params)]
    (manner (fn [v#] (apply f# v# more#))
            (~create-msg prefixes# f-expr# more# params#))))

(defn- remove-trailing-? [a-str]
  (if (= \? (last a-str))
    (subs a-str 0 (dec (count a-str)))
    a-str))

(defn- pred-expr-to-words [f-expr]
  (-> f-expr
      str
      remove-trailing-?
      (s/replace #"-" " ")
      (s/replace #"[\(\)\[\]]" "")
      s/trim))

(defn- create-msg [prefixes f-expr more]
  (str (s/join " " prefixes) \space
       (pred-expr-to-words f-expr)
       (if (seq more)
         (str \space (s/join " " more)))))

(defn- really-create-msg [prefixes f-expr more _]
  (create-msg prefixes f-expr more))

(defn- verily-create-msg [prefixes f-expr _ params]
  (create-msg prefixes f-expr params))

(defmacro really
  "Create a coach from a single predicate and part of a message

    ((really \"must be a\" string?) 828)          ; => (\"must be a string\")
    ((really \"must be a\" string?) \"anything\") ; => ()

  Trailing arguments (i.e. those after the first function) are also included in
  the message.

    ((really \"must be\" < 10) 11) ; => (\"must be < 10\")"
  [& params]
  (really-helper really-create-msg params))

(defmacro verily
  "Just like #'manenrs.really/really except extra params are used in the message
  without being unquoted.

    (def ten 10)
    ((really \"must be\" < ten) 11) ; => (\"must be < 10\")
    ((verily \"must be\" < ten) 11) ; => (\"must be < ten\")"
  [& params]
  (really-helper verily-create-msg params))
