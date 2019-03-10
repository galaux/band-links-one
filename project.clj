(defproject band-links "0.1.0-SNAPSHOT"

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"}}

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"
                  :exclusions [com.google.errorprone/error_prone_annotations
                               com.google.code.findbugs/jsr305
                               com.google.guava/guava]]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"
                  :exclusions [args4j
                               com.google.code.findbugs/jsr305
                               com.google.guava/guava]]

                 [com.datomic/datomic-pro "0.9.5786"
                  :exclusions [org.slf4j/slf4j-nop]]
                 ;; Datomic declares dependency on an old guava
                 ;; so forcing it to a new one
                 [com.google.guava/guava "23.0"]
                 ]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [figwheel-sidecar "0.5.18"
                    :exclusions [args4j
                                 com.google.code.findbugs/jsr305
                                 com.google.guava/guava]]
                   [cider/piggieback "0.4.0"]]

    :plugins      [[lein-figwheel "0.5.16"]]}
   :prod { }
   }

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "band-links.core/mount-root"}
     :compiler     {:main                 band-links.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            band-links.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}


    ]}
  )
