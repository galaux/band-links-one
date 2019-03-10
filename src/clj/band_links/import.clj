(ns band-links.import
  (:require [band-links.db :as db]
            [clojure.tools.logging :as log]
            [datomic.api :as d]))


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

(defn import-artists
  [path]
  (with-open [r (clojure.java.io/reader path)]
    (->> (line-seq r)
         (map str->artist)
         (into {}))))

(defn str->link
  [str]
  (let [fields (clojure.string/split str #"\t")
        link-id (read-field fields link-field-id)
        link-type (read-field fields link-field-type)]
    [link-id link-type]))

(defn import-member-of-links
  [path]
  (with-open [r (clojure.java.io/reader path)]
    (->> (line-seq r)
         (map str->link)
         (filter (fn [[_ link-type]]
                   (= link-type
                      link-type-member-of)))
         (map first)
         set)))


(defn str->artist-artist-link
  [str]
  (let [fields (clojure.string/split str #"\t")
        link-id (read-field fields l-artist-artist-field-link-id)
        entity-a-id (read-field fields l-artist-artist-field-entity-a-id)
        entity-b-id (read-field fields l-artist-artist-field-entity-b-id)]
    [link-id entity-a-id entity-b-id]))

(defn import-artist-artist-links
  [path]
  (with-open [r (clojure.java.io/reader path)]
    (let [artist-artist-links (->> (line-seq r)
                                   (map str->artist-artist-link))]
      (doall artist-artist-links))))

(defn artist-entity
  [artist-name band-name]
  {:artist/name artist-name
   :artist/bands [{:band/name band-name}]})

(defn artist-artist-link->map
  [path-artists path-artist-artist-links path-links]
  ;; TODO compute each of these 3 in its own separate thread
  (let [artist-artist-links (import-artist-artist-links path-artist-artist-links)
        member-of-links (import-member-of-links path-links)
        artists (import-artists path-artists)]
    (->> artist-artist-links
         (map (fn [[link-id entity-a-id entity-b-id]]
                (when (contains? member-of-links link-id)
                  (artist-entity (get artists entity-a-id)
                                 (get artists entity-b-id)))))
         (remove nil?))))

(defn import-data
  [conn path]
  (let [entities (artist-artist-link->map (str path "/artist")
                                          (str path "/l_artist_artist")
                                          (str path "/link"))]
    (d/transact conn entities)))

(defn run
  [datomic-conn dir-path]
  (log/info "Importing from" dir-path)
  (import-data datomic-conn dir-path)
  (let [artist-count (-> datomic-conn
                         d/db
                         db/all-artists
                         count)]
    (log/info "Imported" artist-count "artists")))
