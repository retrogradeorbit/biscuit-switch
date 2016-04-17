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
         :install :none
         :oven :none
         :stamp :none
         :triangle :none
         :circle :none
         :square :none}))

(defn reset []
  (reset! state {:roller :none
         :stamper :none
         :install :none
         :oven :none
         :stamp :none
         :triangle :none
         :circle :none
         :square :none}))

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
         stamper-install (pf/make-text :font "Install the shape cutter"
                                    :scale 2
                                    :x -50
                                    :y -150
                                    :visible false
                                    )
         stamper-remove (pf/make-text :font "Remove the shape cutter"
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

         triangle-pickup (pf/make-text :font "Pick up the triangular cutting stamp"
                                    :scale 2 :x -230 :y 270 :visible false)
         triangle-putdown (pf/make-text :font "Put down the triangular cutting stamp"
                                    :scale 2 :x -230 :y 270 :visible false)

         square-pickup (pf/make-text :font "Pick up the square cutting stamp"
                                    :scale 2 :x 0 :y 300 :visible false)
         square-putdown (pf/make-text :font "Put down the square cutting stamp"
                                    :scale 2 :x 0 :y 300 :visible false)

         circle-pickup (pf/make-text :font "Pick up the circular cutting stamp"
                                    :scale 2 :x 230 :y 270 :visible false)
         circle-putdown (pf/make-text :font "Put down the circular cutting stamp"
                                    :scale 2 :x 230 :y 270 :visible false)

         ]

        (loop [f 0]
          (cond
            (events/is-pressed? :b)
            (swap! state assoc :circle :pickup)

            (events/is-pressed? :n)
            (swap! state assoc :circle :putdown)

            (events/is-pressed? :m)
            (swap! state assoc :circle :none))

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

          (let [[install remove] (case (:install @state)
                               :install [true false]
                               :remove [false true]
                               :none [false false])]
            (s/set-visible! stamper-install install)
            (s/set-visible! stamper-remove remove))


          (let [[pickup putdown] (case (:triangle @state)
                           :pickup [true false]
                           :putdown [false true]
                           :none [false false])]
            (s/set-visible! triangle-putdown putdown)
            (s/set-visible! triangle-pickup pickup))

          (let [[pickup putdown] (case (:circle @state)
                           :pickup [true false]
                           :putdown [false true]
                           :none [false false])]
            (s/set-visible! circle-putdown putdown)
            (s/set-visible! circle-pickup pickup))

          (let [[pickup putdown] (case (:square @state)
                           :pickup [true false]
                           :putdown [false true]
                           :none [false false])]
            (s/set-visible! square-putdown putdown)
            (s/set-visible! square-pickup pickup))


          (<! (e/next-frame))
          (recur (inc f))
          )))
  )
