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

(defn as-coach [coach-fn]
  (with-meta
    (memoize coach-fn)
    {:coach true}))

(defn- pred-msg->coach [predicate message]
  (if message
    (as-coach (fn [value] (when-not ((wrap-try predicate) value)
                            (list message))))
    (falter 'message ["it must not be nil"])))

(def coach? (comp true? :coach meta))

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

(defn- invoke-coaches-on-value [value coaches]
  "Invokes each of the given coaches on the given value. The first non-empty
  sequence is returned."
  (->> coaches
       (map (fn [func] (func value)))
       (keep seq)
       (first)))

(defn- unmemoized-coach
  "Return a memoized function which takes a value to run the given etiquette
  on."
  [etiquette]
  {:pre [(sequential? etiquette)]}
  (as-coach (fn [value]
              (->> etiquette
                   (map manner->coaches)
                   (mapcat (partial invoke-coaches-on-value value))
                   (keep identity)
                   (distinct)))))

;; The actual definition of coach is memoized so that when the same etiquette is
;; passed in we do not generate a new memoized function.
(def coach
  "Create a function from a an etiquette which returns a lazy sequence of bad
  manners."
  (memoize unmemoized-coach))

(defn bad-manners
  "Return all bad manners found with the given etiquette on the given value."
  [etiquette value]
  ((coach etiquette) value))

(defn proper?
  "A predicate to determine if the given value has any bad manners according to the
  validations."
  [etiquette value]
  (empty? (bad-manners etiquette value)))

(def rude?
  "The complement of manners.victorian/proper?."
  (complement proper?))

(defn avow!
  "Throw an AssertionError if there are any bad manners found on the given value with
  the given validations."
  ([prefix etiquette value] (falter prefix (bad-manners etiquette value)))
  ([etiquette value] (avow! nil etiquette value)))

(defmacro defmannerisms
  "Define helper functions for validating using a consistent etiquette."
  [obj-sym & etiquette]
  (let [bad-manners-sym (symbol (str "bad-" obj-sym "-manners"))
        proper?-sym (symbol (str "proper-" obj-sym \?))
        rude?-sym (symbol (str "rude-" obj-sym \?))
        avow!-sym (symbol (str "avow-" obj-sym \!))]
    `(do
       (def ~bad-manners-sym
         (partial bad-manners [~@etiquette]))
       (def ~proper?-sym
         (partial proper? [~@etiquette]))
       (def ~rude?-sym
         (partial rude? [~@etiquette]))
       (defn ~avow!-sym [value#]
         (avow! (quote ~obj-sym) [~@etiquette] value#)))))
