(ns web.scale)

(defn linear
  "map scale from domain to range"
  [dmin dmax rmin rmax]
  (let [m (/ (- rmax rmin) (- dmax dmin))
        c (- rmax (* m dmax))]
    (fn [value] (+ (* m value) c))))
