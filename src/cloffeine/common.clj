(ns cloffeine.common
  (:import [com.github.benmanes.caffeine.cache Caffeine CacheLoader AsyncCacheLoader]
           [java.util.function Function]
           [java.util.concurrent TimeUnit]))

(defn time-unit
  [tu] 
  (case tu
    :ms TimeUnit/MILLISECONDS
    :us TimeUnit/MICROSECONDS
    :s TimeUnit/SECONDS
    :m TimeUnit/MINUTES
    :h TimeUnit/HOURS
    :d TimeUnit/DAYS))

(defn make-builder ^Caffeine [settings]
  (let [bldr (Caffeine/newBuilder)
        settings (merge {:timeUnit :s} settings)
        timeUnit (time-unit (:timeUnit settings))]
    (cond-> bldr
      (:maximumSize settings) (.maximumSize (int (:maximumSize settings)))
      (:expireAfterAccess settings) (.expireAfterAccess (:expireAfterAccess settings) timeUnit)
      (:expireAfterWrite settings) (.expireAfterWrite (:expireAfterWrite settings) timeUnit)
      (:refreshAfterWrite settings) (.refreshAfterWrite (:refreshAfterWrite settings) timeUnit)
      (:weakKeys settings) (.weakKeys)
      (:weakValues settings) (.weakValues)
      (:initialCapacity settings) (.initialCapacity (int (:initialCapacity settings)))
      (:softValues settings) (.softValues))))

(defn reify-async-cache-loader 
  ([loading-fn]
   (reify AsyncCacheLoader
     (asyncLoad [_this k executor]
       (loading-fn k executor))))
  ([loading-fn reloading-fn]
   (reify AsyncCacheLoader
     (asyncLoad [_this k executor]
       (loading-fn k executor))
     (asyncReload [_this k v executor]
       (reloading-fn k v executor)))))

(defn reify-cache-loader 
  ([loading-fn]
   (reify CacheLoader
     (load [_this k]
       (loading-fn k))))
  ([loading-fn reloading-fn]
   (reify CacheLoader
     (load [_this k]
       (loading-fn k))
     (reload [_this k v]
       (reloading-fn k v)))))

(defn ifn->function ^Function [ifn]
  (reify Function
    (apply [_this t]
      (ifn t))))

