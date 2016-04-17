(ns biscuit-switch.dough
  (:require
            [biscuit-switch.text :as text]
            [biscuit-switch.money :as money]
            [biscuit-switch.rising :as rising]
            [biscuit-switch.tv :as tv]

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


(defonce state (atom {:speed 0.7}))

(defn reset []
  (reset! state {:speed 0.7}))

(defn dough-speed [] (:speed @state))

(def oven-position 440)
(def stamper-position -30)
(def roller-length 560)

(defn uncut-thread [canvas]
  (go
    (m/with-sprite canvas :dough
      [biscuit (s/make-sprite :dough-flat :scale 4 :x 0 :y 0)]
      (sound/play-sound :stamp 0.6 false)
      (loop [f 0 pos stamper-position]
        (when (< pos oven-position)
          (s/set-pos! biscuit pos -30)
          (<! (e/next-frame))
          (recur (inc f) (+ pos (dough-speed)))))
      (let [oven-on? (:running @biscuit-switch.oven/state)]
        (cond
          (not oven-on?)
          (do
            (sound/play-sound :splat-2 0.3 false)
            (rising/text canvas "-1" 2 (vec2/vec2 400 -40) (vec2/vec2 0 -1) 100)
            (money/sub 1))

          :default
          (do
            (sound/play-sound :money-loss 0.3 false)
            (rising/text canvas "-5" 2 (vec2/vec2 400 -40) (vec2/vec2 0 -1) 100)
            (money/sub 5)))))))

(defn biscuit-thread [canvas shape]
  (go
    (let [texture (case shape
                    :triangle :biscuit-triangle
                    :square :biscuit-square
                    :circle :biscuit-circle)]
      (m/with-sprite canvas :dough
        [biscuit (s/make-sprite texture :scale 4 :x 0 :y 0)
         biscuit2 (s/make-sprite texture :scale 4 :x 0 :y 0)
         biscuit3 (s/make-sprite texture :scale 4 :x 0 :y 0)
         biscuit4 (s/make-sprite texture :scale 4 :x 0 :y 0)
         ]
        (sound/play-sound :stamp 0.6 false)
        (loop [f 0 pos stamper-position]

          (when (< pos oven-position)
            (s/set-pos! biscuit  (+ pos 20) -30)
            (s/set-pos! biscuit2 (+ pos 0) -20)
            (s/set-pos! biscuit3 (+ pos 0) -40)
            (s/set-pos! biscuit4 (+ pos -20) -30)

            (<! (e/next-frame))
            (recur (inc f) (+ pos (dough-speed)))))

        ;; reached oven...
        (let [oven-on? (:running @biscuit-switch.oven/state)
              tv-shape (:shape @biscuit-switch.tv/state)]
          (cond
            ;; successful batch
            (and oven-on?
                 (or (= :any tv-shape)
                     (= shape tv-shape)))
            (do
              (sound/play-sound :money 0.5 false)
              (rising/text canvas "+10" 2 (vec2/vec2 400 -40) (vec2/vec2 0 -1) 100)
              (money/add 10)
              (tv/one-made))

            (not oven-on?)
            (do
              (sound/play-sound :splat-2 0.3 false)
              (rising/text canvas "-1" 2 (vec2/vec2 400 -40) (vec2/vec2 0 -1) 100)
              (money/sub 1))

            ;; unsuccessful
            :default
            (do
              (sound/play-sound :money-loss 0.3 false)
              (rising/text canvas "-5" 2 (vec2/vec2 400 -40) (vec2/vec2 0 -1) 100)
              (money/sub 5))

            ))
        )))
  )

(defn dough-thread [canvas]
  (go
    (m/with-sprite canvas :dough
      [dough (s/make-sprite :dough-flat :scale 4 :x 0 :y 0)]

      (sound/play-sound :dough 0.3 false)

      (loop [f 0 pos -440]

        (when (< pos stamper-position)
          (s/set-pos! dough pos -30)
          (<! (e/next-frame))
          (recur (inc f) (+ pos (dough-speed)))
          )))

    ;; if stamper is on, we get stamped. if its off, we loose money
    (if (:running @biscuit-switch.stamper/state)
      ;; on
      (if (= :none (:cutter @biscuit-switch.stamper/state))
        ;; no cutter installed... dough comes out other end
        (uncut-thread canvas)

        ;; cutter installed
        (biscuit-thread canvas (:cutter @biscuit-switch.stamper/state)))

      ;; off, lose money
      (do
        (sound/play-sound :splat 0.3 false)
        (rising/text canvas "-1" 2 (vec2/vec2 -80 -40) (vec2/vec2 0 -1) 100)
        (money/sub 1))
      )
    )
)
