(ns user
  (:require [band-links.db :as db]
            [band-links.import :as import]
            [datomic.api :as d]))


(defn db-conn-with-data
  []
  (let [uri "datomic:mem://band-links-dev"
        _ (d/create-database uri)
        conn (d/connect uri)]
    (d/transact conn db/schema)
    (import/run conn "./dev-resources/data")
    conn))
