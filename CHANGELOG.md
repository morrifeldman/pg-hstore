## 2.0.1

- fixing unneeded "E" escaping with PGobject (didn't realize it didn't
  need it)
- removing superfluous tests

## 2.0.0

- Changed the default for PGobject to symbolize keys in map instead of
  using strings. This seems more idiomatic clojure and mirrors the default
  String return as well.

