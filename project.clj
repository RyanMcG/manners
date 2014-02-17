(defproject manners "0.3.0"
  :description "A validation library built on using predicates properly."
  :url "https://github.com/RyanMcG/manners"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[speclj "2.5.0"]]}}
  :plugins [[codox "0.6.4"]
            [speclj "2.5.0"]]
  :codox {:src-uri "https://github.com/RyanMcG/manners"
          :src-dir-uri "https://github.com/RyanMcG/manners/blob/master"
          :src-linenum-anchor-prefix "L"}
  :test-paths ["spec/"])
