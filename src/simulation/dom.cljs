(ns simulation.dom)


(defn $ [sel]
  (.querySelector js/document sel))
