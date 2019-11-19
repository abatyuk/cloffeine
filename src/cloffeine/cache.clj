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

(defn get-if-present [^Cache cache k]
  (.getIfPresent cache k))

(defn invalidate! [^Cache cache k]
  (.invalidate cache k))

(defn put! [^Cache cache k v]
  (.put cache k v))


