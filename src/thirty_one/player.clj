(ns thirty-one.player)

(defn draw-card-from-deck
  [gamestate]
  (let [card (-> gamestate :deck first)
        deck (-> gamestate :deck rest)
        hand (-> gamestate :players first :hand)]
    (-> gamestate
        (assoc-in [:players 0 :hand] 
                  (conj hand card))
        (assoc-in [:deck]
                  deck))))

(defn draw-card-from-discard
  [gamestate]
  (let [card (-> gamestate :discard)]
    (-> gamestate
        (assoc-in [:discard] nil)
        (update-in [:players 0 :hand] 
                   #(conj % card)))))

(defn select-card
  [gamestate card-index]
  (let [player (gamestate :active-player)]
    (-> gamestate
        (assoc-in [:selected-card]
                  (-> gamestate :players (get player) :hand (get 0))))))

(defn discard
  [gamestate]
  (let [player (gamestate :active-player)
        card (gamestate :selected-card)]
    (println (str player "\n" card))
    (-> gamestate
        (assoc-in [:discard] 
                  card)
        (assoc-in [:selected-card]
                  nil)
        (update-in [:players player :hand] 
                   #(remove #{card} %)))))
