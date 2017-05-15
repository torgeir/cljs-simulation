(ns simulation.log)

(defmacro log [& args]
  `(.log js/console ~@args))

(defmacro info [& args]
  `(.info js/console ~@args))