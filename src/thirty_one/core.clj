(ns thirty-one.core
  (:use [thirty-one.input]
        [thirty-one.gamestate]))

(defn take-turn
  [gamestate input-handler]
  (loop [gs gamestate]
    (if (= (gs :awaiting) :next-turn)
      gs
      (recur (input-handler gs)))))

(defn gameplay-loop
  [gamestate input-handler]
  (loop [gs gamestate]
    (if (or (seq (blitzes gs))
            (= (-> gs :players first)
               (-> gs :knocking-player)))
      gs
      (recur (-> gs (take-turn input-handler))))))

(defn start-game 
  "Creates a new gamestate, adds all players, deals opening hands, and checks scores before moving to main gameplay loop"
  [players & input-handler]
  (-> (reduce add-player (new-gamestate) players)
      (deal)))

      
    
  

