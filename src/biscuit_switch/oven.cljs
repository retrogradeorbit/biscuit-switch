(ns biscuit-switch.oven
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

(defn reset []
  (reset! state {:running false}))

(def switch-pos (vec2/vec2 330 0))
(def switch-distance 20)
(def switch-distance-squared (* switch-distance switch-distance))

(def flame-count 15)
(def fires [:fire-1 :fire-2 :fire-3 :fire-4])

(defn oven-state [canvas oven]
  (go
    (loop []
      (when (:running @state)
        ;; fire up sound
        (.log js/console "ON")

        (m/with-sprite-set canvas :machines
          [flames (doall (map #(s/make-sprite
                                (rand-nth fires)
                                :scale 4
                                :x (math/rand-between 370 460) :y 15)
                              (range flame-count)))]

          (while (:running @state)
            ;; flames
            (s/set-texture! (rand-nth flames) (rand-nth fires))
            (s/set-x! (rand-nth flames) (math/rand-between 370 460))
            (<! (e/next-frame)))

          (.log js/console "OFF")
          ))

      (<! (e/next-frame))
      (recur)))
  )

(defn oven-thread [canvas]
  (go
    (m/with-sprite canvas :machines
      [oven (s/make-sprite :oven :scale 4 :x 450 :y -148)]
      (oven-state canvas oven)
      (loop [f 0]
        (if (-> biscuit-switch.player/state
                deref
                :pos
                (vec2/sub switch-pos)
                vec2/magnitude-squared
                (< switch-distance-squared))
          ;; show text
          (do
            (swap! text/state assoc :oven (if (:running @state) :off :on))

            (when (events/is-pressed? :space)
              ;; sound
              (sound/play-sound :bloop 1.00 false)
              (swap! state update :running not)

              (while (events/is-pressed? :space)
                (<! (e/next-frame)))))

          ;; hide text
          (swap! text/state assoc :oven :none)

          )


        (<! (e/next-frame))
        (recur (inc f))))))
