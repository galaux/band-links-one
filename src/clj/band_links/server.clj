(ns band-links.server
  (:require [band-links.handler :refer [handler]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [band-links.db :as db]
            [band-links.import :refer [import-data]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))
        datomic-conn (db/conn-with-schema "datomic:mem://band-links")]    ;; TODO
    (import-data datomic-conn "./dev-resources/data") ;; TODO
    (run-jetty (handler datomic-conn) {:port port :join? false})))
