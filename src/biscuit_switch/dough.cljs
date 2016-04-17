(ns biscuit-switch.dough
  (:require
            [biscuit-switch.text :as text]
            [biscuit-switch.money :as money]

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


(def dough-speed 0.7)

(def oven-position 630)
(def roller-length 560)

(defn uncut-thread [canvas]
  (go
    (m/with-sprite canvas :machines
      [biscuit (s/make-sprite :dough-flat :scale 4 :x 0 :y 0)]
      (loop [f 0]
        (when (< f oven-position)
          (s/set-pos! biscuit  (+ (* f dough-speed) -20) -30)
          (<! (e/next-frame))
          (recur (inc f))
          ))))
  )

(defn biscuit-thread [canvas shape]
  (go
    (let [texture (case shape
                    :triangle :biscuit-triangle
                    :square :biscuit-square
                    :circle :biscuit-circle)]
      (m/with-sprite canvas :machines
        [biscuit (s/make-sprite texture :scale 4 :x 0 :y 0)
         biscuit2 (s/make-sprite texture :scale 4 :x 0 :y 0)
         biscuit3 (s/make-sprite texture :scale 4 :x 0 :y 0)
         biscuit4 (s/make-sprite texture :scale 4 :x 0 :y 0)
         ]

        (loop [f 0]

          (when (< f oven-position)
            (s/set-pos! biscuit  (+ (* f dough-speed) -20) -30)
            (s/set-pos! biscuit2 (+ (* f dough-speed) -40) -20)
            (s/set-pos! biscuit3 (+ (* f dough-speed) -40) -40)
            (s/set-pos! biscuit4 (+ (* f dough-speed) -60) -30)

            (<! (e/next-frame))
            (recur (inc f))
            )))))
  )

(defn dough-thread [canvas]
  (go
    (m/with-sprite canvas :machines
      [dough (s/make-sprite :dough-flat :scale 4 :x 0 :y 0)]

      (loop [f 0]

        (when (< f roller-length)
          (s/set-pos! dough (+ (* f dough-speed) -440) -30)
          (<! (e/next-frame))
          (recur (inc f))
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
      (money/sub 1)
      )
    )
)
