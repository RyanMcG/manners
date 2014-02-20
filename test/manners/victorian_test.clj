(ns manners.victorian-test
  (:require [manners.victorian :refer :all]
            [clojure.test :refer :all]
            [speclj.core :refer :all]))

(def d-msg "should start with d")
(def keyword-msg "must be a keyword")
(def d-keyword-coach
  (manners [keyword? keyword-msg]
           [(comp (partial re-find #"^d") name) d-msg]))

(deftest test-manners
  (is (= (d-keyword-coach :d) (list)))
  (is (= (d-keyword-coach :a) (list d-msg)))
  (is (= (d-keyword-coach "a") (list keyword-msg d-msg)))
  (is (= (d-keyword-coach "d") (list keyword-msg))))

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
    (is (= (derp-keyword-coach :erp) (list d-msg)))))

(describe "manners"
  (with nil-coach (manners [odd? "it should be odd"]
                           [number? "it should be a number"]))
  (it "always returns a sequence"
    (should (sequential? (@nil-coach nil)))
    (should (sequential? (@nil-coach 3737))))
  (it "empty when all predicates pass"
    (should (empty? (@nil-coach 3))))
  (it "a sequence of error messages if it does not pass."
    (should= (list "it should be odd") (@nil-coach 2))
    (should-contain "it should be a number" (@nil-coach nil)))
  (context "joined manners"
    (with nine-or-three-coach (manner number? "it should be a number"
                                      odd? "it should be odd"
                                      (some-fn #(= 9 %) #(= 3 %)) "it should be 9 or 3"))
    (it "short circuits at the first matches predicate"
      (should= (list "it should be a number") (@nine-or-three-coach "hey"))
      (should= (list "it should be odd") (@nine-or-three-coach 2))
      (should= (list "it should be 9 or 3") (@nine-or-three-coach 5))
      (should= (list) (@nine-or-three-coach 3)))))

(describe "bad-manners"
  (with validations [[odd? "it should be odd"]
                     [number? "it should be a number"]])
  (it "works with an empty etiquette"
    (should= (list) (bad-manners [] nil))
    (should= (list) (bad-manners [[]] false)))
  (it "finds bad-manners"
    (should= (list "it should be odd" "it should be a number")
             (bad-manners @validations nil))
    (should= (list "it should be odd") (bad-manners @validations 2))
    (should= (list) (bad-manners @validations 3))))

(describe "rude? and proper?"
  (with validations [[odd? "it should be odd"]
                     [number? "it should be a number"]])
  (it "are complements"
    (should (rude? @validations 2))
    (should-not (proper? @validations 2))
    (should-not (rude? @validations 1))
    (should (proper? @validations 1))))

(describe "avow"
  (with validations [[odd? "it should be odd"]
                     [number? "it should be a number"]])
  (it "throws an error when bad manners are found"
    (should-throw AssertionError "it should be odd, it should be a number"
                  (avow @validations nil))
    (should-throw AssertionError "Invalid odd-number: it should be odd"
                  (avow 'odd-number @validations 2)))
  (it "does nothing when no bad manners are found"
    (should-not-throw (avow @validations 3))))

;; An etiquette is a sequence of manners.
;; A manner is either a coach or a predicate message pair.
(describe "composable coaches"
  (with msg1 "should be a map")
  (with msg2 "should contain the key :hey")
  (with msg3 "should have an odd number of keys")
  (with msg4 "should have key :barb")
  (with msg5 "should length 3 pairs")
  (with base-coach (manners [map? @msg1 :barb @msg4]
                            [(comp odd? count) @msg3]))
  (with etq [[@base-coach :hey @msg2]
             [(comp (partial = 3) count) @msg5]])
  (it "recognizes a proper value"
    (should (proper? @etq {:barb :cats
                           :hey 'yo
                           :anything-else 3})))
  (it "gets all parellel messages from nested coach"
    (should= (list @msg1 @msg3 @msg5)
             (bad-manners @etq [1 2])))
  (it "can get to extended messages if base coach passes"
    (should= (list @msg2 @msg5)
             (bad-manners @etq {:barb :yo})))

  (with msg6 "should have key :boom")
  (with other-etiquette [[:boom @msg6 @base-coach]])
  (it "ignores base coach messages unless previous coach in manner passes"
    (should= (list @msg6)
             (bad-manners @other-etiquette {:bam 1}))
    (should= (list @msg4)
             (bad-manners @other-etiquette {:boom 2}))
    (should= (list @msg4 @msg3)
             (bad-manners @other-etiquette {:boom 2 :bam 4}))))

(run-specs)
(run-tests)
