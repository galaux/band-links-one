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
                 [cljs-ajax "0.8.0"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [cljsjs/vega "5.3.1-0"]
                 [cljsjs/vega-embed "4.0.0-rc1-0"]
                 [cljsjs/vega-lite "3.0.0-rc16-0"]
                 [cljsjs/vega-tooltip "0.16.0-0"]
                 [metasoarous/oz "1.5.6"]

                 [compojure "1.5.0"]
                 [yogthos/config "0.8"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]

                 [org.clojure/tools.logging "0.4.1"]
                 [org.slf4j/slf4j-api "1.7.26"]
                 ;; [org.slf4j/log4j-over-slf4j "1.7.26"]
                 [ch.qos.logback/logback-classic "1.2.3"]

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

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler band-links.handler/dev-handler}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :aliases {"import" ["run" "-m" "band-links.import/run"]}

  :profiles
  {:dev
   {:source-paths ["env/dev"]
    :dependencies [[binaryage/devtools "0.9.10"]
                   [figwheel-sidecar "0.5.18"
                    :exclusions [args4j
                                 com.google.code.findbugs/jsr305
                                 com.google.guava/guava]]
                   [cider/piggieback "0.4.0"]]

    :plugins      [[lein-figwheel "0.5.16"]]}
   :prod { }
   :uberjar {:source-paths ["env/prod/clj"]
             :omit-source  true
             :main         band-links.server
             :aot          [band-links.server]
             :uberjar-name "band-links.jar"
             :prep-tasks   ["compile" ["cljsbuild" "once" "min"]]}
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
     :jar true
     :compiler     {:main            band-links.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}


    ]}
  )
