(ns simulation.mouse
  (:require [cljs.core.async :as a]
            [goog.events :as events]
            [simulation.frame :as frame])) 


(defonce chan (a/chan))


(defn mouse-event-map [e]
  {:x (max 0 (.-clientX e))
   :y (max 0 (- (frame/height) (.-clientY e)))})


(defonce click-listener
  (events/listen frame/parent "click"
                 #(a/put! chan (mouse-event-map %))))