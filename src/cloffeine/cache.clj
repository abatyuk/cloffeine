(ns cloffeine.cache
  (:refer-clojure :exclude [get])
  (:require [cloffeine.common :as common])
  (:import [com.github.benmanes.caffeine.cache Cache]))

(defn make-cache
  (^Cache []
   (make-cache {}))
  (^Cache [settings]
   (let [bldr (common/make-builder settings)]
     (.build bldr))))

(defn get [^Cache cache k loading-fn]
  (.get cache k (common/ifn->function loading-fn)))

(defn get-all [^Cache cache ks mapping-fn]
  (into {} (.getAll cache ks (common/ifn->function mapping-fn))))

(defn get-all-present [^Cache cache ks]
  (into {} (.getAllPresent cache ks)))

(defn get-if-present [^Cache cache k]
  (.getIfPresent cache k))

(defn invalidate! [^Cache cache k]
  (.invalidate cache k))

(defn invalidate-all! 
  ([^Cache cache]
   (.invalidateAll cache))
  ([^Cache cache ks]
   (.invalidateAll cache ks)))

(defn put! [^Cache cache k v]
  (.put cache k v))

(defn cleanup [^Cache cache]
  (.cleanUp cache))

(defn estimated-size [^Cache cache]
  (.estimatedSize cache))

(defn policy [^Cache cache]
  (.policy cache))
