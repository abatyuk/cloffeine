(ns cloffeine.async-cache
  (:refer-clojure :exclude [get])
  (:require [cloffeine.common :as common]
            [cloffeine.cache :as cache])
  (:import [com.github.benmanes.caffeine.cache AsyncCache]))

(defn make-cache
  (^AsyncCache []
   (make-cache {}))
  (^AsyncCache [settings]
   (let [bldr (common/make-builder settings)]
     (.buildAsync bldr))))

(defn get [^AsyncCache acache k f]
    (.get acache k (common/ifn->function f)))

(defn get-if-present [^AsyncCache acache k]
    ;; TODO: acache can return nil. always wrap in future?
    (.getIfPresent acache k))

(defn put! [^AsyncCache acache k future-v]
    (.put acache k future-v))

(defn ->Cache [^AsyncCache acache]
    (.synchronous acache))

(defn invalidate! [^AsyncCache acache k]
  (-> acache
      ->Cache
      (cache/invalidate! k)))
