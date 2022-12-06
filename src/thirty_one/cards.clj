(ns thirty-one.cards)

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
                (flatten (repeat 4 [(range 2 (inc 10)) :J :Q :K :A]))
                (cycle [:spades :hearts :diamonds :clubs]))))
