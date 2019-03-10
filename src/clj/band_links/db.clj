(ns band-links.db
  (:require [datomic.api :as d]))


(def schema
  [{:db/ident :band/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The name of the band"}

   {:db/ident :artist/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity
    :db/doc "The name of the artist"}
   {:db/ident :artist/bands
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "An artist's ref to a band"}])

(def datomic-rules
  '[[(band-name->eid ?band-name ?eid)
     [?eid :band/name ?band-name]]
    [(artist-name->eid ?artist-name ?eid)
     [?eid :artist/name ?artist-name]]
    [(member-of ?artist-eid ?band-eid)
     [?artist-eid :artist/bands ?band-eid]]
    [(band-linked-to-band-by-artist ?b1 ?b2)
     (member-of ?a ?b1)
     (member-of ?a ?b2)
     [(!= ?b1 ?b2)]]])

(defn all-artists
  [db]
  (d/q '[:find [?artist-name ...]
         :where [?e1 :artist/name ?artist-name]]
       db))

(defn all-bands
  [db]
  (d/q '[:find [?band-name ...]
         :where [_ :band/name ?band-name]]
       db))

(defn member-of
  [db artist-name]
  (let [query '[:find [?b ...]
                :in $ % ?artist-name
                :where
                [?a :artist/name ?artist-name]
                (member-of ?a ?b)]]
    (set (d/q query db datomic-rules artist-name))))
  

(defn bands-linked-to-band
  [db band-name]
  (let [query '[:find [?new-band-eid ...]
                :in $ % ?band-name
                :where
                [?band-eid :band/name ?band-name]
                (band-linked-to-band-by-artist ?band-eid ?new-band-eid)]]
    (set (d/q query db datomic-rules band-name))))

(defn eid->band-name
  [db eid]
  (-> (d/entity db eid)
      :band/name))

(defn band-name->eid
  [db band-name]
  (let [query '[:find ?eid .
                :in $ % ?band-name
                :where
                (band-name->eid ?band-name ?eid)]]
    (d/q query db datomic-rules band-name)))

(defn artist-name->eid
  [db artist-name]
  (let [query '[:find ?eid .
                :in $ % ?artist-name
                :where
                (artist-name->eid ?artist-name ?eid)]]
    (d/q query db datomic-rules artist-name)))

(defn band-network
  [db name]
  (->> (artist-name->eid db name)
       (datomic.api/entity db)))

