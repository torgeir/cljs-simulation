(ns simulation.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [taoensso.timbre :as timbre :refer-macros [info]]
            [rum.core :as rum]
            [cljs.reader :as reader]
            [cljs.core.async :as a]
            [goog.events :as events]
            [simulation.ui :as ui]
            [simulation.raf :as raf]
            [simulation.keys :as keys]
            [simulation.dom :refer [$]]
            [simulation.mouse :as mouse]
            [simulation.frame :as frame]
            [simulation.items :as items]
            [simulation.canvas :as canvas]
            [simulation.forces :as forces]
            [simulation.math :refer [half]]))


(defn render-item [ctx {:keys [type pos size]}]
  (let [w (:width size)
        h (:height size)
        x (- (:x pos) (half w))
        y (- (frame/height) (:y pos) (half w))]
    (if (= type :box)
      (canvas/draw-rect! ctx x y w h "#f3c")
      (canvas/draw-circle! ctx [(:x pos) (- (frame/height) (:y pos))] (half w) "#2cf"))))


(defn render-items [ctx items clear]
  (when clear
    (canvas/clear! ctx (frame/width) (frame/height)))
  (mapv #(render-item ctx %) items))


(defn render-loop [state canvas]
  (let [raf-chan (raf/chan state)]
    (go-loop []
      (a/<! raf-chan)
      (render-items (canvas/context canvas) (:items @state) (:clear @state))
      (recur))))


(defn server-state []
  (let [state (.-state js/window)]
    (reader/read-string state)))


(defn game-loop [state-ref]
  (go-loop [state @state-ref
            items-history (list (:items state))]
    (when (:running state)
      (let [[e chan] (a/alts! [keys/chan
                               mouse/chan
                               (a/timeout 16)])]
        (reset! state-ref (assoc state :items (first items-history)))
        (condp = chan
          keys/chan (condp = e
                      :toggle-clear (recur (update state :clear not) items-history)
                      :pause (recur (update state :paused not) items-history)
                      :prev  (recur state (drop 1 items-history))
                      :next  (recur state (conj items-history
                                                (mapv (forces/effects nil)
                                                      (first items-history))))
                      (recur state items-history))
          (let [pos (when (= chan mouse/chan) e)]
            (recur state (if (:paused state)
                           items-history
                           (conj items-history
                                 (mapv (forces/effects pos)
                                       (first items-history)))))))))))


(defn resize-canvas [el]
  (canvas/resize! el (frame/width) (frame/height)))


(defonce start
  (let [state (atom (server-state))
        canvas-el ($ "canvas")
        app-el ($ "#app")]

    (info "you're on")

    (rum/mount (ui/app state (ui/debug state)) app-el)

    (resize-canvas canvas-el)
    (events/listen js/window "resize"
                   #(resize-canvas canvas-el))
    
    (swap! state assoc
           :clear true
           :debug false
           :running true
           :items (doall (mapv #(items/create-item %) (range 100))))
    
    (game-loop state)
    (render-loop state canvas-el)))
