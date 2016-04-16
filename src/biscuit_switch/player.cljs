(ns biscuit-switch.player
  (:require [biscuit-switch.assets :as assets]
            [biscuit-switch.game :as game]

            [infinitelives.pixi.canvas :as c]
            [infinitelives.pixi.events :as e]
            [infinitelives.pixi.resources :as r]
            [infinitelives.pixi.texture :as t]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.sound :as sound]

            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(defn player [canvas]
  (go
    (m/with-sprite canvas :player
          [
           player (s/make-sprite :player-stand-left
                                 :scale 4
                                 :x 0 :y 120)
           ]


          (loop [c 20000]

            (let [fnum (int (/ c 30))]
              (if (odd? fnum)
                (s/set-texture! player :player-stand-left)
                (s/set-texture! player :player-stand-left-2)
                )
              )

            (<! (e/next-frame))
            (recur (dec c))
            ))))
