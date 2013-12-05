# pg-hstore

A Clojure library designed to transform clojure maps to PostgreSQL hstore objects.

## Installation

Add pg-hstore to your project.clj file in leiningen:

```clj
    [pg-hstore "3.0.0"]
```

## Usage

Once installed, you can use it like so:

    user> (use '[pg-hstore.core])
    nil
    user> (def h (to-hstore {:color "blue" :size "small"}))
    #'user/h
    user> h
    #<PGobject "size"=>"small","color"=>"blue">
    user> (from-hstore h)
    {:color "blue", :size "small"}
    user>

## [Korma](http://sqlkorma.com/) example

If you have a table named "products" with an hstore column called "attributes", a korma insertion statement might look like this:

```clj
    (insert products
      (values {:name "computer"
               :attributes (to-hstore {:color "black" :manufacturer "samsung"})}))
```

When you pull the row back out again, just call "from-hstore" on the value at attributes.

    user> row
    {:name "computer", :attributes #<PGobject E'"manufacturer"=>"samsung","color"=>"black"'>}
    user> (from-hstore (:attributes row))
    {:color "black", :manufacturer "samsung"}
    user>

If you are using Korma's ```defentity``` function, you can apply the hstore translation at the entity rather than at query-time. For the above products example, your entity might look something like so:

```clj
    (defentity products
      (prepare #(update-in % [:attributes] to-hstore))
      (transform #(update-in % [:attributes] from-hstore)))
```

With this in place, the translation is transparent and you can insert plain data structures:

```clj
    (insert products
      (values {:name "computer"
               :attributes {:color "black" :manufacturer "samsung"}}))
```

## Attributions

This library was made possible because of two existing libraries:

pghstore-clj (clojure / EPL 1.0) : https://github.com/blakesmith/pghstore-clj

AND

pg-hstore (ruby / MIT License) : https://github.com/seamusabshere/pg-hstore

The code in this library is based on these two excellent libraries.

## License

Copyright © 2013 The Learning Egg, LLC

Distributed under the BSD 3-Clause License, http://opensource.org/licenses/BSD-3-Clause

