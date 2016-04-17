(ns biscuit-switch.roller
  (:require
            [biscuit-switch.text :as text]
            [biscuit-switch.dough :as dough]

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
                      :interval 150}))

(defn reset []
  (reset! state {:running false
                      :interval 150}))

(def switch-pos (vec2/vec2 -430 0))
(def switch-distance 20)
(def switch-distance-squared (* switch-distance switch-distance))

(defn dough-interval [] (max 40 (:interval @state)))

(defn roller-state [canvas roller siren]
  (go
    (loop []
      (when (:running @state)
        ;; fire up sound
        (.log js/console "ON")
        (s/set-texture! siren :siren-green)

        (loop [runtime 0 countdown 0]
          (when (not (pos? countdown))
            (dough/dough-thread canvas)
            (recur 0 (dough-interval)))

          (when (:running @state)
            (<! (e/next-frame))
            (recur (inc runtime) (dec countdown)))
          ))


      (s/set-texture! siren :siren-grey)

      (<! (e/next-frame))
      (recur)))
  )

(defn roller-thread [canvas]
  (go
    (m/with-sprite canvas :machines
      [roller (s/make-sprite :roller :scale 4 :x -382 :y -72)
       siren (s/make-sprite
                    :siren-grey
                    :scale 4
                    :x -350 :y -160)]
      (roller-state canvas roller siren)
      (loop [f 0]
        (if (-> biscuit-switch.player/state
                deref
                :pos
                (vec2/sub switch-pos)
                vec2/magnitude-squared
                (< switch-distance-squared))
          ;; show text
          (do
            (swap! text/state assoc :roller (if (:running @state) :off :on))
            (when (events/is-pressed? :space)
              ;; sound
              (sound/play-sound (if (:running @state) :off :on) 0.8 false)
              (swap! state update :running not)

              (while (events/is-pressed? :space)
                (<! (e/next-frame)))))

          ;; hide text
          (swap! text/state assoc :roller :none)

          )


        (<! (e/next-frame))
        (recur (inc f))))))
