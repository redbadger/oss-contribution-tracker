(ns web.scale)

(defn linear
  "map linear scale from domain to range"
  [dmin dmax rmin rmax]
  (let [m (/ (- rmax rmin) (- dmax dmin))
        c (- rmax (* m dmax))]
    (fn [value] (+ (* m value) c))))

(defn ordinal
  "map ordinal scale from domain to range"
  [values rmin rmax]
  (let [dmax (- (count values) 1)
        m (/ (- rmax rmin) dmax)
        c (- rmax (* m dmax))]
    (fn [value]
      (let [index (max 0 (.indexOf values value))]
        (+ (* m index) c)))))
