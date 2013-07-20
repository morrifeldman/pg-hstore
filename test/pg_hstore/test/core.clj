(ns pg-hstore.test.core
  (:require [pg-hstore.core :refer :all]
            [clojure.string :as st]
            [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s]
            [midje.sweet :refer :all]))

(defonce db {:subprotocol "postgresql"
             :subname "//127.0.0.1:3306/clojure_test"
             :user (st/trim (:out (clojure.java.shell/sh "whoami")))})

(defmacro data-transform-tester [d]
  `(let [[n# m# encoded#] ~d]
     (fact n# (to-hstore m# true) => encoded#)
     (fact n# (.getValue (to-hstore m#)) => encoded#)))

(defmacro data-integration-tester [db d]
  `(let [[n# m# encoded#] ~d
         prepared-sql# "SELECT ?::hstore AS hstore"]
     (fact "test load prepared string"
       (let [ret# (.getValue (:hstore (first (j/query ~db [prepared-sql# (to-hstore m#)]))))]
         ret# => (st/replace encoded# "," ", ")
         (from-hstore ret#) => m#))))

(defmacro nasty-transform-tester [n]
  `(fact "should be able to parse its own output"
     (dotimes [i# 3]
       (let [hstore# (to-hstore ~n true)
             data# (from-hstore hstore#)]
         data# => ~n))))

(defmacro nasty-integration-tester [db n]
  `(let [prepared-sql# "SELECT ?::hstore AS hstore"]
     (fact "test load prepared string"
       (from-hstore (:hstore
                      (first (j/query ~db [prepared-sql#
                                           (to-hstore ~n true)]))) true) => ~n)))

(facts "hstore from maps"
  (fact "should set a value correctly"
    (let [hstore "\"ip\"=>\"17.34.44.22\", \"service_available?\"=>\"false\""]
      (:service_available? (from-hstore hstore)) => "false"))
  (fact "should parse an empty string"
    (let [hstore "\"ip\"=>\"\", \"service_available?\"=>\"false\""]
      (:ip (from-hstore hstore)) => ""))
  (fact "should parse NULL as a value"
    (:x (from-hstore "\"x\"=>NULL")) => nil)
  (let [ db {:subprotocol "postgresql"
             :subname "//127.0.0.1:5432/pg-hstore-test"
             :user (st/trim (:out (clojure.java.shell/sh "whoami")))}]
    (j/db-do-commands db "CREATE EXTENSION IF NOT EXISTS hstore")
    (j/db-do-commands db "SET standard_conforming_strings=on")
    (let [data [["should translate into an SQL literal"
                 {:a "b", :foo "bar"}
                 "\"a\"=>\"b\",\"foo\"=>\"bar\""
                 ]
                ["should store an empty string"
                 {:nothing ""}
                 "\"nothing\"=>\"\""
                 ]
                ["should render nil as NULL"
                 {:x nil}
                 "\"x\"=>NULL"
                 ]
                ["should support single quotes in strings"
                 {:journey "don't stop believin'"}
                 "\"journey\"=>\"don't stop believin'\""
                 ]
                ["should support double quotes in strings"
                 {:journey "He said he was \"ready\""}
                 "\"journey\"=>\"He said he was \\\"ready\\\"\""
                 ]
                ["should escape \\ garbage in strings",
                 {:line_noise "perl -p -e 's/\\$\\{([^}]+\\}/]"}
                 "\"line_noise\"=>\"perl -p -e 's/\\\\$\\\\{([^}]+\\\\}/]\""
                 ]]
          nasty [{:journey "He said he was ready"}
                 {:a "\\"}
                 {:b "\\\\"}
                 {:b1 "\\\\\""}
                 {:b2 "\\\\\"\\\\"}
                 {:c "\\\\\\"}
                 {:d "\\\"\\\"\"\\"}
                 {:d1 "\\\"\\\"\\\"\"\\"}
                 {:e "''"}
                 {:e "\\'\\''\\"}
                 {:e1 "\\'\\''\""}
                 {:f "\\\\\"\\\"\"\\"}
                 {:g "\\'\\''\\"}
                 {:h "$$; SELECT 'lol=>lol' AS hstore; --"}
                 {:z "');SELECT 'lol=>lol' AS hstore;--"}]]
      (facts "should transform correctly"
        (doseq [d data] (data-transform-tester d))
        (doseq [n nasty] (nasty-transform-tester n)))
      (facts "should produce stuff that postgres really likes"
        (doseq [d data] (data-integration-tester db d))
        (doseq [n nasty] (nasty-integration-tester db n))))
    (fact "should be able to parse hstore strings without ''"
      (let [data {:journey "He said he was ready"}
            literal (to-hstore data true)
            parsed (from-hstore literal)]
        parsed => data))
    (fact "should be stable over iteration"
      (let [dump (to-hstore {:journey "He said he was \"ready\""} true)
            original dump
            parse (from-hstore dump)]
        (dotimes [i 10]
          (let [parsed (from-hstore dump)
                dump (to-hstore parsed true)]
            dump => original))))))

