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
  (shuffle (map build-card
                (take 52 (cycle (into [:J :Q :K :A] (range 2 11))))
                (cycle [:spades :hearts :diamonds :clubs]))))
