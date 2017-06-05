(ns simulation.forces
  (:require [simulation.items :as items]
            [simulation.frame :as frame]
            [simulation.math :refer [half]]))


(def forces
  {:gravity  0.1
   :wind     0.05
   :bounce  -0.1 })


(defn constrain-min-x [item]
  (let [x (get-in item [:pos :x])
        w (half (get-in item [:size :width]))]
    (if (< x w)
      (-> item
        (update-in [:vel :x] (partial * (:bounce forces)))
        (assoc-in [:pos :x] w))
      item)))


(defn constrain-max-x [item]
  (let [x (get-in item [:pos :x])
        w (half (get-in item [:size :width]))]
    (if (> (+ x w) (frame/width))
      (-> item
        (update-in [:vel :x] (partial * (:bounce forces)))
        (assoc-in [:pos :x] (- (frame/width) w)))
      item)))


(defn constrain-min-y [item]
  (let [y (get-in item [:pos :y])
        h (half (get-in item [:size :height]))]
    (if (< (- y h) 0)
      (-> item
        (update-in [:vel :y] (partial * (:bounce forces)))
        (assoc-in [:pos :y] h))
      item)))


(defn move [item]
  (let [{:keys [x y]} (:vel item)]
    (-> item
      (update-in [:pos :x] #(+ % x))
      (update-in [:pos :y] #(+ % y)))))


(defn mouse [mouse-event item]
  (if (not mouse-event)
    item
    (-> item
      (assoc :vel (items/rand-vel))
      (assoc :pos mouse-event))))


(defn friction [f item]
  (let [y (get-in item [:pos :y])
        h (half (get-in item [:size :height]))]
    (if (zero? (- y h))
      (update-in item [:vel :x] (partial * (or (:friction item) f)))
      item)))


(defn wind [w item]
  (update-in item [:vel :x] #(+ % w)))


(defn mass [item]
  (* 0.001
     (get-in item [:size :height])
     (get-in item [:size :width])))


(defn gravity [g item]
  (update-in item [:vel :y] #(- % (* g (mass item)))))


(defn effects [mouse-event]
  (comp
    constrain-min-x
    constrain-max-x
    constrain-min-y
    move
    (partial mouse mouse-event)
    (partial wind     (:wind forces))
    (partial friction (:friction forces))
    (partial gravity  (:gravity forces))))