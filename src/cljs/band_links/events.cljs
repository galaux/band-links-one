(ns band-links.events
  (:require
   [re-frame.core :as re-frame]
   [band-links.db :as app-db]
   [ajax.core :as ajax]
   [day8.re-frame.http-fx]))
   

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   app-db/default-db))

(re-frame/reg-event-db
 ::set-field-value
 (fn [db [_ field-id value]]
   (assoc db field-id value)))
   

(defn ->loading
  [db]
  (-> db
      (assoc :loading? true)
      (assoc :data nil)
      (assoc :error nil)))

(defn ->data
  [db data]
  (-> db
      (assoc :loading? false)
      (assoc :data data)
      (assoc :error nil)))

(defn ->error
  [db error]
  (-> db
      (assoc :loading? false)
      (assoc :data nil)
      (assoc :error error)))

(re-frame/reg-event-fx
 ::button-clicked
 (fn [{:keys [db]} _]
   (let [artist-name (:artist-name db)]
     {:http-xhrio {:method          :post
                   :uri             "/api/artist-network"
                   :params          {:artist-name artist-name}
                   :format          (ajax/url-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::process-response]
                   :on-failure      [::bad-response]}
      :db (->loading db)})))

(re-frame/reg-event-db
 ::process-response
 (fn [db [_ response]]
   (->data db (js->clj response))))

(re-frame/reg-event-db
 ::bad-response
 (fn [db [_ response]]
   (->error db (js->clj response))))
