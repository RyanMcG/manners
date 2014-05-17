(ns manners.bellman
  "A namespace containing functions which modifiy messages from coaches or
  create new coaches from given ones that improve messages."
  (:require [manners.victorian :refer [as-coach]]
            [clojure.string :as s]))

(defn modify [modifier]
  "Apply the given modifying function to every message."
  (partial map modifier))

(defn prefix "Add a prefix to messages"
  [some-str] (modify #(str some-str %)))

(defn suffix "Add a suffix to messages."
  [some-str] (modify #(str % some-str)))

(defn- key-names [keys]
  (->> keys (map (fn [k] ((if keyword? name str) k))) (s/join " ")))

(defn at
  "Create a coach for a map by applying the given coach at the given path of
  keys."
  [coach & path]
  (as-coach (prefix (str (key-names path) \space))
            coach
            #(get-in % path)))

(defn specifiying
  "Create a new coach from a function which transforms messages from the given
  coach. This specifying function takes two arguments, the message and the
  value."
  [specify coach]
  (as-coach
    (fn specifying-coach [v]
      (map #(specify % v) (coach v)))))

(def formatting
  "Call format on messages returned by the given coach where the second argument
  is the value."
  (partial specifiying format))

(defn- invoke [f v] (f v))
(def invoking
  "Assume each message is a function which takes the value returning a new
  message."
  (partial specifiying invoke))
