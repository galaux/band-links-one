(ns band-links.import-test
  (:require [clojure.test :refer :all]
            [band-links.import :refer :all]
            [clojure.java.io :as io])
  (:import java.io.StringReader
           java.io.BufferedReader))


(deftest test-str->entities
  (let [s "135032\t128480\t236978\t909\t0\t2017-07-09\t23:04:15.277643+00\t0\t"]
    (is (= ["128480" "236978" "909"]
           (str->artist-artist-link s))))
  (let [s "236978\ta1b61d0f-2333-44f5-afee-cc0c2eab71a4\tMaynard James Keenan\tKeenan, Maynard James\t1964\t4\t17\t\\N\t\\N\t\\N\t1\t222\t1\t0\t2016-12-27\t12:18:25.283755+00\tf\t21869\t\\N"]
    (is (= ["236978" "Maynard James Keenan"]
           (str->artist s))))
  (let [s "128480\t103\t1999\t\\N\t\\N\t\\N\t\\N\t\\N\t2\t2013-08-30 13:01:06.880796+00\tf"]
    (is (= ["128480" "103"]
           (str->link s)))))

(deftest test-import-data
  (let [artist-strs (line-seq (io/reader "./dev-resources/data/artist"))
        artist-artist-link-strs (line-seq (io/reader "./dev-resources/data/l_artist_artist"))
        link-strs (line-seq (io/reader "./dev-resources/data/link"))
        expected [{:artist/bands [{:band/name "A Perfect Circle"}],
                   :artist/name "Maynard James Keenan"}
                  {:artist/bands [{:band/name "Puscifer"}], :artist/name "Maynard James Keenan"}
                  {:artist/bands [{:band/name "Tool"}], :artist/name "Maynard James Keenan"}
                  {:artist/bands [{:band/name "A Perfect Circle"}],
                   :artist/name "Troy Van Leeuwen"}
                  {:artist/bands [{:band/name "Queens of the Stone Age"}],
                   :artist/name "Troy Van Leeuwen"}
                  {:artist/bands [{:band/name "Nine Inch Nails"}], :artist/name "Josh Freese"}
                  {:artist/bands [{:band/name "A Perfect Circle"}], :artist/name "Josh Freese"}
                  {:artist/bands [{:band/name "Nine Inch Nails"}], :artist/name "Trent Reznor"}
                  {:artist/bands [{:band/name "Nine Inch Nails"}], :artist/name "Jeordie White"}
                  {:artist/bands [{:band/name "Marilyn Manson"}], :artist/name "Jeordie White"}
                  {:artist/bands [{:band/name "Marilyn Manson"}], :artist/name "Jeordie White"}
                  {:artist/bands [{:band/name "A Perfect Circle"}], :artist/name "Jeordie White"}
                  {:artist/bands [{:band/name "Marilyn Manson"}], :artist/name "Marilyn Manson"}]]
    (is (= expected
           (artist-artist-link->map artist-strs artist-artist-link-strs link-strs)))))
