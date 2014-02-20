(ns manners.with-test
  (:require [speclj.core :refer :all]
            [manners.with :refer :all]))

(describe "with-etiquette"
  (with etq [[odd? "it is odd"]
                   [#(= 0 (mod % 5)) "it is divisible by 5"]])
  (context "works inside"
    (around [it] (with-etiquette @etq (it)))
    (it "in victorian dialect"
      (should (proper? 5))
      (should (rude? 9))
      (should-contain "it is divisible by 5" (bad-manners 21))
      (should-throw AssertionError (avow 19))
      (should-not-throw (avow 25))))
  (it "does not work outside"
    (should-throw AssertionError (rude? 9))))

(run-specs)
