(ns biscuit-switch.rising
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

(defn text [canvas text scale pos vel length]
  (go
    (m/with-sprite canvas :ui
      [rising-text
       (pf/make-text :font text :scale scale)]
      (loop [c length p pos]
        (s/set-pos! rising-text p)
        (<! (e/next-frame))
        (when (pos? c)
          (recur (dec c) (vec2/add p vel)))))))
