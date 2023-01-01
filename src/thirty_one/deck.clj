(ns thirty-one.deck
  "builds cards and decks")

(defn build-card
  "Adds important info to a card, including the value and a pretty version of the suit"
  [face suit]
  (-> {:face face :suit suit}
      (assoc :value
             (case face
               (:J :Q :K) 10
               :A 11
               face)
      
             :name 
             (str (if (keyword? face)
                    (name face)
                    face)
                  
                  (case suit
                    :clubs "♣"
                    :diamonds "♦"
                    :hearts "♥"
                    :spades "♠")))))

(defn build-deck 
  "Builds and shuffles a deck of cards."
  []
  (shuffle (for [face (into [:J :Q :K :A] (range 2 11))
                 suit [:clubs :diamonds :hearts :spades]]
             (build-card face suit))))
