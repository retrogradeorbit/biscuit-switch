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

(defonce state (atom {:running false
                      :door false}))

(def switch-pos (vec2/vec2 7 0))
(def switch-distance 20)
(def switch-distance-squared (* switch-distance switch-distance))

(def door-pos (vec2/vec2 -60 0))
(def door-distance 20)
(def door-distance-squared (* door-distance door-distance))


(defn stamper-state [canvas stamper siren]
  (go
    (loop []
      (when (:running @state)
        ;; fire up sound
        (.log js/console "ON")
        (s/set-texture! siren :siren-green)

        (while (:running @state)
            (<! (e/next-frame))
            )
        )


      (s/set-texture! siren :siren-grey)

      (<! (e/next-frame))
      (recur)))
  )

(defn stamper-thread [canvas]
  (go
    (m/with-sprite canvas :machines
      [stamper (s/make-sprite :stamper :scale 4 :y -74)
       siren (s/make-sprite
                    :siren-grey
                    :scale 4
                    :x 55 :y -100)]
      (stamper-state canvas stamper siren)
      (loop [f 0]

        ;; on/off
        (if (-> biscuit-switch.player/state
                deref
                :pos
                (vec2/sub switch-pos)
                vec2/magnitude-squared
                (< switch-distance-squared))
          ;; show text
          (do
            (swap! text/state assoc :stamper (if (:running @state) :off :on))
            (when (events/is-pressed? :space)
              ;; sound
              (sound/play-sound :bloop 1.00 false)
              (swap! state update :running not)

              (while (events/is-pressed? :space)
                (<! (e/next-frame)))))

          ;; hide text
          (swap! text/state assoc :stamper :none)

          )

        ;; access door
        (if (-> biscuit-switch.player/state
                deref
                :pos
                (vec2/sub door-pos)
                vec2/magnitude-squared
                (< door-distance-squared))
          ;; show text
          (do
            (swap! text/state assoc :door (if (:door @state) :close :open))
            (when (events/is-pressed? :space)
              ;; sound
              (sound/play-sound :bloop 1.00 false)
              (swap! state update :door not)

              (while (events/is-pressed? :space)
                (<! (e/next-frame)))))

          ;; hide text
          (swap! text/state assoc :door :none)

          )



        (<! (e/next-frame))
        (recur (inc f))))))
