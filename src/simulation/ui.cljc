(ns simulation.ui
  (:require [rum.core :as rum]))

(rum/defc app < rum/reactive [state child]
  [:div
   [:canvas]
   (let [state (rum/react state)]
     (when (:running state)
       child))])
