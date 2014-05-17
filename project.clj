(defproject manners "0.6.0"
  :description "A validation library built on using predicates properly."
  :url "https://github.com/RyanMcG/manners"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:aliases {"incise" ^:pass-through-help ["run" "-m" "incise.core"]}
                   :resource-paths ["dev-resources"]
                   :dependencies [[com.ryanmcg/incise-vm-layout "0.4.0"]
                                  [incise "0.3.2"]
                                  [com.ryanmcg/incise-codox "0.1.0"]]}})
