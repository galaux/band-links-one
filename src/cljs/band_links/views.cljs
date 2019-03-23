(ns band-links.views
  (:require [band-links.events :as events]
            [band-links.subs :as subs]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [cljsjs.d3 :as d3]
            [clojure.string :as s]))

(defn ->node
  [id text color]
  {:index id
   :text text :color color})

(defn ->link
  [artist-node band-node]
  {:source (:index artist-node)
   :target (:index band-node)})

(defn network-data->viz-data
  [network-data]
  (let [artist-node (->node 0 (:artist/name network-data) "#FF0000")
        [band-nodes links] (->> (:artist/bands network-data)
                                (map-indexed vector)
                                (map (fn [[id band]]
                                       (let [band-node (->node (inc id) (:band/name band) "#00FF00")]
                                         [band-node (->link artist-node band-node)])))
                                (apply map vector))]
    {:nodes (cons artist-node band-nodes)
     :links links}))

(defn network-viz-inner-comp
  [viz-data]
  (letfn [(draw-simul [simul]
            (.. js/d3
                (selectAll "g")
                remove)
            (.. js/d3
                (selectAll "line")
                remove)
            (.. js/d3
                (select "svg")
                (selectAll "line")
                (data (.links (.force simul "link")))
                enter
                (append "line")
                (attr "stroke" "#FF00FF")
                (attr "x1" #(-> % .-source .-x))
                (attr "y1" #(-> % .-source .-y))
                (attr "x2" #(-> % .-target .-x))
                (attr "y2" #(-> % .-target .-y)))
            (let [point-enter (.. js/d3
                                  (select "svg")
                                  (selectAll "circle")
                                  (data (.nodes simul))
                                  enter
                                  (append "g")
                                  (attr "transform" #(str "translate(" (.-x %) " " (.-y %) ")")))
                  drag-fn (fn [s]
                            (.. js/d3
                                drag
                                (on "start" (fn [d]
                                              (.. s
                                                  (alphaTarget 0.3)
                                                  restart)
                                              (set! (.-fx d) (.-x d))
                                              (set! (.-fy d) (.-y d))))
                                (on "drag" (fn [d]
                                             (let [event (.. js/d3 -event)]
                                               (set! (.-fx d) (.-x event))
                                               (set! (.-fy d) (.-y event)))))
                                (on "end" (fn [d]
                                              (set! (.-fx d) nil)
                                              (set! (.-fy d) nil)))))]
              (.. point-enter
                  (append "svg:circle")
                  (attr "r" 5)
                  (attr "fill" #(.-color %))
                  (call (drag-fn simul)))
              (.. point-enter
                  (append "text")
                  (attr "x" 10)
                  (attr "y" -10)
                  (text #(.-text %)))))
          (refresh-viz [simul]
            (.. js/d3
                (selectAll "line")
                (data (.. simul (force "link") links))
                (attr "x1" #(-> % .-source .-x))
                (attr "y1" #(-> % .-source .-y))
                (attr "x2" #(-> % .-target .-x))
                (attr "y2" #(-> % .-target .-y)))
            (.. js/d3
                (selectAll "g")
                (data (.nodes simul))
                (attr "transform" #(str "translate(" (.-x %) " " (.-y %) ")"))))
          (gen-simul [{:keys [nodes links]}]
            (.. js/d3
                (forceSimulation (clj->js nodes))
                (force "link" (.. js/d3
                                  (forceLink (clj->js links))
                                  (id #(.-index %))))
                (force "charge" (.forceManyBody js/d3))
                (force "center" (.forceCenter js/d3 200 200))))
          (init-simul [viz-data]
            (let [simul (gen-simul viz-data)]
              (.on simul "tick" #(refresh-viz simul))
              (draw-simul simul)))]

    (reagent/create-class
     {:reagent-render
      (fn []
        [:svg {:height 400 :width 400}])

      :component-did-mount
      (fn [this]
        (init-simul viz-data))

      :component-did-update
      (fn [this]
        (let [[_ viz-data] (reagent/argv this)]
          (init-simul viz-data)))})))

(defn network-viz-outer-comp
  [network-data]
  [network-viz-inner-comp (network-data->viz-data network-data)])

(defn artist-network-comp
  []
  (let [data (re-frame/subscribe [::subs/result])]
    [:div
     [:input
      {:default-value @(re-frame/subscribe [::subs/artist-name])
       :on-change #(re-frame/dispatch [::events/set-field-value
                                       :artist-name
                                       (-> % .-target .-value)])}]
     [:button
      {:on-click #(re-frame/dispatch [::events/button-clicked])}
      "Click me"]
     (when (not (empty? @data))
       [network-viz-outer-comp @data])]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "Hello from " @name]
     [artist-network-comp]]))
