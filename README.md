# manners

A validation library built on using predicates properly.

## Thoughts

This library is the result of reading [Functional JavaScript][] and being
inspired by the simplicity of the validation functions [the author][fogus]
creates. Composing predicates is a very easy thing to do in Clojure and I wanted
a library which takes advantage of that [without any magic](#comparisons). The
[valip][] library takes advantage of simple predicates but it is limited to
validating maps.  Although maps are probably the most common thing to validate
only working on one type of value is limiting.

Additionally, for those who think validation libraries take themselves too
seriously, *manners* has two dialects: [victorian](#victorian) and
[modern](#modern).

## Usage &amp; Dialect

Traditional [API documentation][doc] can be found [here][doc].

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
  returning a sequence of messages for the predicates that fail. The returned
  function is called an etiquette coach (see what I did there?).

All of the remaining functions are just helpers. Consider `bad-manners` which
works like so:

### `bad-manners` &amp; `coach`

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

#### `proper?` &amp; `rude?`

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

#### `avow!` &amp; `falter`

Next on the list is `avow!` which takes the results of a call to `bad-manners`
and throws an `AssertionError` when a non-empty sequence is returned.
Internally `avow!` is conceptionally like the composition of `falter` (which
does the throwing) and an etiquette coach.

```clojure
;; assume `etiquette` is a valid etiquette and `value` is the value have
;; predicates applied to.
((comp falter (coach etiquette)) value)
```

#### `defmannerisms`

The last part of the API is `defmannerisms`. This is a helper macro for defining
functions that wrap the core API and a given etiquette.

```clojure
(defmannerisms empty-thing
  [identity "must be truthy"]
  [empty? "must be empty"])

(proper-empty-thing? []) ; => true
(rude-empty-thing? []) ; => false
(bad-empty-thing-manners nil) ; => ("must be truthy")
(avow-empty-thing! 1)
; throws and AssertionError with the message:
;   Invalid empty-thing: must be truthy

;; And so on.
```

### Modern

There is nothing new with the modern dialect. It is composed of aliases to the
victorian functions. Its target audience is for more serious folk who like
consistent, commonly used terminology. It is also for people who do not like
Dickens.

| victorian   | modern       |
| ----------- | ------------ |
| coach       | validator    |
| bad-manners | errors       |
| proper?     | valid?       |
| rude?       | invalid?     |
| falter      | throw-errors |
| avow!       | validate!    |

The only thing modern is missing is the `defmannerisms` function defining macro.

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

## Comparisons

Clojure already has quite a few good validation libraries. This library is not
greatly different and has no groundbreaking features. However, it does differ a
couple of key ways. The following are some descriptions of other validation
libraries in the wild. They are listed in alphabetical.

* [*bouncer*](https://github.com/leonardoborges/bouncer) is a neat DSL for
  validating maps which works particularly well on nested maps. It accepts
  predicates as validators and even checks custom meta on those predicates for
  special behaviour like message formatting. Like *mississippi* it also returns
  a custom formated data structure:

  > `validate` takes a map and one or more validation forms and returns a
  > vector.
  >
  > The first element in this vector contains a map of the error messages,
  > whereas the second element contains the original map, augmented with the
  > error messages.

* [*metis*](https://github.com/mylesmegyesi/metis) is a keyword based DSL for
  validating maps by creating higher order functions. Like the previous two it
  returns a map with reusing the validated keys with values being a sequence of
  errors. It advertises validator composition as well which works great for
  validating nested maps.
* [*mississippi*](https://github.com/mikejones/mississippi) offers a lot of the
  same functionality by returning maps containing errors on each key. Of course
  that means this library is useful for validating maps only.
* [*Red Tape*](http://sjl.bitbucket.org/red-tape/) is a form validation library.
  It is not meant to be general purpose at all in an effort to "reduce friction"
  in its problem domain.
* [*sandbar*](https://github.com/brentonashworth/sandbar) is a web application
  library which, along with many other neat features, offers form generation
  with built in validation. Obviously this is also very focused on its singular
  use case.
* [*validateur*](https://github.com/michaelklishin/validateur) a popular library
  with good documentation which does much the same thing is *mississippi*.
* [*valip*](https://github.com/weavejester/valip) is perhaps the must similar of
  the validation libraries to *manners* in that it is based on the simple
  application of predicates. However does not differ greatly from *validateur*
  or *mississippi*. It also provides [a helpful suite of predicates and
  higher-order predicate generating functions][valip.predicates] which are
  compatible with *manners* (since they are just predicates).

Clearly validating maps is a common problem in Clojure. I think this is due to
the web application use case of validating parameters as well as the use of
custom maps without a strongly defined (i.e. typed) schema. Personally I find
Clojure's default data types empowering and it seems like others do as well,
enough to prefer validating maps using libraries in some cases. The case for
validating arbitrary values exists though and the fact that none of the above
offers validation on non-maps would be bizarre if they were not all designed to
worked with a keyed type of data structure. Having been primarily doing Rails
development for my so far short professional career I've become accustomed to
keyed errors but I have rarely found much value in them. When I want to flash a
message to a user I do want a well worded message and
`Model#errors.full_messages` has never been it.

In an effort to be more generic this library differs from those above by not
caring on what field an error occurred. There is no concept of a value needing
to have fields at all. Another consequence of not requiring fields is that
validating related fields is easier. Consider the following etiquette and data.

```clojure
(def data {:count 3 :words ["big" "bad" "wolf"]})
(defn count-words-equal? [{cnt :count words :words}]
  ((= cnt (count words))
(def etiquette
  [[(comp number? :count) "count must be a number"]
   [count-words-equal? "count must equal the length of the words sequence"]])
```

This etiquette works fine with *manners*. Other libraries make validating
relationships between fields much more difficult by limiting the scope of
predicates to a single field at a time. The benefit of doing it that way is you
can concisely define per field validations. The alternative of having to drill
down to the field you mean to apply your predicate may seem like more work but
using compose it is still quite concise (see above example).

## Test

For testing this project uses [speclj][]. Running the tests is pretty easy:

```bash
lein spec
# The vigilant runner works too
lein spec -a
```

You can even run the tests interactively by requiring the file they are in via
the magic of [speclj's `run-specs` function][speclj-run-specs].

## License

Copyright © 2012 Ryan McGowan

Distributed under the Eclipse Public License, the same as Clojure.

[Functional JavaScript]: http://www.amazon.com/Functional-JavaScript-Introducing-Programming-Underscore-js/dp/1449360726
[speclj]: https://github.com/slagyr/speclj
[speclj-run-specs]: https://github.com/slagyr/speclj/#run-specs
[fogus]: http://fogus.me/
[valip]: https://github.com/weavejester/valip
[valip.predicates]: https://github.com/weavejester/valip/blob/master/src/valip/predicates.clj
[doc]: http://ryanmcg.github.io/manners