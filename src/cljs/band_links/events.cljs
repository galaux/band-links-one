(ns band-links.events
  (:require
   [re-frame.core :as re-frame]
   [band-links.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
