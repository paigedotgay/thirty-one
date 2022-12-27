(ns thirty-one.evaluator
  "Gives info about the gamestate")

(defn hand->str
  [gamestate player-index]
  (clojure.string/join 
   " " 
   (mapv :name (-> gamestate :players (get player-index) :hand))))

(defn blitzes
  [gamestate]
  (filter #(= 31 (% :hand-points)) (gamestate :players)))

(defn time-to-score?
  [{active-player :active-player
    knocking-player :knocking-player
    deck :deck
    :as gamestate}]
   (or (seq (blitzes gamestate)) 
       (= active-player knocking-player)
       (empty? deck)))

(defn player-loses-life?
  [gamestate player-index]
  (let [lowscore (->> gamestate :players (map :hand-points) (apply min))
        highscore (->> gamestate :players (map :hand-points) (apply max))
        knocker (gamestate :knocking-player)
        player (-> gamestate :players (get player-index))
        points (player :hand-points)]
    (or (= points lowscore)
        (and (or (= player knocker) (= 31 highscore))
             (not= points highscore)))))

(defn losing-indexes
  [gamestate]
  (filterv #(player-loses-life? gamestate %) 
           (range (-> gamestate :players count))))

(defn losing-names
  [gamestate]
  (let [players (gamestate :players)
        losers (map players (losing-indexes gamestate))]
    (map :name losers)))
