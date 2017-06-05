(ns simulation.ui
  (:require [rum.core :as rum]
            [#?(:cljs cljs.pprint
                :clj clojure.pprint) :as pprint]))


(rum/defc debug < rum/reactive [state]
  (let [state (rum/react state)]
    (when (:debug state)
      [:pre.debug (with-out-str (pprint/pprint state))])))


(rum/defc app < rum/reactive [state child]
  (let [state (rum/react state)]
    [:div
     [:canvas {:width (:width state)
               :height (:height state)}]
     (when (:running state)
       child)]))
