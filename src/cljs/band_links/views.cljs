(ns band-links.views
  (:require [band-links.events :as events]
            [band-links.subs :as subs]
            [oz.core :as oz]
            [re-frame.core :as re-frame]))

(def vega-force
  {:$schema "https://vega.github.io/schema/vega/v5.json"
   :autosize "none"
   :width 700
   :height 500
   :padding 0

   :signals
   [{:name "cx" :update "width / 2"}
    {:name "cy" :update "height / 2"}
    {:name "nodeRadius"
     :value 8}
    {:name "nodeCharge"
     :value -30}
    {:name "linkDistance"
     :value 30}
    {:name "static" :value false :bind {:input "checkbox"}}
    {:description "State variable for active node fix status."
     :name "fix"
     :value false
     :on
     [{:events "symbol:mouseout[!event.buttons], window:mouseup"
       :update "false"}
      {:events "symbol:mouseover" :update "fix || true"}
      {:events
       "[symbol:mousedown, window:mouseup] > window:mousemove!"
       :update "xy()"
       :force true}]}
    {:description "Graph node most recently interacted with."
     :name "node"
     :value nil
     :on
     [{:events "symbol:mouseover"
       :update "fix === true ? item() : node"}]}
    {:description
     "Flag to restart Force simulation upon data changes."
     :name "restart"
     :value false
     :on [{:events {:signal "fix"} :update "fix && fix.length"}]}]

   :data []

   :scales
   [{:name "color"
     :type "ordinal"
     :domain {:data "node-data" :field "group"}
     :range {:scheme "category20c"}}]

   :marks
   [{:name "nodes"
     :type "symbol"
     :zindex 1
     :from {:data "node-data"}
     :on
     [{:trigger "fix"
       :modify "node"
       :values
       "fix === true ? {fx: node.x, fy: node.y} : {fx: fix[0], fy: fix[1]}"}
      {:trigger "!fix"
       :modify "node"
       :values "{fx: null, fy: null}"}]
     :encode
     {:enter
      {:fill {:scale "color" :field "group"}
       :stroke {:value "white"}}
      :update
      {:size {:signal "2 * nodeRadius * nodeRadius"}
       :cursor {:value "pointer"}}}
     :transform
     [{:type "force"
       :iterations 300
       :restart {:signal "restart"}
       :static {:signal "static"}
       :signal "force"
       :forces
       [{:force "center" :x {:signal "cx"} :y {:signal "cy"}}
        {:force "collide" :radius {:signal "nodeRadius"}}
        {:force "nbody" :strength {:signal "nodeCharge"}}
        {:force "link"
         :links "link-data"
         :distance {:signal "linkDistance"}}]}]}
    {:name "nodes-text"
     :type "text"
     :from {:data "nodes"}
     :encode
     {:update
      {:x {:field "x" :offset 10}
       :y {:field "y" :offset -10}
       :text {:field "datum.name"}
       :fill {:value "#C5C8C6"}}
      :hover
      {:fill {:value "#707880"}}}}
    {:type "path"
     :from {:data "link-data"}
     :interactive false
     :encode
     {:update {:stroke {:value "#ccc"} :strokeWidth {:value 0.5}}}
     :transform
     [{:type "linkpath"
       :require {:signal "force"}
       :shape "line"
       :sourceX "datum.source.x"
       :sourceY "datum.source.y"
       :targetX "datum.target.x"
       :targetY "datum.target.y"}]}]})


(defn ->node
  [id name group]
  {:index id
   :name name :group group})

(defn ->link
  [artist-node band-node]
  {:source (:index artist-node)
   :target (:index band-node)
   :value 1})

(defn network-data->viz-data
  [network-data]
  (let [artist-node (->node 0 (:artist/name network-data) 0)
        [band-nodes links] (->> (:artist/bands network-data)
                                (map-indexed vector)
                                (map (fn [[id band]]
                                       (let [band-node (->node (inc id) (:band/name band) 1)]
                                         [band-node (->link artist-node band-node)])))
                                (apply map vector))]
    [{:name "node-data"
      :values (cons artist-node band-nodes)}
     {:name "link-data"
      :values links}]))

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
       [oz/vega (assoc vega-force
                       :data
                       (network-data->viz-data @data))])]))


(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "Hello from " @name]
     [artist-network-comp]]))
