(ns band-links.db-test
  (:require [band-links.db :refer :all]
            [clojure.test :refer :all]
            [datomic.api :as d]))

(def ^:dynamic *test-conn* nil)

(def initial-band-data
  [{:artist/name "Jeordie White"
    :artist/bands [{:band/name "Marilyn Manson"}
                   {:band/name "A Perfect Circle"}
                   {:band/name "Nine Inch Nails"}]}
   {:artist/name "Josh Freese"
    :artist/bands [{:band/name "Nine Inch Nails"}
                   {:band/name "A Perfect Circle"}]}
   {:artist/name "Maynard James Keenan"
    :artist/bands [{:band/name "Tool"}
                   {:band/name "Puscifer"}
                   {:band/name "A Perfect Circle"}]}
   {:artist/name "Trent Reznor"
    :artist/bands [{:band/name "Nine Inch Nails"}]}
   {:artist/name "John 5"
    :artist/bands [{:band/name "Marilyn Manson"}]}
   {:artist/name "Troy Van Leeuwen"
    :artist/bands [{:band/name "A Perfect Circle"}
                   {:band/name "Queens Of The Stone Age"}]}])


(defn test-fixture
  [f]
  (let [uri "datomic:mem://band-links-test-db"
        _ (d/create-database uri)
        conn (d/connect uri)]
    (d/transact conn schema)
    (d/transact conn initial-band-data)
    (binding [*test-conn* conn]
      (f)
      (d/release conn))))


(use-fixtures :each test-fixture)


(deftest db-test-band
  (testing "osef"
    (let [artists (all-artists (d/db *test-conn*))
          bands (all-bands (d/db *test-conn*))]
      (is (= 6 (count artists)))
      (is (= 6 (count bands))))))

(deftest test-bands-linked-to-bands
  (testing "test bands linked to bands"
    (let [db (d/db *test-conn*)]
      (is (= #{"Tool" "A Perfect Circle"}
             (->> (bands-linked-to-band db "Puscifer")
                  (map #(eid->band-name db %))
                  set)))
      (is (= #{"Tool"
               "Puscifer"
               "Marilyn Manson"
               "Nine Inch Nails"
               "Queens Of The Stone Age"}
             (->> (bands-linked-to-band db "A Perfect Circle")
                  (map #(eid->band-name db %))
                  set))))))

(deftest test-member-of
  (let [db (d/db *test-conn*)]
    (is (= #{"Nine Inch Nails" "Marilyn Manson" "A Perfect Circle"}
           (->> (member-of db "Jeordie White")
                (map #(eid->band-name db %))
                set)))))
