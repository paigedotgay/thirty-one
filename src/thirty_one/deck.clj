(ns thirty-one.deck)

(defn build-card
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

(defn build-deck []
  (shuffle (for [face (into [:J :Q :K :A] (range 2 11))
                 suit [:clubs :diamonds :hearts :spades]]
             (build-card face suit)))
