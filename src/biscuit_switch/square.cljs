(ns biscuit-switch.square
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


(defonce state (atom {:carried false}))

(defn reset []
  (reset! state {:carried false}))

(def pickup-distance 10)
(def pickup-distance-squared (* pickup-distance pickup-distance))

(def installed-point (vec2/vec2 -45 0))

(def drop-point (vec2/vec2 0 280))
(def drop-distance 100)
(def drop-distance-squared (* drop-distance drop-distance))

(defn square-thread [canvas]
  (go
    (m/with-sprite canvas :stamps
      [stamp (s/make-sprite :stamp :scale 4
                            :x 100 :y 100)]

      (loop []

        (if (:carried @state)
          ;; carried by player
          (s/set-pos! stamp
                      (vec2/add
                       (:pos @biscuit-switch.player/state)
                       (case (:facing @biscuit-switch.player/state)
                         :left
                         (vec2/vec2 -20 0)

                         :right
                         (vec2/vec2 20 0))
                       ))

          ;; not carried
          (if (= :square (:cutter @biscuit-switch.stamper/state))
            (s/set-pos! stamp installed-point)
            (s/set-pos! stamp drop-point)
            )
          )


        (if (:carried @state)
          ;; carried
          ;; drop?
          (if (-> biscuit-switch.player/state
                deref
                :pos
                (vec2/sub drop-point)
                vec2/magnitude-squared
                (< drop-distance-squared))
            (do
              (swap! biscuit-switch.text/state assoc :square :putdown)
                                        ;(> (vec2/get-y (:pos @biscuit-switch.player/state)) 20)
              (when (events/is-pressed? :space)
                (sound/play-sound :put-down 0.5 false)
                (swap! state #(-> %
                                  (assoc :carried false)
                                  (assoc :pos
                                         (vec2/add
                                          (:pos @biscuit-switch.player/state)
                                          (vec2/vec2 0 20)))))
                (biscuit-switch.player/carry :none)

                ;; wait for release
                (while (events/is-pressed? :space)
                  (<! (e/next-frame)))
                ;(swap! biscuit-switch.text/state assoc :square :none)
                ))

            (swap! biscuit-switch.text/state assoc :square :none)
            )

          ;; not carried
          ;; pickup?
          (if (and
               (> (vec2/get-y (:pos @biscuit-switch.player/state)) 20)
               (-> biscuit-switch.player/state
                       deref
                       :pos
                       (vec2/sub drop-point)
                       vec2/magnitude-squared
                       (< drop-distance-squared)))
            (do
              ;; show pickup text
              ;(text/set-pickup-text-pos drop-point)
              (swap! biscuit-switch.text/state assoc :square :pickup)

              (when (events/is-pressed? :space)
                (sound/play-sound :pick-up 0.5 false)

                (swap! biscuit-switch.text/state assoc :square :none)
                (swap! state assoc :carried true)
                (biscuit-switch.player/carry :square)

                (while (events/is-pressed? :space)
                  (<! (e/next-frame)))))

            ;; hide pickup text
            (swap! biscuit-switch.text/state assoc :square :none)
            )
          )

          ;; wait for space to be released

        #_ (while (events/is-pressed? :space)
                (<! (e/next-frame)))


        (<! (e/next-frame))
        (recur)))))
