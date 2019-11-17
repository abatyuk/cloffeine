(ns cloffeine.core
  (:import [com.github.benmanes.caffeine.cache Cache Caffeine CacheLoader LoadingCache]
           [java.util.function Function]
           [java.util.concurrent TimeUnit]))

(defrecord CacheSettings
  [^Long expireAfterWrite
   ^Long expireAfterAccess
   ^Integer maximumSize
   ^Boolean weakKeys
   ^Boolean weakValues
   ^Boolean softValues
   timeUnit])

(defn time-unit
  [name] 
  (case name
    :ms TimeUnit/MILLISECONDS
    :us TimeUnit/MICROSECONDS
    :s TimeUnit/SECONDS
    :m TimeUnit/MINUTES
    :h TimeUnit/HOURS
    :d TimeUnit/DAYS))

(defn ifn-to-loader [ifn]
  (reify CacheLoader
    (load [_this key]
      (do (println key)
          (ifn key)))))

(defn ifn-to-function [ifn] 
  (reify Function
    (apply [_this t] (ifn t))))

(defn make-builder
  [^CacheSettings settings]
  (let [builder (Caffeine/newBuilder)
        mergedSettings (merge settings {:timeUnit :s})
        timeUnit (time-unit (:timeUnit mergedSettings))
        expire-after-write (:expireAfterWrite mergedSettings)] 
    (when expire-after-write
      (.expireAfterWrite builder expire-after-write timeUnit))
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
  [^Cache cache key value]
  (.put cache key value))

(defn get-if-present [^Cache cache key] (.getIfPresent cache key))

(defn get
  ([^LoadingCache cache key] (.get cache key))
  ([^Cache cache key ifn] (.get cache key (ifn-to-function ifn))))

(defn invalidate
  [^Cache cache key]
  (.invalidate cache key))
