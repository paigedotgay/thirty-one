(ns thirty-one.gamestate.builder
  (:use [thirty-one.cards]))

(defn add-player
  [gamestate player-name]
  (assoc-in gamestate [:players player-name] 
            {:name player-name
             :hand []
             :hand-points 0
             :lives 5}))

(defn new-gamestate
  []
  {:deck (shuffle (build-deck))
   :discard-pile []
   :players {}
   :active-player nil
   :knocking-player nil})
   
  
