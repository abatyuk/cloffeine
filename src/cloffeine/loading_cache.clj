(ns cloffeine.loading-cache
  (:refer-clojure :exclude [get])
  (:require [cloffeine.common :as common]
            [cloffeine.cache :as cache])
  (:import [com.github.benmanes.caffeine.cache Cache LoadingCache CacheLoader]))

(defn make-cache
  (^LoadingCache [^CacheLoader cl]
   (make-cache cl {}))
  (^LoadingCache [^CacheLoader cl settings]
   (let [bldr (common/make-builder settings)]
     (.build bldr cl))))

(def get-if-present cache/get-if-present)

(def invalidate! cache/invalidate!)

(def put! cache/put!)

(defn get
  ([^LoadingCache lcache k]
   (.get lcache k))
  ([^Cache lcache k loading-fn]
   (cache/get lcache k loading-fn)))

(defn cleanup [^Cache lcache]
  (cache/cleanup lcache))

(defn refresh [^LoadingCache lcache k]
  (.refresh lcache k))

(defn get-all [^LoadingCache lcache ks]
  (into {} (.getAll lcache ks)))
