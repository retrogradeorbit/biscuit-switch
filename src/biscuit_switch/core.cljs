(ns biscuit-switch.core
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

(defonce canvas
  (c/init {:layers [:bg :belt :player :tables :ui]
           :background 0xa0a0a0
           :expand true}))

(defonce main
  (go
    (<! (r/load-resources canvas :ui
                          ["img/sprites.png"
                           "sfx/bloop.ogg"]))

    (sound/play-sound :bloop 1.00 false)

    (t/load-sprite-sheet!
     (r/get-texture :sprites :nearest)
     assets/assets)))
