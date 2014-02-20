(ns manners.with-test
  (:require [clojure.test :refer :all]
            [manners.with :refer :all]))

(deftest test-with-etiquette
  (with-etiquette [[odd? "it is odd"]
                   [#(= 0 (mod % 5)) "it is divisible by 5"]]
    (is (proper? 5))
    (is (rude? 9))
    (is (= (bad-manners 21) (list "it is divisible by 5")))
    (is (thrown? AssertionError (avow 19)))
    (is (nil? (avow 25))))
  (is (thrown? AssertionError (rude? 9))))

#_(run-tests)
