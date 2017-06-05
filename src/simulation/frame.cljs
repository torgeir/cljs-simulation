(ns simulation.frame
  (:require [simulation.dom :refer [$]]
            [simulation.math :refer [half]]))


(def parent ($ "body"))


(defn width []
  (-> parent .-clientWidth))


(defn height []
  (-> parent .-clientHeight))