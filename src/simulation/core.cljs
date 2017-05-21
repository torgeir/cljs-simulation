(ns simulation.core
  (:require-macros [simulation.log :refer [log]]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require [rum.core :as rum]
            [simulation.ui :as ui]
            [cljs.reader :as reader]
            [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :as a]))

(defn $ [sel]
  (.querySelector js/document sel))

(def parent ($ "body"))

(defn half [x] (/ x 2))

(def frame-size
  {:width  (-> parent .-clientWidth)
   :height (-> parent .-clientHeight)})

(defn invert-y [y]
  (- (:height frame-size) y))

(def frame-center
  {:x (half (:width frame-size))
   :y (half (:height frame-size))})

(defn server-state []
  (let [state (.-state js/window)]
    (reader/read-string state)))

(rum/defc debug < rum/reactive [state]
  (let [state (rum/react state)]
    (when (:debug state)
      [:pre.debug
       (with-out-str (cljs.pprint/pprint state))])))

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
  {:x (rand-int (:width frame-size))
   :y (rand-int (- (:height frame-size) 100))})

(defmulti with-friction :type)
(defmethod with-friction :box [item] (assoc item :friction 0.1))
(defmethod with-friction :circle [item] (assoc item :friction 1))

(defn create-item [index]
  (with-friction
    {:key  (str "item-" index)
     :type (rand-type)
     :size (rand-size)
     :vel  (rand-vel)
     :pos  (rand-pos)}))

(defn mass [item]
  (* 0.001
     (get-in item [:size :height])
     (get-in item [:size :width])))

(defonce mouse-events (a/chan))

(defn mouse-event-map [e]
  {:x (max 0 (.-clientX e))
   :y (max 0 (- (:height frame-size)
                (.-clientY e)))})

(defonce mouse-click-listener
  (events/listen parent "click" #(a/put! mouse-events (mouse-event-map %))))

(defonce mouse-move-listener
  (events/listen parent "mousemove" #(a/put! mouse-events (mouse-event-map %))))

(def forces
  {:gravity  0.5
   :wind     0.05
   :bounce  -0.5})

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
    (if (> (+ x w) (:width frame-size))
      (-> item
        (update-in [:vel :x] (partial * (:bounce forces)))
        (assoc-in [:pos :x] (- (:width frame-size) w)))
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
      (assoc :vel (rand-vel))
      (assoc :pos mouse-event))))

(defn friction [f item]
  (let [y (get-in item [:pos :y])
        h (half (get-in item [:size :height]))]
    (if (zero? (- y h))
      (update-in item [:vel :x] (partial * (or (:friction item) f)))
      item)))

(defn wind [w item]
  (update-in item [:vel :x] #(+ % w)))

(defn gravity [g item]
  (update-in item [:vel :y] #(- % (* g (mass item)))))

(defn effects [mouse-event]
  (comp
    constrain-min-x
    constrain-max-x
    constrain-min-y
    move
    (partial mouse mouse-event)
    (partial friction (:friction forces))
    (partial wind (:wind forces))
    (partial gravity (:gravity forces))))

(defn raf-chan [state]
  (let [c (a/chan (a/sliding-buffer 1))]
    (letfn [(animation-loop []
              (if (:running @state)
                (do
                  (a/put! c true)
                  (js/window.requestAnimationFrame animation-loop))
                (a/close! c)))]
      (js/window.requestAnimationFrame animation-loop))
    c))

(defn game-loop [state]
  (go-loop []
    (when (:running @state)
      (let [[mouse-event chan] (a/alts! [mouse-events (a/timeout 10)])]
        (swap! state update :items
               (fn [items]
                 (mapv
                   (effects (when (= chan mouse-events) mouse-event))
                   items))))
      (recur))))

(defn clear-canvas! [ctx w h]
  (.clearRect ctx 0 0 w h))

(defn draw-circle! [ctx pos r color]
  (set! (-> ctx .-fillStyle) color)
  (let [circle (js/Path2D.)
        x (:x pos)
        y (- (:height frame-size) (:y pos))]
    (.arc circle x y r 0 (* 2 (.-PI js/Math)))
    (.fill ctx circle)))

(defn draw-rect! [ctx x y w h color]
  (set! (-> ctx .-fillStyle) color)
  (.fillRect ctx x y w h))

(defn render-item [ctx {:keys [type pos size]}]
  (let [w (:width size)
        h (:height size)
        x (- (:x pos) (half w))
        y (- (:height frame-size) (:y pos) (half w))]
    (if (= type :box)
      (draw-rect! ctx x y w h "#f3c")
      (draw-circle! ctx pos (half w) "#2cf"))))

(defn render-items [ctx items]
  (clear-canvas! ctx (:width frame-size) (:height frame-size))
  (mapv #(render-item ctx %) items))

(defn run [state]
  (let [rc (raf-chan state)
        canvas (let [el ($ "canvas")]
                 (set! (-> el .-height) (:height frame-size))
                 (set! (-> el .-width) (:width frame-size))
                 el)
        ctx (.getContext canvas "2d")]

    (go-loop []
      (a/<! rc)
      (render-items ctx (:items @state))
      (recur))))

(defonce start
  (do
    (defonce state (atom (server-state)))

    (rum/mount (ui/app state (debug state)) ($ "#app"))

    (swap! state assoc
           :debug false
           :running true
           :items (doall (mapv #(create-item %) (range 100))))

    (run state)
    (game-loop state)
    ))