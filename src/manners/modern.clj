(ns manners.modern
  (:require [manners.utils :refer [defalias]]
            [manners.victorian :as vic]))

;; Define aliases to victorian functions to form a more modern dialect. This is
;; for use cases where a more common, widely used vocabulary is preferred.
(defalias valid? vic/proper?)
(defalias invalid? vic/rude?)
(defalias errors vic/bad-manners)
(defalias validate! vic/avow!)
(defalias validator vic/coach)
(defalias throw-errors vic/falter)
