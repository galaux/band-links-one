(ns band-links.import
  (:require [band-links.db :as db]
            [clojure.tools.logging :as log]
            [datomic.api :as d]
            [clojure.java.io :as io]))


(def ^:const empty-field "\\N")

(def ^:const artist-field-id 0)
(def ^:const artist-field-name 2)
(def ^:const artist-field-type 10)

(def ^:const link-field-id 0)
(def ^:const link-field-type 1)

(def ^:const link-type-member-of "103")

(def ^:const l-artist-artist-field-link-id 1)
(def ^:const l-artist-artist-field-entity-a-id 2)
(def ^:const l-artist-artist-field-entity-b-id 3)

(defn clean-empty-field
  [v]
  (when (not= v empty-field)
    v))

(defn read-field
  [m k]
  (some-> (get m k) clean-empty-field))

(defn str->artist
  [str]
  (let [fields (clojure.string/split str #"\t")
        artist-id (read-field fields artist-field-id)
        artist-name (read-field fields artist-field-name)]
    [artist-id artist-name])) 

(defn str->link
  [str]
  (let [fields (clojure.string/split str #"\t")
        link-id (read-field fields link-field-id)
        link-type (read-field fields link-field-type)]
    [link-id link-type]))

(defn strs->links
  [links]
  (->> links
       (map str->link)
       (filter (fn [[_ link-type]]
                 (= link-type
                    link-type-member-of)))
       (map first)
       set))


(defn str->artist-artist-link
  [str]
  (let [fields (clojure.string/split str #"\t")
        link-id (read-field fields l-artist-artist-field-link-id)
        entity-a-id (read-field fields l-artist-artist-field-entity-a-id)
        entity-b-id (read-field fields l-artist-artist-field-entity-b-id)]
    [link-id entity-a-id entity-b-id]))

(defn artist-entity
  [artist-name band-name]
  {:artist/name artist-name
   :artist/bands [{:band/name band-name}]})

(defn artist-artist-link->map
  [artists artist-artist-links links]
  (let [artists (future (into {} (map str->artist artists)))
        artist-artist-links (future (map str->artist-artist-link artist-artist-links))
        member-of-links (future (strs->links links))]
    (->> @artist-artist-links
         (map (fn [[link-id entity-a-id entity-b-id]]
                (when (contains? @member-of-links link-id)
                  (artist-entity (get @artists entity-a-id)
                                 (get @artists entity-b-id)))))
         (remove nil?))))

(defn import-data
  [conn path]
  (log/info "Importing from" path)
  (with-open [artist-artist-link-r (io/reader (str path "/l_artist_artist"))
              artist-r (io/reader (str path "/artist"))
              link-r (io/reader (str path "/link"))]
    (->> (artist-artist-link->map (line-seq artist-r)
                                  (line-seq artist-artist-link-r)
                                  (line-seq link-r))
         (d/transact conn))))

(defn db-conn-with-schema
  [uri]
  (when (d/create-database uri)
    (log/info "Created schema for" uri)
    (d/transact (d/connect uri) db/schema))
  (d/connect uri))

(defn run
  [& [datomic-uri data-path]]
  (let [datomic-conn (db-conn-with-schema (or datomic-uri
                                              "datomic:mem://band-links-dev"))]
    (import-data datomic-conn (or data-path "./dev-resources/data"))
    (log/info "Imported" (count (db/all-artists (d/db datomic-conn))) "artists")))
