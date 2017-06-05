(ns simulation.keys
  (:require [simulation.frame :as frame]
            [cljs.core.async :as a]
            [goog.events :as events])) 


(defonce chan (a/chan))


(defonce listener
  (events/listen frame/parent "keydown"
                 #(a/put! chan
                          (case (.-keyCode %)
                            32 :pause
                            37 :prev
                            39 :next
                            67 :toggle-clear
                            :unhandled))))

