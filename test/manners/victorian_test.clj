(ns manners.victorian-test
  (:require [manners.victorian :refer :all]
            [clojure.test :refer :all]))

(deftest test-as-coach
  (let [coach (as-coach (constantly []))]
    (is (= (apply coach [1]) []) "applyable")
    (is (= (coach 1) []) "callable"))
  (testing "composes like comp"
    (are [fns v] (= ((apply comp fns) v)
                    ((apply as-coach fns) v))
         [inc inc inc] 3
         [inc inc inc] 1388
         [inc dec inc] 4)))

(deftest test-coach?
  (is (coach? (as-coach (constantly [])))))

(def d-msg "should start with d")
(def keyword-msg "must be a keyword")
(def d-keyword-coach
  (manners [keyword? keyword-msg]
           [(comp (partial re-find #"^d") name) d-msg]))

(deftest test-manners
  (is (= (d-keyword-coach :d) (list)))
  (is (= (d-keyword-coach :a) (list d-msg)))
  (is (= (d-keyword-coach "a") (list keyword-msg d-msg)))
  (is (= (d-keyword-coach "d") (list keyword-msg)))
  (testing "idempotence"
    (let [d-keyword-coach2 (manners (manners (manners d-keyword-coach)))]
      (doseq [v [nil 1 'yp "erp" "derp" :derp]]
        (is (= (d-keyword-coach v)
               (d-keyword-coach2 v)))))))

(def derp-keyword-coach
  (manner d-keyword-coach
          (comp (partial = "derp") name) "name is derp"))

(deftest test-manner
  (testing "simple manner"
    (let [truthy-str-coach (manner identity "is truthy"
                                   string? "is a string")]
      (is (= (truthy-str-coach 1) (list "is a string")))
      (is (= (truthy-str-coach nil) (list "is truthy")))))
  (testing "complex manner with a coach"
    (is (= (derp-keyword-coach :derp) (list)))
    (is (= (derp-keyword-coach "derp") (list keyword-msg)))
    (is (= (derp-keyword-coach "erp")
           (list keyword-msg d-msg)))
    (is (= (derp-keyword-coach :erp) (list d-msg))))
  (testing "idempotence"
    (let [derp-keyword-coach2 (manner (manner (manner derp-keyword-coach)))]
      (doseq [v [nil 1 'yp "erp" "derp" :derp]]
        (is (= (derp-keyword-coach v)
               (derp-keyword-coach2 v)))))))

(def odd-msg "should be odd")
(def num-msg "should be a number")
(def odd-number-etq [[odd? odd-msg]
                     [number? num-msg]])
(def odd-number-coach (etiquette odd-number-etq))

(deftest test-etiquette
  (testing "always returns a sequence"
    (is (sequential? (odd-number-coach nil)))
    (is (sequential? (odd-number-coach 3737))))
  (testing "empty when all predicates pass"
    (is (empty? (odd-number-coach 3))))
  (testing "a sequence of error messages if it does not pass."
    (is (= (list odd-msg) (odd-number-coach 2)))
    (is (= (list odd-msg num-msg) (odd-number-coach nil))))
  (testing "joined manners"
    (let [nine-or-three-msg "it should be 9 or 3"
          nine-or-three-coach
          (manner odd-number-coach
                  (some-fn #(= 9 %) #(= 3 %)) nine-or-three-msg)]
      (testing "short circuits at the first matches predicate"
        (is (= (list odd-msg num-msg) (nine-or-three-coach "hey")))
        (is (= (list odd-msg) (nine-or-three-coach 2)))
        (is (= (list nine-or-three-msg) (nine-or-three-coach 5)))
        (is (= (list) (nine-or-three-coach 3)))))
    (testing "idempotence"
      (let [odd-number-coach2 (etiquette (etiquette odd-number-coach))]
        (doseq [v [nil 1 2 100 78471 :derp]]
          (is (= (odd-number-coach v)
                 (odd-number-coach2 v))))))))

(deftest test-bad-manners
  (testing "works with an empty etiquette"
    (is (= (list) (bad-manners [] nil)))
    (is (= (list) (bad-manners [[]] false))))
  (testing "finds bad-manners"
    (is (= (list odd-msg num-msg)
           (bad-manners odd-number-etq nil)))
    (is (= (list odd-msg) (bad-manners odd-number-etq 2)))
    (is (= (list) (bad-manners odd-number-etq 3)))))

(deftest test-rude?-and-proper?
  (testing "are complements"
    (is (rude? odd-number-etq 2))
    (is (not (proper? odd-number-etq 2)))
    (is (not (rude? odd-number-etq 1)))
    (is (proper? odd-number-etq 1))))

(deftest test-avow
  (testing "throws an error when bad manners are found"
    (is (thrown? AssertionError #"it should be odd, it should be a number"
                 (avow odd-number-etq nil)))
    (is (thrown? AssertionError #"Invalid odd-number: it should be odd"
                 (avow odd-number-etq 2))))
  (testing "does nothing when no bad manners are found"
    (is (nil? (avow odd-number-etq 3)))))

;; An etiquette is a sequence of manners.
;; A manner is either a coach or a predicate message pair.
(deftest test-composable-coaches
  (let [msg1 "should be a map"
        msg2 "should contain the key :hey"
        msg3 "should have an odd number of keys"
        msg4 "should have key :barb"
        msg5 "should length 3 pairs"
        base-coach (manners [map? msg1 :barb msg4]
                            [(comp odd? count) msg3])
        etq [[base-coach :hey msg2]
             [(comp (partial = 3) count) msg5]]
        msg6 "should have key :boom"
        other-etiquette [[:boom msg6 base-coach]]]

    (testing "recognizes a proper value"
      (is (proper? etq {:barb :cats
                        :hey 'yo
                        :anything-else 3})))
    (testing "gets all parallel messages from nested coach"
      (is (= (list msg1 msg3 msg5)
             (bad-manners etq [1 2]))))
    (testing "can get to extended messages if base coach passes"
      (is (= (list msg2 msg5)
             (bad-manners etq {:barb :yo}))))
    (testing "ignores base coach messages unless previous coach in manner passes"
      (is (= (list msg6)
             (bad-manners other-etiquette {:bam 1})))
      (is (= (list msg4)
             (bad-manners other-etiquette {:boom 2})))
      (is (= (list msg4 msg3)
             (bad-manners other-etiquette {:boom 2 :bam 4}))))))

(defmacro catch-message [& body]
  `(try
     ~@body
     (catch AssertionError e#
       (.getMessage e#))))

(deftest test-defmannerisms
  (defmannerisms odd-number odd-number-etq)
  (doseq [v [nil 1 {} 2 "3"]]
    (doseq [[pfunc func] [[proper-odd-number? proper?]
                          [rude-odd-number? rude?]
                          [bad-odd-number-manners bad-manners]]]
      (is (= (pfunc v) (func odd-number-etq v))))
    (is (= (catch-message (avow 'odd-number odd-number-etq v))
           (catch-message (avow-odd-number v))))))
