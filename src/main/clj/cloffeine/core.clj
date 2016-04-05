(ns cloffeine.core
  (:import (com.github.benmanes.caffeine.cache Caffeine CacheLoader)
           (java.util.function Function)))


(defrecord CacheSettings
  [^Long expireAfterWrite
   ^Long expireAfterAccess
   ^Integer maximumSize
   ^Boolean weakKeys
   ^Boolean weakValues
   ^Boolean softValues
   timeUnit])

(defn time-unit
  [name] (case name
                   :ms java.util.concurrent.TimeUnit/MILLISECONDS
                   :us java.util.concurrent.TimeUnit/MICROSECONDS
                   :s java.util.concurrent.TimeUnit/SECONDS
                   :m java.util.concurrent.TimeUnit/MINUTES
                   :h java.util.concurrent.TimeUnit/HOURS
                   :d java.util.concurrent.TimeUnit/DAYS))

(defn ifn-to-loader
  [ifn]
  (reify com.github.benmanes.caffeine.cache.CacheLoader
    (load [this key]
      (do (println key)
        (ifn key)))))

(defn ifn-to-function
  [ifn] (reify java.util.function.Function
          (apply [this t] (ifn t))))

(defn make-builder
  [^CacheSettings settings]
  (let [builder (Caffeine/newBuilder)
        mergedSettings (merge settings {:timeUnit :s})
        timeUnit (time-unit (:timeUnit mergedSettings))]
    (when-let
      [delay (:expireAfterWrite mergedSettings)]
      (.expireAfterWrite builder delay timeUnit))
    (when-let
      [delay (:expireAfterAccess mergedSettings)]
      (.expireAfterAccess builder delay timeUnit))
    (when-let
      [weakKeys (:weakKeys mergedSettings)]
      (.weakKeys builder))
    (when-let
      [weakValues (:weakValues mergedSettings)]
      (.weakValues builder))
    (when-let
      [softValues (:softValues mergedSettings)]
      (.softValues builder))
    (when-let
      [size (:maximumSize mergedSettings)]
      (.maximumSize builder (int size)))))

(defn make-cache
  ([^CacheSettings settings]
   (when-let [builder (make-builder settings)]
     (.build builder)))
  ([^CacheSettings settings loader]
   (when-let [builder (make-builder settings)]
     (.build builder (ifn-to-function loader)))))


(defn put
  [^com.github.benmanes.caffeine.cache.Cache cache key value]
  (.put cache key value))

(defn get-if-present [^com.github.benmanes.caffeine.cache.Cache cache key] (.getIfPresent cache key))

(defn get
  ([^com.github.benmanes.caffeine.cache.LoadingCache cache key] (.get cache key))
  ([^com.github.benmanes.caffeine.cache.Cache cache key ifn] (.get cache key (ifn-to-function ifn))))

(defn invalidate
  [^com.github.benmanes.caffeine.cache.Cache cache key]
  (.invalidate cache key))
