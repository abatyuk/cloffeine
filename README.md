Simple clojure wrapper around https://github.com/ben-manes/caffeine cache.

Usage:
------
```clojure
(require '[cloffeine.core :as cc])
(def settings (cc/map->CacheSettings {:maximumSize 5 :expireAfterWrite 3}))
(def cache (cc/make-cache settings))
(cc/put cache :a 1)
(cc/get-if-present cache :a)
```

Todo:
-----
* Loading/async loading
* Refresh
* Weight functions
* Listeners
* get rid of map->CacheSettings
* Documentation
