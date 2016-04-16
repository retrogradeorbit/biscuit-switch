(ns biscuit-switch.core
  (:require [biscuit-switch.assets :as assets]
            [biscuit-switch.game :as game]
            [biscuit-switch.player :as player]
            [biscuit-switch.text :as text]
            [biscuit-switch.stamper :as stamper]
            [biscuit-switch.roller :as roller]
            [biscuit-switch.oven :as oven]

            [infinitelives.pixi.canvas :as c]
            [infinitelives.pixi.events :as e]
            [infinitelives.pixi.resources :as r]
            [infinitelives.pixi.texture :as t]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.pixi.pixelfont :as pf]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.sound :as sound]

            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]
                   [infinitelives.pixi.pixelfont :as pf]
                   ))

(defonce canvas
  (c/init {:layers [:bg :belt :machines :player :tables :ui]
           :background 0xa0a0a0
           :expand true
           :origins {:roller :left}}))

(defonce main
  (go
    (<! (r/load-resources canvas :ui
                          ["img/sprites.png"
                           "img/fonts.png"
                           "sfx/bloop.ogg"]))

    (pf/pixel-font :font "img/fonts.png" [12 118] [229 167]
                   :chars ["ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                           "abcdefghijklmnopqrstuvwxyz"
                           "0123456789!?#`'.,"]
                   :kerning {"fo" -2  "ro" -1 "la" -1 }
                   :space 5)

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
         tv (s/make-sprite :tv :scale 4 :x 300 :y -280)
         tri-table (s/make-sprite :tri-table :scale 4 :x -250 :y 275)
         circle-table (s/make-sprite :round-table :scale 4 :x 0 :y 300)
         square-table (s/make-sprite :square-table :scale 4 :x 250 :y 300)
         ]

        ;; "threads"
        (player/player canvas)
        (text/text-thread canvas)
        (stamper/stamper-thread canvas)
        (roller/roller-thread canvas)
        (oven/oven-thread canvas)


        (loop [c 20000]

            (<! (e/next-frame))
            (recur (dec c))
            )

)

      )

    ))
