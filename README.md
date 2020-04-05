# Cloffeine 

Simple clojure wrapper around https://github.com/ben-manes/caffeine cache.

[![Clojars Project](https://img.shields.io/clojars/v/com.appsflyer/cloffeine.svg)](https://clojars.org/com.appsflyer/cloffeine)

[![Coverage Status](https://coveralls.io/repos/github/AppsFlyer/cloffeine/badge.svg?branch=master)](https://coveralls.io/github/AppsFlyer/cloffeine?branch=master)

add `[com.appsflyer/cloffeine "0.1.2"]` under `:dependencies`

Usage:
------
Manual loading:

```clojure
(require '[cloffeine.cache :as cache])
(require '[clojure.test :refer [is]])

(def cache (cache/make-cache))
(cache/put! cache :key :v)
(is (= :v (cache/get cache :key name)))
(cache/invalidate! cache :key)
(is (= "key" (cache/get cache :key name)))
```

Automatic loading

```clojure
(require '[cloffeine.loading-cache :as loading-cache])
(require '[cloffeine.common :as common])

(def loads (atom 0))
(def cl (common/reify-cache-loader (fn [k]
                                      (swap! loads inc)
                                      (name k))))
(def lcache (loading-cache/make-cache cl))
(loading-cache/put! lcache :key :v)
(is (= :v (loading-cache/get lcache :key)))
(is (= 0 @loads))
(loading-cache/invalidate! lcache :key)
(is (= "key" (loading-cache/get lcache :key)))
(is (= 1 @loads))
(is (= "key" (loading-cache/get lcache :key name)))
(is (= 1 @loads))
(is (= "key" (cache/get lcache :key name)))
(is (= 1 @loads))
(cache/invalidate! lcache :key)
(is (= "key" (cache/get lcache :key name)))
(is (= 1 @loads))

```

Async:

```clojure
(require '[cloffeine.async-cache :as async-cache])
(require '[promesa.core :as p])

(def acache (async-cache/make-cache))
(async-cache/put! acache :key (p/resolved :v))
(is (= :v @(async-cache/get acache :key name)))
(async-cache/invalidate! acache :key)
(is (= "key" @(async-cache/get acache :key name)))

```

Async with automatic loading:

```clojure
(require '[cloffeine.async-loading-cache :as async-loading-cache])

(def alcache (async-loading-cache/make-cache (common/reify-cache-loader name)))
(async-loading-cache/put! alcache :key (p/resolved :v))
(is (= :v @(async-loading-cache/get alcache :key name)))
(async-loading-cache/invalidate! alcache :key)
(is (= "key" @(async-loading-cache/get alcache :key name)))
```

Todo:
-----
* Weight functions
* Listeners
