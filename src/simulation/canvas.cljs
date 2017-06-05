(ns simulation.canvas)


(defn resize! [el w h]
  (set! (-> el .-width) w)
  (set! (-> el .-height) h)
  el)


(defn context [el]
  (.getContext el "2d"))


(defn clear! [ctx w h]
  (.clearRect ctx 0 0 w h))


(defn draw-circle! [ctx [x y] r color]
  (set! (-> ctx .-fillStyle) color)
  (let [circle (js/Path2D.)]
    (.arc circle x y r 0 (* 2 (.-PI js/Math)))
    (.fill ctx circle)))


(defn draw-rect! [ctx x y w h color]
  (set! (-> ctx .-fillStyle) color)
  (.fillRect ctx x y w h))