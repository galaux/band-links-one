(ns band-links.views
  (:require
   [re-frame.core :as re-frame]
   [band-links.subs :as subs]
   [band-links.events :as events]))


(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "Hello from " @name]
     [:input
      {:default-value @(re-frame/subscribe [::subs/artist-name])
       :on-change #(re-frame/dispatch [::events/set-field-value
                                       :artist-name
                                       (-> % .-target .-value)])}]
     [:button
      {:on-click #(re-frame/dispatch [::events/button-clicked])}
      "Click me"]
     [:p
      @(re-frame/subscribe [::subs/result])]
     [:p
      {:style {:color "#FF0000"}}
      @(re-frame/subscribe [::subs/error])]]))

