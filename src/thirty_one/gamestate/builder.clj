(ns thirty-one.gamestate.builder
  (:use [thirty-one.cards]
        [thirty-one.player]))

(defn new-gamestate
  []
  {:deck (shuffle (build-deck))
   :discard nil
   :selected-card nil
   :players []
   :knocking-player nil})
   
(defn add-player
  [gamestate player-name]
  (update-in gamestate [:players] 
             #(conj % {:name player-name
                       :hand []
                       :hand-points 0
                       :lives 5})))

;; I don't like using flatten here, but conj kept changing the resulting type
(defn next-player
  [gamestate]
  (let [players (gamestate :players)]
    (assoc-in gamestate [:players]
              (conj (vec (rest players)) (first players)))))

(defn deal
  [gamestate]
  (loop [gs gamestate
         cards-remaining (* 3 (-> gamestate :players count))]
    (if (zero? (cards-remaining))
      gs
      (recur (-> gs
                 draw-card-from-deck
                 next-player)
             (dec cards-remaining)))))

