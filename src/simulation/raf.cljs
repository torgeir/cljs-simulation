(ns simulation.raf
  (:require [cljs.core.async :as a]))


(defn chan [state]
  (let [c (a/chan (a/sliding-buffer 1))]
    (letfn [(animation-loop []
              (if (:running @state)
                (do
                  (a/put! c true)
                  (js/window.requestAnimationFrame animation-loop))
                (a/close! c)))]
      (js/window.requestAnimationFrame animation-loop))
    c))
