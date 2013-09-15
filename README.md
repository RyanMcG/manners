# manners

A validation library built on using predicates properly.

## Thoughts

This library is the result of reading [Functional JavaScript][] and being
inspired by the simplicity of the validation functions [the author][fogus]
creates. Composing predicates is a very easy thing to do in Clojure and I wanted
a library which takes advantage of that without any magic. The [valip][] library
takes advantage of simple predicates but it is limited to validating maps.
Although maps are probably the most common thing to validate only working on one
type of value is limiting.

Additionally, for those who think validation libraries take themselves to
seriously, manners has two dialects: [victorian](#victorian) and
[modern](#modern).

## Usage &amp; Dialect

<a id="victorian"></a>
### Victorian

First some terms vital to the victorian manner's lexicon.

* etiquette -- A sequence of manners
* manner -- A predicate and a message as the first and second arguments of a
  sequential collection.

  ```clojure
  [empty? "Must be empty."]
  ```

* `coach` - A higher order function which takes an etiquette and returns a
  function that applies the predicates from the etiquette to its argument
  returning a sequence of messages for the predicates that fail.

All of the remaining functions are just helpers. Consider `bad-manners` which
works like so:

```clojure
(use 'manners.victorian)
(def etiquette [[even? "must be even"]
                [#(>= % 19) "must be greater than or equal to 19"]])
(bad-manners etiquette 11)
; => ("must be even" "must be greater than or equal to 19")
(bad-manners etiquette 10) ; => ("must be greater than or equal to 19")
(bad-manners etiquette 19) ; => ("must be even")
(bad-manners etiquette 20) ; => ()
```

`bad-manners` is simply defined as:
```clojure
(defn bad-manners
  [etiquette value]
  ((coach etiquette) value))
```

Memoization is used so that subsequent calls to coach and the function generated
by coach does not repeat any work. That also means predicates used in an
etiquette should be referentially transparent.

Next are `proper?` and `rude?`.  They are complements of each other.

```clojure
;; continuing with the etiquette defined above.
(proper? etiquette 19) ; => false
(proper? etiquette 20) ; => true
(rude? etiquette 19)   ; => true
(rude? etiquette 20)   ; => false
```

`proper?` is defined by calling `empty?` on the result of `bad-manners`. With
the memoization you can call `proper?` then check `bad-manners` without doubling
the work.

```clojure
(if (proper? etiquette some-value)
  (success-func)
  (failure-func (bad-manners etiquette some-value))
```

Of course we all want to be dry so you could do the same as above with a bit
more work that does not rely on the memoization. Pick your poison.

```clojure
(let [bad-stuff (bad-manners etiquette some-value)]
  (if (empty? bad-stuff)
    (success-func)
    (failure-func bad-stuff)))
```

<a id="modern"></a>
### Modern

There is nothing new with the modern dialect. It is composed of aliases to the
victorian functions. Its target audience is for more serious folk who like
consistent, commonly used terminology. It is also for people who do not like
Dickens.

| victorian   | modern       |
| ----------- | ------------ |
| proper?     | valid?       |
| rude?       | invalid?     |
| bad-manners | errors       |
| avow!       | validate!    |
| falter      | throw-errors |
| coach       | validator    |

The only thing modern is missing is the `defmannerisms` function defining macro.

<a id="with"></a>
### With

To avoid having to consistently pass in etiquette as a first argument you can
use the `with-etiquette` macro. It works with both dialects.

```clojure
(use 'manners.with)
(with-etiquette [[even? "must be even"]
                [#(>= % 19) "must be greater than or equal to 19"]]
  (proper? 10) ; => false
  (invalid? 11) ; => true
  (errors 19) ; => ("must be even")
  (bad-manners 20)) ; => ()
```


## Test

For testing this project uses [speclj][]. Running the tests is pretty easy:

```bash
lein spec
# The vigilant runner works too
lein spec -a
```

You can even run the tests interactively by requiring the file they are in via
the magic of [speclj's `run-specs` function][specljs-run-specs].

## License

Copyright © 2012 Ryan McGowan

Distributed under the Eclipse Public License, the same as Clojure.

[Functional JavaScript]: http://www.amazon.com/Functional-JavaScript-Introducing-Programming-Underscore-js/dp/1449360726
[speclj]: https://github.com/slagyr/speclj
[specljs-run-specs]: https://github.com/slagyr/speclj/#run-specs
[fogus]: http://fogus.me/
[valip]: https://github.com/weavejester/valip
