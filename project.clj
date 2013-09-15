(defproject manners "0.1.0-SNAPSHOT"
  :description "A validation library built on using predicates properly."
  :url "https://github.com/RyanMcG/manners"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[speclj "2.5.0"]]}}
  :plugins [[speclj "2.5.0"]]
  :test-paths ["spec/"])
