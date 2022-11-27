(ns thirty-one.input
  (:use [thirty-one.gamestate]))

(defn regex-name
  "too sleepy to actually do regex"
  [name]
  (clojure.string/replace 
   name 
   #"\sof\s|:clubs|:diamonds|:hearts|:spades"
   {":clubs" "♣"
    ":diamonds" "♦"
    ":hearts" "♥"
    ":spades" "♠"
    " of " ""}))

(defn- get-draw-or-knock-option
  [gamestate]
  (let [cards (->> gamestate :players first :hand (map (comp regex-name :name))(clojure.string/join " "))
        discard (-> gamestate :discard :name)
        str-out (str "Cards in hand: " cards
                     "\nWhat do you want to do?\n"
                     "[draw]: Draw a card from the deck\n"
                     "[discard]: Take " (regex-name discard) " from the discard pile\n"
                     "[knock]: Knock")]
    (do (println str-out)
        (case (read-line)
          "draw" (draw-from-deck gamestate)
          "discard" (draw-from-discard gamestate)
          "knock" (knock gamestate)
          (recur gamestate)))))

(defn get-discard-index 
  [gamestate]
  (let [cards (->> gamestate :players first :hand (map (comp regex-name :name)))
        str-out (apply format 
                       "Discard a card\n[1]: %s\n[2]: %s\n[3]: %s\n[4]: %s"
                       cards)]
    (do (println str-out)
        (if-let [selection ((->> (range 1 5) (map str) set) (read-line))]
          (discard gamestate (dec (Integer/parseInt selection)))
          (recur gamestate)))))

(defn round-summary
  [gamestate]
  (loop [s ""
         players (-> gamestate :players)]
    (if (empty? players)
      (format "%s\nThe following players lose a point:"
              s
              (filter player-loses-point? (repeat
              (map
      (recur (format "%s\n%s has %d points in hand:\n\t%s"
                     s
                     (-> players first :name)
                     (-> players first :hand-points)
                     (->> players first :hand (map (comp regex-name :name))(clojure.string/join " "))) (rest players)))))


(defn round-end
  [gamestate]
  ())
(defn default-input-handler
  ([gamestate]
   (case (gamestate :awaiting)
     :draw-or-knock  (get-draw-or-knock-option gamestate)
     :discard (get-discard-index gamestate)
     :next-turn (do (println "Press Enter to begin "(-> gamestate :players first :name)"'s turn")
                    (read-line)
                    (next-turn gamestate))
     :next-round (round-end gamestate)))
  ([gamestate status]
   (-> gamestate
       (assoc :awaiting status)
       (default-input-handler))))
