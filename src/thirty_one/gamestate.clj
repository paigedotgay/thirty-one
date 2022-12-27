(ns thirty-one.gamestate
  "Builds and edits the gamestate"
  (:require [thirty-one.deck :as deck]
            [thirty-one.evaluator :as ev]))

(defn new-gamestate
  "Creates the basic skeleton of the gamestate, including a shuffled deck."
  []
  {:active-player 0
   :awaiting nil
   :deck (deck/build-deck)
   :discard nil
   :round 0
   :knocking-player nil
   :message "Setting up"
   :players []})


(defn dec-life
  [gamestate player-index]
  (update-in gamestate [:players player-index :lives] dec))

(defn score
  [gamestate]
  (println "Scoring the game now...")
  (loop [gs (assoc gamestate :awaiting :end-round)
         losers (ev/losing-indexes gamestate)]
    (if (empty? losers)
      gs
      (recur (dec-life gs (first losers))
             (rest losers)))))


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
(defn next-player
  [gamestate]
  (if (= (gamestate :active-player) 
         (dec (count (gamestate :players))))
    0
    (inc (gamestate :active-player))))

  
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
  ([gamestate] (update-all-hand-points gamestate 0))
  ([gamestate player-index]
   (if (= player-index (-> gamestate :players count))
     gamestate
     (recur (update-hand-points gamestate player-index) 
            (inc player-index)))))

(defn draw-from-deck
  [gamestate player-index]
  (let [deck (gamestate :deck)
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
                                (:name card)))
        (assoc :discard nil)
        (update-in [:players player-index :hand] 
                   #(conj % card))
        (assoc :awaiting :discard))))

(defn discard
  [gamestate player-index card-index]
  (let [hand (-> gamestate :players (get player-index) :hand)
        card (hand card-index)
        gs (-> gamestate
               (assoc :discard card)
               (assoc-in [:players player-index :hand] 
                         (vec (remove #{card} hand)))
               (update-hand-points player-index)
               (assoc :active-player (next-player gamestate)))]
    (if (ev/time-to-score? gs)
      (score gs)
      (assoc gs :awaiting :start-turn))))

(defn knock
  [gamestate player-index]
  {:pre [(nil? (gamestate :knocking-player))]}
  (-> gamestate
      (assoc :knocking-player player-index)
      (assoc :active-player (next-player gamestate))
      (assoc :awaiting :start-turn)))
  
(defn deal
  [gamestate]
  (loop [gs gamestate
         deal-order (->> gamestate :players count range (repeat 3) flatten)]
    (if (empty? deal-order)
      (-> gs
          (update-all-hand-points)
          (assoc-in [:discard] (-> gs :deck first))
          (assoc-in [:deck] (-> gs :deck rest))
          (assoc-in [:awaiting] :start-turn))
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
  (as-> gamestate gs
      (assoc gs :deck (deck/build-deck))
      (assoc gs :knocking-player nil)
      (assoc-in gs [:players]
                (->> (gs :players)
                     (remove #(zero? (% :lives))) 
                     vec))
      (update gs :round inc)
      (empty-hands gs)
      (deal gs)
      (assoc gs :active-player (mod (gs :round) (count (gs :players))))))

