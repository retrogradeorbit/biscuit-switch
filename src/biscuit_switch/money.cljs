(ns biscuit-switch.money
  (:require [biscuit-switch.assets :as assets]
            [biscuit-switch.game :as game]

            [infinitelives.pixi.canvas :as c]
            [infinitelives.pixi.events :as e]
            [infinitelives.pixi.resources :as r]
            [infinitelives.pixi.texture :as t]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.pixi.pixelfont :as pf]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.events :as events]
            [infinitelives.utils.sound :as sound]
            [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.boid :as boid]


            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]
                   [infinitelives.pixi.pixelfont :as pf]))


(defonce state
  (atom {:money 0}))

(defn alter [f money]
  (swap! state update :money #(f % money)))

(def add (partial alter +))
(def sub (partial alter -))

(defn money-thread [canvas]
  (go
    (m/with-sprite canvas :ui
      [money-bag (s/make-sprite
                  :money-bag
                  :scale 4 :x -50 :y -300)]

      (loop []
        (let [amount (:money @state)]
          (m/with-sprite canvas :ui
            [money (pf/make-text :font (str amount)
                                 :scale 4
                                 :x 20
                                 :y -320
                                 :visible true)]

            (while (= amount (:money @state))
              (<! (e/next-frame)))))

        (recur)))))
