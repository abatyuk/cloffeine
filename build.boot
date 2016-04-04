(set-env! :dependencies
          '[[org.clojure/clojure "1.8.0"]
            [boot/core "2.5.5" :scope "provided"]
            [adzerk/boot-test "1.1.0" :scope "test"]
            [org.clojure/test.check "0.9.0" :scope "test"]
            [tolitius/boot-check "0.1.1" :scope "test"]
            [com.github.ben-manes.caffeine/caffeine "2.2.3"]]


          :source-paths #{"src/main/clj" "src/main/cljc"}
          :resource-paths #{"src/test/clj" "src/main/cljs" "src/test/cljs" "src/test/cljc" "src/main/resources"})

(require '[adzerk.boot-test :refer [test]])
(require '[tolitius.boot-check :as check])

(task-options!
 pom {:project 'cloffeine
      :version "0.0.1"}
 jar {:main 'cloffeine.runner
      :file "cloffeine.jar"}
 aot {:all true}
 test {:junit-output-to "junit"})

(deftask check-sources []
  (comp
   (check/with-bikeshed)
   (check/with-eastwood)
   (check/with-yagni)
   (check/with-kibit)))

(deftask build
  []
  (comp (aot) (uber) (jar) (target)))
