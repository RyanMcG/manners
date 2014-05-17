(ns manners.really-test
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [manners.victorian :refer [coach?]]
            [manners.really :refer :all]))

(deftest test-really
  (testing "without more arguments"
    (let [string-coach (really "must be a" string?)]
      (is (coach? string-coach))
      (is (= (string-coach "") []))
      (is (= (string-coach 1) ["must be a string"]))))

  (testing "with more arguments"
    (let [ten 10
          lt-10-coach (really "must be" < 10)
          lt-ten-coach (really "must be" < ten)]
      (is (coach? lt-10-coach))
      (testing "really created coaches message the same with the same values"
        (are [v msgs] (= (lt-10-coach v) (lt-ten-coach v) msgs)
             9 []
             -88 []
             10 ["must be < 10"]))))
  (testing "with a function call as the predicate"
    (let [be (fn [kw] (fn [v] (= (name kw) (name v))))
          be-happy-coach (really "must" (be :happy))]
        (are [v msgs] (= (be-happy-coach v) msgs)
             :happy []
             :sad ["must be :happy"]))))

(deftest test-verily
  (testing "without more arguments behaves the same as really"
    (let [v-string-coach (verily "must be a" string?)
          r-string-coach (really "must be a" string?)]
      (is (coach? v-string-coach))
      (are [v] (= (v-string-coach v) (r-string-coach v))
           \y 2 " " nil 'hey-there {})))

  (testing "with more arguments"
    (let [ten 10
          lt-10-coach (verily "must be" < 10)
          lt-str-ten-coach (verily "must be" < "ten")
          lt-ten-coach (verily "must be" < ten)]
      (is (coach? lt-10-coach))
      (testing "verily created coaches message the same with same looking params"
        (are [v msgs] (= (lt-10-coach v) (lt-ten-coach v) msgs)
             9 []
             -88 [])
        (are [v msgs] (= (lt-str-ten-coach v) (lt-ten-coach v) msgs)
             10 ["must be < ten"])
        (is (= (lt-ten-coach 10) ["must be < ten"]))
        (is (= (lt-10-coach 10) ["must be < 10"]))))))

#_(run-tests)
