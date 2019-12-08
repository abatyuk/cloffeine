(ns cloffeine.core-test 
  (:require [cloffeine.common :as common]
            [cloffeine.cache :as cache]
            [cloffeine.async-cache :as async-cache]
            [cloffeine.loading-cache :as loading-cache]
            [cloffeine.async-loading-cache :as async-loading-cache]
            [clojure.test :refer [deftest is testing]]
            [promesa.core :as p])
  (:import [com.google.common.testing FakeTicker]))

(deftest manual
  (let [cache (cache/make-cache)]
    (is (= 0 (.estimatedSize cache)))
    (cache/put! cache :key :v)
    (is (= 1 (.estimatedSize cache)))
    (is (= :v (cache/get cache :key name)))
    (cache/invalidate! cache :key)
    (is (= "key" (cache/get cache :key name)))))

(deftest loading
  (let [loads (atom 0)
        cl (common/reify-cache-loader (fn [k]
                                        (swap! loads inc)
                                        (name k)))
        lcache (loading-cache/make-cache cl {:recordStats true})]
    (loading-cache/put! lcache :key :v)
    (is (= :v (loading-cache/get lcache :key)))
    (= 1 (:hitCount (common/stats lcache)))
    (= 1 (:requestCount (common/stats lcache)))
    (is (= 0 @loads))
    (loading-cache/invalidate! lcache :key)
    (is (= "key" (loading-cache/get lcache :key)))
    (= 2 (:requestCount (common/stats lcache)))
    (= 1 (:loadCount (common/stats lcache)))
    (= 0.5 (:hitRate (common/stats lcache)))
    (= 0.5 (:missRate (common/stats lcache)))
    (is (= 1 @loads))
    (is (= "key" (loading-cache/get lcache :key name)))
    (is (= 1 @loads))
    (is (= "key" (cache/get lcache :key name)))
    (is (= 1 @loads))
    (cache/invalidate! lcache :key)
    (is (= "key" (cache/get lcache :key name)))
    (is (= 1 @loads))))

(deftest get-if-present
  (let [cache (cache/make-cache)]
    (cache/put! cache :key "v")
    (is (= "v" (cache/get-if-present cache :key)))
    (is (nil? (cache/get-if-present cache :non-existent))))
  (let [loading-cache (loading-cache/make-cache (common/reify-cache-loader str))]
    (loading-cache/put! loading-cache :key "v")
    (is (= (loading-cache/get-if-present loading-cache :key) "v"))
    (loading-cache/invalidate! loading-cache :key)
    (is (nil? (loading-cache/get-if-present loading-cache :key)))))

(deftest refresh-test
  (let [loads (atom 0)
        reloads (atom 0)
        cl (common/reify-cache-loader 
             (fn [k]
                 (swap! loads inc)
                 (name k))
             (fn [k _v]
                 (swap! reloads inc)
                 (name k)))
        lcache (loading-cache/make-cache cl)]
    (is (= "key" (loading-cache/get lcache :key)))
    (is (= 1 @loads))
    (loading-cache/refresh lcache :key)
    (Thread/sleep 10)
    (is (= 1 @reloads))
    (is (= 1 @loads))))

(deftest get-async
  (let [acache (async-cache/make-cache)]
    (async-cache/put! acache :key (p/resolved :v))
    (is (= :v @(async-cache/get acache :key name)))
    (async-cache/invalidate! acache :key)
    (is (= "key" @(async-cache/get acache :key name))))
  (let [alcache (async-loading-cache/make-cache (common/reify-cache-loader name))]
    (async-cache/put! alcache :key (p/resolved :v))
    (is (= :v @(async-loading-cache/get alcache :key name)))
    (async-cache/invalidate! alcache :key)
    (is (= "key" @(async-loading-cache/get alcache :key name)))))

(deftest get-if-present-async
  (let [acache (async-cache/make-cache)]
    (is (nil? (async-cache/get-if-present acache :key)))
    (async-cache/put! acache :key (p/resolved :v))
    (is (= :v @(async-cache/get-if-present acache :key))))
  (let [loads (atom 0)
        cl (common/reify-cache-loader (fn [k]
                                          (swap! loads inc)
                                          (name k)))
        alcache (async-loading-cache/make-cache cl)]
    (is (nil? (async-loading-cache/get-if-present alcache :key)))
    (async-loading-cache/put! alcache :key (p/resolved :v))
    (is (= :v @(async-loading-cache/get-if-present alcache :key)))
    (is (= 0 @loads))))

(deftest get-async-loading
  (let [loads (atom 0)
        cl (common/reify-cache-loader (fn [k]
                                        (swap! loads inc)
                                        (name k)))
        alcache (async-loading-cache/make-cache cl)]
    (is (nil? (async-loading-cache/get-if-present alcache :key)))
    (is (= 0 @loads))
    (is (= "key" @(async-loading-cache/get alcache :key)))
    (is (= 1 @loads))
    (async-loading-cache/invalidate! alcache :key)
    (is (= "key" @(async-loading-cache/get alcache :key)))
    (is (= 2 @loads))
    (let [alcache2 (async-loading-cache/->LoadingCache alcache)]
      (is (= "key" (loading-cache/get alcache2 :key)))
      (is (= 2 @loads))
      (is (= "key2" (loading-cache/get alcache2 :key2)))
      (is (= 3 @loads)))))

(deftest async-auto-refresh
  (let [loads (atom 0)
        reloads (atom 0)
        load-fails? (atom false)
        reload-fails? (atom false)
        cl (common/reify-cache-loader
             (fn [k]
               (if @load-fails?
                 (throw (Exception. (format "load failed for key: %s" k)))
                 (do
                   (swap! loads inc)
                   (name k))))
             (fn [k old-v]
               (if @reload-fails?
                 old-v
                 (do
                   (swap! reloads inc)
                   (name k)))))
        alcache (async-loading-cache/make-cache cl {:refreshAfterWrite 1 :timeUnit :s})]
    (testing "no key, load succeeds"
      (is (= "key" @(async-loading-cache/get alcache :key)))
      (is (= 1 @loads))
      (is (= 0 @reloads)))
    (testing "key exists, just return from cache"
      (is (= "key" @(async-loading-cache/get alcache :key)))
      (is (= 1 @loads))
      (is (= 0 @reloads)))
    (testing "key needs to be refreshed, but reload fails"
      (reset! reload-fails? true)
      (Thread/sleep 1100)
      (is (= "key" @(async-loading-cache/get alcache :key)))
      (is (= 1 @loads))
      (is (= 0 @reloads)))
    (testing "reloads succeeds, time to refresh again"
      (reset! reload-fails? false)
      (Thread/sleep 1000)
      (is (= "key" @(async-loading-cache/get alcache :key)))
      (is (= 1 @reloads)))))

(deftest async-auto-async-refresh
  (let [loads (atom 0)
        reloads (atom 0)
        load-fails? (atom false)
        reload-fails? (atom false)
        acl (common/reify-async-cache-loader
              (fn [k _executor]
                (if @load-fails?
                  (p/rejected (Exception. (format "load failed for key: %s" k)))
                  (do
                    (swap! loads inc)
                    (p/resolved (name k)))))
              (fn [k old-v _executor]
                (if @reload-fails?
                  (p/resolved old-v)
                  (do
                    (swap! reloads inc)
                    (p/resolved (name k))))))
        alcache (async-loading-cache/make-cache-async-loader acl {:refreshAfterWrite 1 :timeUnit :s})]
    (testing "no key, load succeeds"
      (is (= "key" @(async-loading-cache/get alcache :key)))
      (is (= 1 @loads))
      (is (= 0 @reloads)))
    (testing "key exists, just return from cache"
      (is (= "key" @(async-loading-cache/get alcache :key)))
      (is (= 1 @loads))
      (is (= 0 @reloads)))
    (testing "key needs to be refreshed, but reload fails"
      (reset! reload-fails? true)
      (Thread/sleep 1100)
      (is (= "key" @(async-loading-cache/get alcache :key)))
      (is (= 1 @loads))
      (is (= 0 @reloads)))
    (testing "reloads succeeds, time to refresh again"
      (reset! reload-fails? false)
      (Thread/sleep 1000)
      (is (= "key" @(async-loading-cache/get alcache :key)))
      (is (= 1 @reloads)))))
  
