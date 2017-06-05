(ns simulation.items
  (:require [simulation.frame :as frame]))


(defn rand-type []
  (rand-nth [:box :circle]))


(defn rand-size []
  (let [x (rand-int 40)]
    {:width x
     :height x}))


(defn rand-vel []
  {:x (rand-nth (range -40 40))
   :y (rand-nth (range -20 40))})


(defn rand-pos []
  {:x (rand-int (frame/width))
   :y (rand-int (- (frame/height) 100))})


(defmulti with-friction :type)
(defmethod with-friction :box    [item] (assoc item :friction 0.1))
(defmethod with-friction :circle [item] (assoc item :friction 1))


(defn create-item [index]
  (with-friction
    {:key  (str "item-" index)
     :type (rand-type)
     :size (rand-size)
     :vel  (rand-vel)
     :pos  (rand-pos)}))
