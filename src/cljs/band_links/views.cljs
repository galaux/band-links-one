(ns band-links.views
  (:require
   [re-frame.core :as re-frame]
   [band-links.subs :as subs]
   ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "Hello from " @name]
     ]))
