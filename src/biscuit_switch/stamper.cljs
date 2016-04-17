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
                      :door false
                      :cutter :none}))

(defn reset []
  (reset! state {:running false
                 :door false
                 :cutter :none}))

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
        (if (and
             (not (:running @state))
             (-> biscuit-switch.player/state
                     deref
                     :pos
                     (vec2/sub door-pos)
                     vec2/magnitude-squared
                     (< door-distance-squared)))
          ;; show text
          (do
            (swap! text/state assoc :install
                   (if (not= :none (:cutter @state))
                     :remove
                     (if (not= :none (:carrying @biscuit-switch.player/state))
                       :install
                       :none)))

            (when
                (and (not= :none (:carrying @biscuit-switch.player/state))
                     (events/is-pressed? :space))
              ;; sound
              (sound/play-sound :bloop 1.00 false)

              ;; install cutter
              (let [cutter (:carrying @biscuit-switch.player/state)]
                (swap! state assoc :cutter cutter)
                (swap! biscuit-switch.triangle/state assoc :carried false)
                (swap! biscuit-switch.square/state assoc :carried false)
                (swap! biscuit-switch.circle/state assoc :carried false)

                (case cutter
                  :triangle
                  (swap! biscuit-switch.triangle/state
                         assoc
                         :pos (vec2/vec2 -40 0)
                         :carried false)

                  :square nil
                  :circle nil)

                (biscuit-switch.player/carry :none))

              (while (events/is-pressed? :space)
                (<! (e/next-frame))))

            (when
                (and (= :none (:carrying @biscuit-switch.player/state))
                     (not= :none (:cutter @state))
                     (events/is-pressed? :space))
              ;; remove cutter
              (sound/play-sound :bloop 1.00 false)

              (let [cutter (:cutter @state)]
                (swap! state assoc :cutter :none)
                (biscuit-switch.player/carry cutter)
                (case cutter
                  :triangle
                  (swap! biscuit-switch.triangle/state
                         assoc :carried true)

                  :square nil
                  :circle nil))

              (while (events/is-pressed? :space)
                (<! (e/next-frame)))


              )

            )

          ;; hide text
          (swap! text/state assoc :install :none)

          )



        (<! (e/next-frame))
        (recur (inc f))))))
