(ns biscuit-switch.stamper
  (:require
            [biscuit-switch.text :as text]

            [infinitelives.pixi.canvas :as c]
            [infinitelives.pixi.events :as e]
            [infinitelives.pixi.resources :as r]
            [infinitelives.pixi.texture :as t]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.events :as events]
            [infinitelives.utils.sound :as sound]
            [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.boid :as boid]

            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(defonce state (atom {:running false}))

; roller
;(def switch-pos (vec2/vec2 -433 0))

(def switch-pos (vec2/vec2 7 0))
(def switch-distance 20)
(def switch-distance-squared (* switch-distance switch-distance))

(defn stamper-thread [canvas]
  (go
    (m/with-sprite canvas :machines
      [stamper (s/make-sprite :stamper :scale 4 :y -74)]
      (loop [f 0]
        (.log js/console (str @biscuit-switch.player/state))
        (if (-> biscuit-switch.player/state
                deref
                :pos
                (vec2/sub switch-pos)
                vec2/magnitude-squared
                (< switch-distance-squared))
          ;; show text
          (swap! text/state assoc :stamper (if (:running @state) :off :on))

          ;; hide text
          (swap! text/state assoc :stamper :none)

          )


        (<! (e/next-frame))
        (recur (inc f))))))
