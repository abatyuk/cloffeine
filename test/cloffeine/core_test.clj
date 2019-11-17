(ns cloffeine.core-test 
  (:require [cloffeine.core :as cc]
            [clojure.test :refer [deftest is]]))

(deftest get-if-present-test 
  (let [settings (cc/map->CacheSettings {:maximumSize 100})
        cache (cc/make-cache settings)]
    (cc/put cache :key "value")
    (is (= (cc/get-if-present cache :key) "value"))
    (is (nil? (cc/get-if-present cache :non-existent)))))



