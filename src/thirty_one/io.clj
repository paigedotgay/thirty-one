(ns thirty-one.io
  "Input/Output tools, most of the game wants a io-handler function, see [[default-io-handler]] for an example"
  (:require [clojure.pprint :as pprint]
            [thirty-one.gamestate :as gs]
            [thirty-one.evaluator :as ev]))

;; (defn- get-draw-or-knock-option
;;   [{player-index :active-player
;;     discard :discard
;;     knocking-player :knocking-player
;;     :as gamestate}]
;;   (let [cards (ev/hand->str gamestate player-index)
;;         discard (-> gamestate :discard :name)
;;         str-out (str "Cards in hand: " cards
;;                        "\nWhat do you want to do?\n"
;;                        "[draw]: Draw a card from the deck\n"
;;                        "[discard]: Take " discard " from the discard pile\n"
;;                        (when-not knocking-player "[knock]: Knock"))]
;;     (do (println str-out)
;;         (case (read-line)
;;           "draw" (gs/draw-from-deck gamestate player-index)
;;           "discard" (gs/draw-from-discard gamestate player-index)
;;           "knock" (if knocking-player
;;                     (do (println "someone has already knocked.")
;;                         (recur gamestate))
;;                     (gs/knock gamestate player-index))
;;           (recur gamestate)))))

;; (defn- get-discard-index 
;;   [{player-index :active-player
;;     :as gamestate}]
;;   (let [cards (clojure.string/split (ev/hand->str gamestate player-index) #" ")
;;         str-out (apply format 
;;                        "Discard a card\n[1]: %s\n[2]: %s\n[3]: %s\n[4]: %s"
;;                        cards)]
;;     (do (println str-out)
;;         (if-let [selection ((->> (range 1 5) (map str) set) (read-line))]
;;           (gs/discard gamestate player-index (dec (Integer/parseInt selection)))
;;           (recur gamestate)))))

(defn get-actions
  "Returns a vec of actions you can take in the current gamestate. Use [[perform-action]] to do the action"
  [{awaiting :awaiting 
    player-index :active-player 
    discard :discard 
    knocking-player :knocking-player 
    :as gamestate}]
  (let [draw-options ["Draw from the Deck" (str "Draw " (discard :name))]
        cards (clojure.string/split (ev/hand->str gamestate player-index) #" ")]
    (case awaiting
      :draw-or-knock (if-not knocking-player
                       (conj draw-options "Knock")
                       draw-options)
      :discard (mapv #(str "Discard " %) cards)
      :start-turn [(str "Start "(-> gamestate :players (get (gs/next-player gamestate)) :name)"'s turn")]
      :end-round ["Continue"])))

(defn perform-action
  "Performs the action cooresponding to the index in the vec given by [[get-actions]]"
  [{awaiting :awaiting
    player-index :active-player
    discard :discard
    knocking-player :knocking-player
    :as gamestate} 
   action-index]
  ;; Ensure the action-index is an available option
  {:pre [((-> (get-actions gamestate) count range set) action-index)]}
  (case awaiting
    :draw-or-knock (#(% gamestate player-index) ;; Now *this* is janky
                    (case action-index
                      0 gs/draw-from-deck
                      1 gs/draw-from-discard
                      2 gs/knock))
    :discard (gs/discard player-index action-index)
    :start-turn (assoc gamestate :awaiting :draw-or-knock)
    :end-round (-> gamestate (gs/new-round) (assoc :awaiting :start-turn))))


;; Everything below here is an example of what an io-handler might look like

(defn- print-round-summary
  [gamestate]
  (do (println (format "The following players lose a point:\n\t%s"
                       (clojure.string/join " " (ev/losing-names gamestate))))
      (pprint/print-table [:name :hand-points :lives] (gamestate :players))))

(defn default-io-handler
  "An example of how to interface with the library."
  [{player-index :active-player
    players :players
    :as gamestate}]
  (let [player-name (-> players (get player-index) :name)]
    (case (gamestate :awaiting)
      (:discard :draw-or-knock)  (do (println "Your Hand: " (ev/hand->str gamestate player-index))
                                     (println (clojure.string/join "\n" (zipmap (range 4) (get-actions gamestate))))
                                     (perform-action gamestate (Integer/parseInt (read-line))))
      :start-turn (do (println "\033[2J Press Enter to begin"player-name"'s turn")
                      (read-line) 
                      (perform-action gamestate 0))
      :end-round (do (print"\033[2J") ;; ANSI to clear the terminal.
                     (print-round-summary gamestate)
                     (println "Press ENTER to continue")
                     (read-line)
                     (perform-action gamestate 0)))))
