(ns thirty-one.evaluator
  "Gives info about the gamestate")

(defn hand->str
  "Returns a space-seperated string of each card in the hand of the player at `player-index`"
  [gamestate player-index]
  (clojure.string/join 
   " " 
   (mapv :name (-> gamestate :players (get player-index) :hand))))

(defn blitzing-players
  "Returns players who have 31 points in hand"
  [gamestate]
  (filter #(= 31 (% :hand-points)) (gamestate :players)))

(defn time-to-score?
  "Returns `true` if the game should move to the scoring phase"
  [{active-player :active-player
    knocking-player :knocking-player
    deck :deck
    :as gamestate}]
  (boolean (or (seq (blitzing-players gamestate)) 
               (= active-player knocking-player)
               (empty? deck))))

(defn player-loses-life?
  "Returns `true` the player at player-index should lose a life"
  [gamestate player-index]
  (let [lowscore (->> gamestate :players (map :hand-points) (apply min))
        highscore (->> gamestate :players (map :hand-points) (apply max))
        knocker (gamestate :knocking-player)
        player (-> gamestate :players (get player-index))
        points (player :hand-points)]
    (boolean (or (= points lowscore)
                 (and (or (= player knocker) (= 31 highscore))
                      (not= points highscore))))))

(defn losing-indexes
  "Indexes of any player who should lose a life during scoring"
  [gamestate]
  (filterv #(player-loses-life? gamestate %) 
           (range (-> gamestate :players count))))

(defn losing-names
  "Names of any player who should lose a life during scoring"
  [gamestate]
  (let [players (gamestate :players)
        losers (map players (losing-indexes gamestate))]
    (map :name losers)))
