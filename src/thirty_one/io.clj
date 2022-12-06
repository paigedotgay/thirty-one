(ns thirty-one.io
  (:require [clojure.pprint :as pprint]
            [thirty-one.gamestate :as gs]
            [thirty-one.evaluator :as ev]))

(defn- get-draw-or-knock-option
  [{player-index :active-player
    discard :discard
    knocking-player :knocking-player
    :as gamestate}]
  (let [cards (ev/hand->str gamestate player-index)
        discard (-> gamestate :discard :name)
        str-out (str "Cards in hand: " cards
                       "\nWhat do you want to do?\n"
                       "[draw]: Draw a card from the deck\n"
                       "[discard]: Take " discard " from the discard pile\n"
                       (when-not knocking-player "[knock]: Knock"))]
    (do (println str-out)
        (case (read-line)
          "draw" (gs/draw-from-deck gamestate player-index)
          "discard" (gs/draw-from-discard gamestate player-index)
          "knock" (if knocking-player
                    (do (println "someone has already knocked.")
                        (recur gamestate))
                    (gs/knock gamestate player-index))
          (recur gamestate)))))

(defn- get-discard-index 
  [{player-index :active-player
    :as gamestate}]
  (let [cards (clojure.string/split (ev/hand->str gamestate player-index) #" ")
        str-out (apply format 
                       "Discard a card\n[1]: %s\n[2]: %s\n[3]: %s\n[4]: %s"
                       cards)]
    (do (println str-out)
        (if-let [selection ((->> (range 1 5) (map str) set) (read-line))]
          (gs/discard gamestate player-index (dec (Integer/parseInt selection)))
          (recur gamestate)))))

(defn- round-summary
  [gamestate]
  (do (println (format "The following players lose a point:\n\t%s"
                       (clojure.string/join " " (ev/losing-names gamestate))))
      (pprint/print-table [:name :hand-points :lives] (gamestate :players))
      (println "Press ENTER to continue")
      (read-line)
      (-> gamestate
          (gs/new-round)
          (assoc :awaiting :start-turn))))

(defn default-input-handler
  "An example of how to interface with the library. Need to rewrite it to use the below fs"
  [{player-index :active-player
    players :players
    :as gamestate}]
  (let [player-name (-> players (get player-index) :name)]
    (case (gamestate :awaiting)
      :draw-or-knock  (get-draw-or-knock-option gamestate)
      :discard (get-discard-index gamestate)
      :start-turn (do (println "\033[2J Press Enter to begin"player-name"'s turn")
                      (read-line) 
                      (assoc gamestate 
                             :awaiting :draw-or-knock))
      :end-round (do (print"\033[2J") ;; ANSI to clear the terminal.
                     (round-summary gamestate)))))
  
(defn get-actions
  "Returns a vec of descriptions of actions you can take in the current gamestate."
  [{awaiting :awaiting 
    player-index :active-player 
    discard :discard 
    knocking-player :knocking-player 
    :as gamestate}]
  (let [draw-knock-options ["Draw from the Deck" (str "Draw " (discard :name))]
        cards (clojure.string/split (ev/hand->str gamestate player-index) #" ")]
    (case awaiting
      :draw-or-knock (if-not knocking-player
                       (conj draw-knock-options "Knock")
                       draw-knock-options)
      :discard (mapv #(str "Discard " %) cards))
      {0 "Continue"}))

(defn perform-action
  "performs the action cooresponding to the index in the vec given by get-actions"
  [{awaiting :awaiting
    player-index :active-player
    discard :discard
    knocking-player :knocking-player
    :as gamestate} 
   action-index]
  {:pre [((set (keys (get-actions gamestate))) action-index)]}
  (case awaiting
    :draw-or-knock (#(% gamestate player-index) ;; Now *this* is janky
                    (case action-index
                      0 gs/draw-from-deck
                      1 gs/draw-from-discard
                      2 gs/knock))
    :discard (gs/discard player-index action-index)
    :start-turn (assoc gamestate :awaiting :draw-or-knock)
    :end-round (-> gamestate (gs/new-round) (assoc :awaiting :start-turn))))
