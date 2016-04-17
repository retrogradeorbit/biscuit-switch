(ns biscuit-switch.text
  (:require [biscuit-switch.assets :as assets]
            [biscuit-switch.game :as game]

            [infinitelives.pixi.canvas :as c]
            [infinitelives.pixi.events :as e]
            [infinitelives.pixi.resources :as r]
            [infinitelives.pixi.texture :as t]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.pixi.pixelfont :as pf]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.events :as events]
            [infinitelives.utils.sound :as sound]
            [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.boid :as boid]


            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]
                   [infinitelives.pixi.pixelfont :as pf]))


(defonce state
  (atom {:roller :none
         :stamper :none
         :door :none
         :oven :none
         :stamp :none
         :pickup-text-pos (vec2/vec2 0 0)}))

(defn set-pickup-text-pos [pos]
  (swap! state assoc :pickup-text-pos pos))

(defn text-thread [canvas]
  (go (m/with-sprite canvas :ui
        [roller-on (pf/make-text :font "Switch the roller on"
                                 :scale 2
                                 :x -350
                                 :y -150
                                 :visible false
                                 )
         roller-off (pf/make-text :font "Switch the roller off"
                                  :scale 2
                                  :x -350
                                  :y -150
                                  :visible false)

         stamper-on (pf/make-text :font "Switch the stamper on"
                                  :scale 2
                                  :x 0
                                  :y -150
                                  :visible false
                                  )
         stamper-off (pf/make-text :font "Switch the stamper off"
                                   :scale 2
                                   :x 0
                                   :y -150
                                   :visible false
                                   )
         stamper-open (pf/make-text :font "Open the access door"
                                    :scale 2
                                    :x -50
                                    :y -150
                                    :visible false
                                    )
         stamper-close (pf/make-text :font "Close the access door"
                                     :scale 2
                                     :x -50
                                     :y -150
                                     :visible false
                                     )

         oven-on (pf/make-text :font "Switch the oven on"
                               :scale 2
                               :x 350
                               :y -150
                               :visible false
                               )
         oven-off (pf/make-text :font "Switch the oven off"
                                :scale 2
                                :x 350
                                :y -150
                                :visible false
                                )

         stamp-pickup (pf/make-text :font "Pick up the cutting stamp"
                                    :scale 2 :x 0 :y 150 :visible false)

         stamp-putdown (pf/make-text :font "Put down the cutting stamp"
                                    :scale 2 :x 0 :y 150 :visible false)


         ]

        (loop [f 0]
          (cond
            (events/is-pressed? :b)
            (swap! state assoc :stamp :pickup)

            (events/is-pressed? :n)
            (swap! state assoc :stamp :putdown)

            (events/is-pressed? :m)
            (swap! state assoc :stamp :none))

          ;; set states
          (let [[on off] (case (:roller @state)
                           :on [true false]
                           :off [false true]
                           :none [false false])]
            (s/set-visible! roller-off off)
            (s/set-visible! roller-on on))

          (let [[on off] (case (:stamper @state)
                           :on [true false]
                           :off [false true]
                           :none [false false])]
            (s/set-visible! stamper-off off)
            (s/set-visible! stamper-on on))

          (let [[on off] (case (:oven @state)
                           :on [true false]
                           :off [false true]
                           :none [false false])]
            (s/set-visible! oven-off off)
            (s/set-visible! oven-on on))

          (let [[open close] (case (:door @state)
                               :open [true false]
                               :close [false true]
                               :none [false false])]
            (s/set-visible! stamper-open open)
            (s/set-visible! stamper-close close))

          (let [[pickup putdown] (case (:stamp @state)
                           :pickup [true false]
                           :putdown [false true]
                           :none [false false])]
            (s/set-visible! stamp-putdown putdown)
            (s/set-visible! stamp-pickup pickup))

          ;; pickup text tracks stamp
          (let [text-pos (vec2/add (:pickup-text-pos @state) (vec2/vec2 0 -50))]
            (s/set-pos! stamp-putdown text-pos)
            (s/set-pos! stamp-pickup text-pos))

          (<! (e/next-frame))
          (recur (inc f))
          )))
  )
