(ns manners.bellman-test
  (:require [manners.bellman :refer :all]
            [clojure.test :refer :all]))

(def ^:private messages ["b" "c"])

(deftest test-prefix
  (is (= ((prefix "a ") messages) ["a b" "a c"]))
  (is (= ((prefix {}) messages) ["{}b" "{}c"])))

(deftest test-suffix
  (is (= ((suffix " a") messages) ["b a" "c a"]))
  (is (= ((suffix {}) messages) ["b{}" "c{}"])))

(deftest test-at
  (let [v {:a {:b {:c 4}}}
        three-coach (constantly ["must be 3"])
        four-coach (constantly [])]
    (is (= ((at three-coach :a :b :c) v) ["a b c must be 3"]))
    (is (= ((at four-coach  :a :b :c) v) []))))

(deftest test-specifiying
  (testing "a very simple specifying function"
    (let [derp (constantly "derp")
          a-coach (constantly ["yep" "anything, this will get overridden"])
          derp-coach (specifiying derp a-coach)]
      (is (= (derp-coach 1) ["derp" "derp"]))))

  (testing "speciying function using message and value"
    (let [a-coach (constantly [10 3828])
          spec-coach (specifiying / a-coach)]
      (is (= (spec-coach 10) [1 3828/10])))))

(deftest test-formatting
  (is (= ((formatting (constantly ["I got a %s"])) "thing") ["I got a thing"]))
  (is (= ((formatting (constantly ["I got a %8.2f"])) 1234.0) ["I got a  1234.00"])))

(deftest test-invoking
  (let [i-coach (invoking (constantly [identity inc]))]
    (is (= (i-coach 1) [1 2]))
    (is (= (i-coach 388) [388 389]))))

#_(run-tests)
