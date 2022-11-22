(ns thirty-one.gamestate.evaluator)

(defn get-hand-points
  [gamestate]
  (for [player (gamestate :players)
        suit [:clubs :diamonds :hearts :spades]]
    (player :hand)
    
