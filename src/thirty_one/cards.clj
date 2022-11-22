(ns thirty-one.cards)

(defn build-card
  [face suit]
  (let [card (case face
               :A {:face "A"
                   :value 11}
               :J {:face "J"
                   :value 10}
               :Q {:face "Q"
                   :value 10}
               :K {:face "K"
                   :value 10}
               {:face face
                :value face})]
    (assoc card 
           :suit suit 
           :name (str (card :face) " of " suit))))
      
;; I think I can do this with cool functional prog
(defn build-deck []
  (for [suit [:spades :hearts :diamonds :clubs]
        face (flatten [(range 2 (inc 9)) :A :J :Q :K])]
        (build-card face suit)))
    
