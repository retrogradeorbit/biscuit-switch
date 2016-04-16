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
         :oven :none}))

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
         ]

        (loop [f 0]
          (cond
            (events/is-pressed? :b)
            (swap! state assoc :door :open)

            (events/is-pressed? :n)
            (swap! state assoc :door :close)

            (events/is-pressed? :m)
            (swap! state assoc :door :none))

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


          (<! (e/next-frame))
          (recur (inc f))
          )))
  )
