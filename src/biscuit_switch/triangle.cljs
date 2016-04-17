(ns biscuit-switch.triangle
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


(defonce state (atom {:pos (vec2/vec2 0 0)
                      :carried true}))

(def pickup-distance 10)
(def pickup-distance-squared (* pickup-distance pickup-distance))

(defn triangle-thread [canvas]
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

          ;; just sits where it is
          (s/set-pos! stamp (:pos @state))
          )

        (if (:carried @state)
          ;; drop?
          (when (events/is-pressed? :space)
            (sound/play-sound :bloop 1.0 false)
            (swap! state #(-> %
                              (assoc :carried false)
                              (assoc :pos
                                     (vec2/add
                                      (:pos @biscuit-switch.player/state)
                                      (vec2/vec2 0 20)))))
            (biscuit-switch.player/carry :none)

            ;; wait for release
            (while (events/is-pressed? :space)
                  (<! (e/next-frame))))

          ;; pickup?
          (if (-> biscuit-switch.player/state
                  deref
                  :pos
                  (vec2/sub (:pos @state))
                  vec2/magnitude-squared
                  (< pickup-distance-squared))
            (do
              ;; show pickup text
              (text/set-pickup-text-pos (:pos @state))
              (swap! biscuit-switch.text/state assoc :stamp :pickup)

              (when (events/is-pressed? :space)
                (sound/play-sound :bloop 1.0 false)

                (swap! biscuit-switch.text/state assoc :stamp :none)
                (swap! state assoc :carried true)
                (biscuit-switch.player/carry :triangle)

                (while (events/is-pressed? :space)
                  (<! (e/next-frame)))))

            ;; hide pickup text
            (swap! biscuit-switch.text/state assoc :stamp :none)
            )
          )

          ;; wait for space to be released

        #_ (while (events/is-pressed? :space)
                (<! (e/next-frame)))


        (<! (e/next-frame))
        (recur)))))
