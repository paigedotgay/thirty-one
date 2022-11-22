(ns thirty-one.core
  (:use [thirty-one.gamestate.builder]
        [thirty-one.player]))


(defn start-game 
  [players]
  (as-> (new-gamestate) gs
    (reduce add-player gs players)
    (deal gs)))
