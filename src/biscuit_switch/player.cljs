(ns biscuit-switch.player
  (:require [biscuit-switch.assets :as assets]
            [biscuit-switch.game :as game]

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

(def p-boid
  {:mass 1.0
   :pos (vec2/vec2 0 120)
   :vel (vec2/vec2 0 0)
   :max-force 5.0
   :max-speed 7.0})

(def anim-speed-empty 10)
(def anim-speed-carrying 20)

(def state (atom {:pos (vec2/vec2 0 0)
                  :facing :left
                  :carrying :none}))

(defn left? []
  (events/is-pressed? :left))

(defn right? []
  (events/is-pressed? :right))

(defn up? []
  (events/is-pressed? :up))

(defn down? []
  (events/is-pressed? :down))

(defn drag [bd]
  (assoc bd :vel (vec2/scale (:vel bd)
                             (if (or (left?) (right?) (up?) (down?))
                               ;; when walking drag is lower
                               0.95
                               0.90)))
  )

(defn carry [s]
  (swap! state assoc :carrying s))

(defn limit-y
  "constrain boid to walkable area"
  [{:keys [pos vel] :as bd} cmp limit]
  (let [[x y] (vec2/as-vector pos)
        [vx vy] (vec2/as-vector vel)]
    (if (cmp y limit)
      (assoc bd
             :vel (vec2/vec2 vx 0)
             :pos (vec2/vec2 x limit))
      bd))
)

(defn limit-x
  "constrain boid to walkable area"
  [{:keys [pos vel] :as bd} cmp limit]
  (let [[x y] (vec2/as-vector pos)
        [vx vy] (vec2/as-vector vel)]

    ;; limit bottom
    (if (cmp x limit)
      (assoc bd
             :vel (vec2/vec2 0 vy)
             :pos (vec2/vec2 limit y))
      bd))
)

(defn player [canvas]
  (go
    (m/with-sprite canvas :player
      [
       player (s/make-sprite :player-stand-left
                             :scale 4
                             :x 0 :y 120)
       ]
      (loop [c 20000
             b p-boid
             ]
        (if (or (left?) (right?) (up?) (down?))
          (let [fnum
                (int (/ c
                        (if
                            (= :none (:carrying @state))
                          anim-speed-empty anim-speed-carrying)))]
            (if (odd? fnum)
              (s/set-texture! player :player-stride-left)
              (s/set-texture! player :player-stand-left-2)
              )
            )
          (if (< (vec2/magnitude (:vel b)) 0.01)
            (let [fnum (int (/ c 50))]
              (if (odd? fnum)
                (s/set-texture! player :player-stand-left)
                (s/set-texture! player :player-stand-left-2)
                ))

            (s/set-texture! player :player-stand-left)


            ))

        ;; make player sprite reflect boid
        (s/set-pos! player (:pos b))
        (let [face-left? (neg? (aget (:vel b) 0))]
          (if face-left?
            (do (swap! state assoc :facing :left)
                (s/set-scale! player 4 4))
            (do (swap! state assoc :facing :right)
                (s/set-scale! player -4 4))))


                                        ;
        (swap! state assoc :pos (:pos b))
                                        ;(.log js/console "pos:" (str (vec2/as-vector (:pos b))))


        (<! (e/next-frame))



        (let [speed (if (= :none (:carrying @state))
                      0.2
                      0.05)]
          (recur (dec c)

                 (-> b
                     (boid/apply-steering
                      (vec2/vec2
                       (if (left?) (- speed) (if (right?) speed 0.0))
                       (if (up?) (- speed) (if (down?) speed 0.0)))
                      )
                     drag
                     (limit-y < 0)
                     (limit-y > 200)
                     (limit-x < -450)
                     (limit-x > 450))))))))
