(ns pg-hstore.core
  (:require [clojure.string :as st])
  (:import [org.postgresql.util PGobject]))

(def ^:private single-quote "'")
(def ^:private e-single-quote "E'")
(def ^:private double-quote "\"")
(def ^:private hashrocket "=>")
(def ^:private comma ",")
(def ^:private slash "\\")
(def ^:private null-string "NULL")

(def ^:private escaped-char #"\\(.)")
(def ^:private escaped-single-quote "\\'")
(def ^:private escaped-double-quote "\\\"")
(def ^:private escaped-slash "\\\\")

(def ^:private e-single-quote-stripper (re-pattern (str "(" e-single-quote ")(.+)")))
(def ^:private quoted-literal #"\"[^\"\\]*(?:\\.[^\"\\]*)*\"")
(def ^:private unquoted-literal #"[^\s=,][^\s=,\\]*(?:\\.[^\s=,\\]*|=[^,>])*")
(def ^:private literal (re-pattern (str "(" quoted-literal "|" unquoted-literal ")")))
(def ^:private pair (re-pattern (str literal "\\s*=>\\s*" literal)))
(def ^:private null #"(?i)\ANULL\z")

(defprotocol THstorable
  (to-hstore [this]
             [this raw-string?]))

(defprotocol FHstorable
  (from-hstore [this]
               [this symbolize-keys?]))

(defn- escape-nonnull-for-hstore [s]
  (str double-quote
       (-> s
           (st/replace slash escaped-slash)
           (st/replace double-quote escaped-double-quote))
       double-quote))

(defn- create-key-value-pair [k v]
  (let [k (escape-nonnull-for-hstore k)
        v (if (nil? v) null-string (escape-nonnull-for-hstore v))]
    (st/join hashrocket [k v])))

(defn- as-postgresql-string-constant [s]
  (str e-single-quote
       (-> s
           (st/replace slash escaped-slash)
           (st/replace single-quote escaped-single-quote))
       single-quote))

(defn- s-unquote [s quote-char]
  (let [s (or (last (re-find e-single-quote-stripper s)) s)]
    (if (.startsWith s quote-char)
      (let [sl (.length s)
            l (.length quote-char)]
        (subs s l (- sl l)))
      s)))

(defn- unescape [l]
  (st/replace l escaped-char "$1"))

(defn- from-hstore-to-map [s symbolize-keys?]
  (reduce (fn [memo [k v]]
            (let [k (unescape (s-unquote k double-quote))
                  k (if symbolize-keys? (keyword k) k)
                  v (if (re-find null v)
                      nil
                      (unescape (s-unquote v double-quote)))]
              (merge memo {k v})))
          {}
          (map #(rest %) (re-seq pair s))))

(extend-type org.postgresql.util.PGobject
  FHstorable
  (from-hstore
    ([this] (from-hstore this false))
    ([this symbolize-keys?]
     (when (= (.getType this) "hstore")
       (from-hstore-to-map (.getValue this) symbolize-keys?)))))

(extend-type java.lang.String
  FHstorable
  (from-hstore
    ([this] (from-hstore this true))
    ([this symbolize-keys?]
     (from-hstore-to-map this symbolize-keys?))))

(extend-type clojure.lang.IPersistentMap
  THstorable
  (to-hstore
    ([this] (to-hstore this false))
    ([this raw-string?]
     (let [kvps (for [[k v] this] (create-key-value-pair (name k) v))
           hstore (apply str (interpose comma (reverse kvps)))]
       (if raw-string?
         hstore
         (doto (PGobject.)
           (.setType "hstore")
           (.setValue
             (as-postgresql-string-constant hstore))))))))

