(ns manners.victorian
  (:require [clojure.string :as s]))

(defn- wrap-try
  "Create a function which has the same behaviour as func but catches all
  exceptions returning nil if one is received."
  [func]
  (fn [& more]
    (try (apply func more) (catch RuntimeException _))))

(defn falter
  "Throw an AssertionError when there are bad manners."
  ([prefix-sym bad-manners]
   (let [full-prefix (when prefix-sym (str "Invalid " prefix-sym ": "))]
     (when-not (empty? bad-manners)
       (throw (AssertionError. (str full-prefix (s/join ", " bad-manners)))))))
  ([bad-manners] (falter nil bad-manners)))

(defn as-coach
  "Memoize and mark the given function as a coach with the meta {:coach true}."
  [coach-fn]
  (with-meta
    (memoize coach-fn)
    {:coach true}))

(defn- pred-msg->coach
  "Create a coach from a predicate and a message. Falter if message is nil."
  [predicate message]
  (if (nil? message)
    (falter 'message ["must not be nil"])
    (as-coach
      (fn [value]
        (sequence (when-not ((wrap-try predicate) value)
                    (list message)))))))

(def coach?
  "A predicate which checks to see if the given value is a coach. It does this
  by seeing if the meta :coach key is true."
  (comp true? :coach meta))

(defn- manner->coaches
  "Return a lazy sequence of coaches from the given manner."
  [manner]
  (let [step (fn [a-manner]
               (when-let [m (seq a-manner)]
                 (let [[coach-or-pred msg] (take 2 m)
                       [coach more]
                       (if (coach? coach-or-pred)
                         [coach-or-pred (next m)]
                         [(pred-msg->coach coach-or-pred msg) (nnext m)])]
                   (cons coach (manner->coaches more)))))]
    (lazy-seq (step manner))))

(defn- invoke-on [value]
  (fn [func] (func value)))

(defn manner
  "Creates a coach from a sequence of coaches and/or predicate message
  pairs."
  [& manner]
  (as-coach
    (fn [value]
      (->> (manner->coaches manner)
           (map (invoke-on value))
           (keep seq)
           (first)
           (sequence)))))

(defn etiquette
  "Create a function from a an etiquette which returns a lazy sequence of bad
  manners."
  [etq]
  {:pre [((some-fn sequential? coach?) etq)]}
  (if (coach? etq)
    etq
    (as-coach (fn [value]
                (->> etq
                     (map (fn [ms]
                            (if (sequential? ms)
                              (apply manner ms)
                              (manner ms))))
                     (mapcat (invoke-on value))
                     (keep identity)
                     (distinct))))))

(defn manners
  "Creates a coach from one or more manners or coaches."
  [& manners-and-coaches]
  (etiquette manners-and-coaches))

(def memoized-etiquette
  "A memoized version of etiquette. Note that the coach etiquette returns will
  be memoized without using memoized-etiquette."
  (memoize etiquette))

(defn bad-manners
  "Return all bad manners found with the given etiquette on the given value."
  [etq value]
  ((memoized-etiquette etq) value))

(defn proper?
  "A predicate to determine if the given value has any bad manners according to the
  validations."
  [etiquette value]
  (empty? (bad-manners etiquette value)))

(def rude?
  "The complement of manners.victorian/proper?."
  (complement proper?))

(defn avow
  "Throw an AssertionError if there are any bad manners found on the given value with
  the given validations."
  ([prefix etiquette value] (falter prefix (bad-manners etiquette value)))
  ([etiquette value] (avow nil etiquette value)))

(defmacro defmannerisms
  "Define helper functions for validating using a consistent etiquette."
  [obj-sym etiquette]
  (let [bad-manners-sym (symbol (str "bad-" obj-sym "-manners"))
        proper?-sym (symbol (str "proper-" obj-sym \?))
        rude?-sym (symbol (str "rude-" obj-sym \?))
        avow-sym (symbol (str "avow-" obj-sym))
        doc-string #(str "A partial of " %
                         " with an etiquette for " obj-sym \.)]
    `(do
       (def ~bad-manners-sym
         ~(doc-string 'bad-manners)
         (partial bad-manners ~etiquette))
       (def ~proper?-sym
         ~(doc-string 'proper?)
         (partial proper? ~etiquette))
       (def ~rude?-sym
         ~(doc-string 'rude?)
         (partial rude? ~etiquette))
       (def ~avow-sym
         ~(doc-string 'avow)
         (partial avow (quote ~obj-sym) ~etiquette)))))
