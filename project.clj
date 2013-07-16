(defproject pg-hstore "1.0.0"
  :description "A Clojure library designed to transform clojure maps to PostgreSQL hstore objects"
  :url "https://github.com/learningegg/pg-hstore"
  :license {:name "The BSD 3-Clause License"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [postgresql "9.1-901.jdbc4"]]
  :profiles {:dev {:source-paths ["dev"]
                 :dependencies [[midje "1.5.1"]
                                [org.clojure/java.jdbc "0.3.0-alpha4"]
                                [org.clojure/tools.namespace "0.2.3"]
                                [org.clojure/java.classpath "0.2.0"]]}}
  :repl-options {:init-ns user})
