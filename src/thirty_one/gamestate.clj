(ns thirty-one.gamestate
  (:use [thirty-one.cards]))

(defn new-gamestate
  "Creates the basic skeleton of the gamestate, including a shuffled deck."
  []
  {:awaiting nil
   :deck (shuffle (build-deck))
   :discard nil
   :knocking-player nil
   :players []})

(defn add-player
  [gamestate player-name]
  (update-in gamestate [:players] 
             #(conj % {:name player-name
                       :hand []
                       :hand-points 0
                       :lives 5})))

(defn next-player
  [gamestate]
  (let [players (gamestate :players)]
    (assoc gamestate 
           :players (conj (subvec players 1) (first players)))))

(defn next-turn
  [gamestate]
  (-> gamestate
      (next-player)
      (assoc :awaiting :draw-or-knock)))

(defn update-hand-points
  [gamestate]
  (let [hand (-> gamestate :players first :hand)
        suits [:clubs :diamonds :hearts :spades]]
    (assoc-in gamestate [:players 0 :hand-points]
              (apply max (for [suit suits]
                           (->> hand
                                (filter #(= suit (:suit %)))
                                (map :value)
                                (apply +)))))))

(defn update-all-hand-points
  [gamestate]
  (if (empty? (filter #(zero? (% :hand-points)) (gamestate :players)))
    gamestate
    (recur (-> gamestate update-hand-points next-player))))

(defn draw-from-deck
  [gamestate]
  (let [deck (-> gamestate :deck)
        hand (-> gamestate :players first :hand)]
    (-> gamestate
        (assoc-in [:players 0 :hand] 
                  (conj hand (first deck)))
        (assoc :deck (rest deck))
        (assoc :awaiting :discard))))

(defn draw-from-discard
  [gamestate]
  (let [card (-> gamestate :discard)]
    (-> gamestate
        (assoc :discard nil)
        (update-in [:players 0 :hand] 
                   #(conj % card))
        (assoc :awaiting :discard))))

(defn discard
  [gamestate index]
  (let [hand (-> gamestate :players first :hand)
        card (hand index)]
    (-> gamestate
        (assoc :discard card)
        (assoc-in [:players 0 :hand] 
                   (vec (remove #{card} hand)))
        (update-hand-points)
        (assoc :awaiting :next-turn))))

(defn knock
  [gamestate]
  (-> gamestate
      (assoc :kocking-player (-> gamestate :players first))
      (assoc :awaiting :next-turn)))

(defn deal
  [gamestate]
  (loop [gs gamestate
         cards-remaining (* 3 (-> gamestate :players count))]
    (if (zero? cards-remaining)
      (-> gs
          (update-all-hand-points)
          (assoc-in [:discard] (-> gs :deck first))
          (assoc-in [:deck] (-> gs :deck rest))
          (assoc-in [:awaiting] :next-turn))
      (recur (-> gs
                 draw-from-deck
                 next-player)
             (dec cards-remaining)))))

(defn empty-hands
  [gamestate]
  (loop [gs gamestate]
    (if (empty? (-> gs :players first :hand))
      gs
      (recur (-> gs
                 (assoc-in [:players 0 :hand] [])
                 (assoc-in [:players 0 :hand-points] 0)
                 next-player)))))

(defn new-round
  "Shuffles a new deck, removes players who are no longer in the game, then deals"
  [gamestate]
  ;; (deal) sets the :awaiting and :discard, we don't need to change them here.
  (-> gamestate
      (assoc-in [:deck]
                (build-deck))
      
      (assoc-in [:knocking-player] 
                nil)
      
      (assoc-in [:players]
                (->> (gamestate :players)
                     (remove #(zero? (% :lives))) 
                     vec))))

(defn update-all-hand-points
  [gamestate]
  (if (empty? (filter #(zero? (% :hand-points)) (gamestate :players)))
    gamestate
    (recur (-> gamestate update-hand-points next-player))))

(defn blitzes
  [gamestate]
  (filter #(= 31 (% :hand-points)) (gamestate :players)))

(defn player-loses-point?
  [gamestate index]
  (let [lowscore (->> gamestate :players (map :hand-points) (apply min))
        highscore (->> gamestate :players (map :hand-points) (apply max))
        knocker (gamestate :knocking-player)
        player (-> gamestate :players (get index))
        points (player :hand-points)]
    (or (= points lowscore)
        (and (or (= player knocker) (= 31 highscore))
             (not= points highscore)))))

(defn losing-indexes
  [gamestate]
  (filterv #(player-loses-point? gamestate %) 
           (range (-> gamestate :players count))))

(defn players-losing-point
  [gamestate]
  (mapv #(-> gamestate :players (get %)) 
        (losing-indexes gamestate)))
