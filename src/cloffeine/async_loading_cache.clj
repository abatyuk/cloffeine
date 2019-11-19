(ns cloffeine.async-loading-cache
  (:refer-clojure :exclude [get])
  (:require [cloffeine.common :as common]
            [cloffeine.async-cache :as acache]
            [cloffeine.loading-cache :as loading-cache])
  (:import [com.github.benmanes.caffeine.cache AsyncCache AsyncLoadingCache CacheLoader AsyncCacheLoader]))

(defn make-cache
  (^AsyncLoadingCache [^CacheLoader cl]
   (make-cache cl {}))
  (^AsyncLoadingCache [^CacheLoader cl settings]
   (let [bldr (common/make-builder settings)]
     (.buildAsync bldr cl))))

(defn make-cache-async-loader
  (^AsyncLoadingCache [^AsyncCacheLoader cl]
   (make-cache cl {}))
  (^AsyncLoadingCache [^AsyncCacheLoader cl settings]
   (let [bldr (common/make-builder settings)]
     (.buildAsync bldr cl))))

(defn get
  ([^AsyncLoadingCache alcache k]
   (.get alcache k))
  ([^AsyncCache alcache k f]
   (acache/get alcache k f)))

(def get-if-present acache/get-if-present)
(def put! acache/put!)

(defn ->LoadingCache [^AsyncLoadingCache alcache]
  (.synchronous alcache))

(defn invalidate! [^AsyncLoadingCache alcache k]
  (-> alcache
      ->LoadingCache
      (loading-cache/invalidate! k)))
