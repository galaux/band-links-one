(ns band-links.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::artist-name
 (fn [db]
   (:artist-name db)))

(re-frame/reg-sub
 ::result
 (fn [db]
   (:data db)))

(re-frame/reg-sub
 ::error
 (fn [db]
   (str (:error db))))


(re-frame/reg-sub
 ::circles
 (fn [db]
   (:circles db)))
 
