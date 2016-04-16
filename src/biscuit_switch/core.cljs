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
  (c/init {:layers [:bg :belt :machines :player :tables :ui]
           :background 0xa0a0a0
           :expand true
           :origins {:roller :left}}))

(defonce main
  (go
    (<! (r/load-resources canvas :ui
                          ["img/sprites.png"
                           "sfx/bloop.ogg"]))

    (sound/play-sound :bloop 1.00 false)

    (t/load-sprite-sheet!
     (r/get-texture :sprites :nearest)
     assets/assets)

    (m/with-sprite-set canvas :belt
      [conveyors
       (map
        #(s/make-sprite :conveyor :scale 4
                        :x (* 64 %)
                       ;; TODO: get tiling going in infinitelives
                                        ;:tiling :tiling-width 100 :tiling-height 100
                        )
        (range -10 10)
        )]
      (m/with-sprite canvas :machines
        [
         roller (s/make-sprite :roller :scale 4 :x -350 :y -72)
         stamper (s/make-sprite :stamper :scale 4 :y -74)
         oven (s/make-sprite :oven :scale 4 :x 450 :y -148)
         tv (s/make-sprite :tv :scale 4 :x 300 :y -280)
         tri-table (s/make-sprite :tri-table :scale 4 :x -250 :y 250)
         circle-table (s/make-sprite :round-table :scale 4 :x 0 :y 300)
         square-table (s/make-sprite :square-table :scale 4 :x 250 :y 250)
         ]

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
            )))

      )
))
