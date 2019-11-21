(ns cloffeine.benchmark
  (:require [cloffeine.cache :as cache]
            [citius.core :as cit]))

(defn benchmark-miss-hit-same-key []
  (let [cloffeine-cache (cache/make-cache)
        atom-cache (atom {})
        atom-get (fn [c k] (if-let [v (get @c k)]
                             v
                             (swap! c assoc k :value)))
        cloffeine-get (fn [c k] (cache/get c k (fn [_] :value)))]
    (cit/with-bench-context ["atom cache" "cloffeine manual"]
      {:chart-filename (format "bench-cache-single-key-clj-%s.png" cit/clojure-version-str)
       :concurrency [8 8]}
      (cit/compare-perf
        "cache bench"
        (do
          (atom-get atom-cache 1)
          (atom-get atom-cache 1)
          (cache/invalidate! cloffeine-cache 1))
        (do
          (cloffeine-get cloffeine-cache 1)
          (cloffeine-get cloffeine-cache 1)
          (swap! atom-cache dissoc 1))))))

(defn benchmark-miss-hit-different-keys []
  (let [cloffeine-cache (cache/make-cache)
        atom-cache (atom {})
        atom-get (fn [c k] (if-let [v (get @c k)]
                             v
                             (swap! c assoc k :value)))
        cloffeine-get (fn [c k] (cache/get c k (fn [_] :value)))]
    (cit/with-bench-context ["atom cache" "cloffeine manual"]
      {:chart-filename (format "bench-cache-many-keys-clj-%s.png" cit/clojure-version-str)
       :concurrency [8 8]}
      (cit/compare-perf
        "cache bench"
        (dotimes [_ 100]
          (swap! atom-cache dissoc (rand-int 50))
          (atom-get atom-cache (rand-int 50))
          (atom-get atom-cache (rand-int 50)))
        (dotimes [_ 100]
          (cache/invalidate! cloffeine-cache (rand-int 50))
          (cloffeine-get cloffeine-cache (rand-int 50))
          (cloffeine-get cloffeine-cache (rand-int 50)))))))

