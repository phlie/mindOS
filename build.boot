;; A very simple boot-clj script
(set-env!
  :resource-paths #{"src/"}
  :asset-paths #{"assets"}
  :dependencies '[[org.clojure/clojure "RELEASE"]
                  ;; Needed for cljfx
                  [cljfx "1.6.0"]
                  [clj-time "0.15.2"]
                  [org.clojure/core.async "0.4.500"]])

;; The current task options
(task-options!
 pom {:project 'mindOS
      :version "0.0.1"})

;; The run task which simply loads the -main function
(deftask run
  "Runs my project"
  [a args ARG [str] "the arguments for the application."]
  (with-pass-thru _
    (require '[core.main :as app])
    (apply (resolve 'app/-main) args)))
