## 3.0.3

- trying to get clojars deployment to work

## 3.0.2

- trying to get clojars deployment to work

## 3.0.1

- trying to get clojars deployment to work

## 3.0.0

- adding extension for java.util.HashMap
- updating to postgresql jdbc 9.3-1100-jdbc4

## 2.0.1

- fixing unneeded "E" escaping with PGobject (didn't realize it didn't
  need it)
- removing superfluous tests

## 2.0.0

- Changed the default for PGobject to symbolize keys in map instead of
  using strings. This seems more idiomatic clojure and mirrors the default
  String return as well.

