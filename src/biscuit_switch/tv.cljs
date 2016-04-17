(ns biscuit-switch.tv
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
  (atom {:shape :any}))

(defn alter [shape]
  (swap! state assoc :shape shape))

(defn tv-thread [canvas]
  (go
    (m/with-sprite canvas :machines
      [tv (s/make-sprite :tv :scale 4 :x 300 :y -280)
       triangle (s/make-sprite :tv-triangle :scale 4 :x 260 :y -275 :visible false)
       square (s/make-sprite :tv-square :scale 4 :x 260 :y -275 :visible false)
       circle (s/make-sprite :tv-circle :scale 4 :x 260 :y -275 :visible false)
       ]
      (loop []
        (let [[tri sq circ] (case (:shape @state)
                              :triangle
                              [true false false]

                              :square
                              [false true false]

                              :circle
                              [false false true]

                              :any
                              [false false false])]
          (s/set-visible! triangle tri)
          (s/set-visible! square sq)
          (s/set-visible! circle circ)
          )

        (<! (e/next-frame))
        (recur)))))
