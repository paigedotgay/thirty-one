(ns thirty-one.gamestate
  (:use [thirty-one.cards]))

(defn new-gamestate
  "Creates the basic skeleton of the gamestate, including a shuffled deck."
  []
  {:awaiting nil
   :deck (shuffle (build-deck))
   :discard nil
   :knocking-player nil
   :players []
   :message "Setting up"})

(defn add-player
  [gamestate player-name]
  (-> gamestate
      (assoc :message (format "Adding %s to the game" 
                                 player-name))
      (update-in [:players] 
                 #(conj % {:name player-name
                           :hand []
                           :hand-points 0
                           :lives 5}))))
  
(defn update-hand-points
  [gamestate player-index]
  (let [hand (-> gamestate :players (get player-index) :hand)
        suits [:clubs :diamonds :hearts :spades]
        max-points (apply max (for [suit suits]
                               (->> hand
                                    (filter #(= suit (:suit %)))
                                    (map :value)
                                    (apply +))))]
    (-> gamestate
        (assoc :message (format "Updating %s's points to %d"
                                (-> gamestate :players (get player-index) :name)
                                max-points))
        (assoc-in [:players player-index :hand-points]
                  max-points))))

(defn update-all-hand-points
  ([gamestate]
   (update-all-hand-points (assoc gamestate :message "Updating all points")
                           0))
  
  ([gamestate player-index]
   (if (= player-index (-> gamestate :players count))
     gamestate
     (recur (update-hand-points gamestate player-index) 
            (inc player-index)))))

(defn draw-from-deck
  [gamestate player-index]
  (let [deck (-> gamestate :deck)
        hand (-> gamestate :players (get player-index) :hand)]
    (-> gamestate
        (assoc :message (format "%s drew a card from the deck"
                                (-> gamestate :players (get player-index) :name)))
        (assoc-in [:players player-index :hand] 
                  (conj hand (first deck)))
        (assoc :deck (rest deck))
        (assoc :awaiting :discard))))

(defn draw-from-discard
  [gamestate player-index]
  (let [card (-> gamestate :discard)]
    (-> gamestate
        (assoc :message (format "%s drew %s from the discard"
                                (-> gamestate :players (get player-index) :name)
                                (regex-name card)))
        (assoc :discard nil)
        (update-in [:players player-index :hand] 
                   #(conj % card))
        (assoc :awaiting :discard))))

(defn discard
  [gamestate player-index card-index]
  (let [hand (-> gamestate :players (get player-index) :hand)
        card (hand card-index)]
    (-> gamestate
        (assoc :discard card)
        (assoc-in [:players player-index :hand] 
                  (vec (remove #{card} hand)))
        (update-hand-points player-index)
        (assoc :awaiting :next-turn))))

(defn knock
  [gamestate player-index]
  {:pre [(nil? (gamestate :knocking-player))]}
  (-> gamestate
      (assoc :knocking-player (-> gamestate :players (get player-index)))
      (assoc :awaiting :next-turn)))
  
(defn deal
  [gamestate]
  (loop [gs gamestate
         deal-order (->> gamestate :players count range (repeat 3) flatten)]
    (if (empty? deal-order)
      (-> gs
          (update-all-hand-points)
          (assoc-in [:discard] (-> gs :deck first))
          (assoc-in [:deck] (-> gs :deck rest))
          (assoc-in [:awaiting] :next-turn))
      (recur (draw-from-deck gs (first deal-order))
             (rest deal-order)))))

(defn empty-hands
  ([gamestate] (empty-hands gamestate 0))
  ([gamestate player-index]
   (if (= (-> gamestate :players count)
          player-index)
     gamestate
     (recur (assoc-in gamestate [:players player-index :hand] []) 
            (inc player-index)))))
  

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
