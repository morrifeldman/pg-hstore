(defproject pg-hstore "3.0.0"
  :description "A Clojure library designed to transform clojure maps to PostgreSQL hstore objects"
  :url "https://github.com/learningegg/pg-hstore"
  :license {:name "The BSD 3-Clause License"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.postgresql/postgresql "9.3-1100-jdbc4"]]
  :profiles {:dev {:source-paths ["dev"]
                 :dependencies [[midje "1.5.1"]
                                [org.clojure/java.jdbc "0.3.0-beta2"]
                                [java-jdbc/dsl "0.1.0"]
                                [org.clojure/tools.namespace "0.2.4"]
                                [org.clojure/java.classpath "0.2.0"]]}}
  :repl-options {:init-ns user})
