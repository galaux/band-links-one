(ns band-links.handler
  (:require [band-links.db :as db]
            [band-links.import :refer [import-data]]
            [compojure.core :refer [context defroutes GET POST routes]]
            [compojure.route :refer [resources]]
            [datomic.api :as d]
            [ring.middleware.defaults
             :refer
             [api-defaults site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :refer [content-type resource-response response]]))

(defn api-routes
  [datomic-conn]
  (routes
   (POST "/artist-network" [artist-name]
         (-> (d/db datomic-conn)
             (db/band-network artist-name)
             response))))


(defroutes site-routes
  (GET "/" [] (-> (resource-response "index.html" {:root "public"})
                  ;; Content-Type should later be set by `wrap-defaults … site-defaults`
                  ;; but it is based on the request's :uri which does not end with "html"
                  ;; as we are serving it as "/" so setting it manually here
                  (content-type (ring.util.mime-type/default-mime-types "html"))))
  (resources "/"))

(defn handler
  [datomic-conn]
  (routes (context "/api" []
                   (-> (api-routes datomic-conn)
                       (wrap-json-response {:pretty true})
                       (wrap-defaults api-defaults)
                       wrap-reload))
          (-> site-routes
              (wrap-defaults
               (-> site-defaults
                   (assoc :cookies false)
                   (assoc-in [:session :flash] false)
                   (assoc-in [:session :cookie-attrs] {})))
              wrap-reload)))

(def datomic-conn-dev-with-data
  (let [conn (db/conn-with-schema "datomic:mem://band-links-dev")]
    (import-data conn "./dev-resources/data")
    conn))

(def dev-handler
  (handler datomic-conn-dev-with-data))
