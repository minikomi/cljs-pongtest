(ns ecscljspong.components)

(deftype Velocity [^:mutable dx ^:mutable dy]
  IFn
  (-invoke [this kw]
    (case kw
      :dx dx
      :dy dy
      nil))
  Object
  (velocity-set [v dx' dy']
    (set! dx dx')
    (set! dy dy'))
  (bounce-x [_] (set! dx (- dx)))
  (bounce-y [_] (set! dy (- dy)))
  (gravity [_] (set! dy (inc dy))))

(deftype Position [^:mutable x ^:mutable y]
  IFn
  (-invoke [this kw]
    (case kw
      :x x
      :y y
      nil))
  Object
  (move-up [_]
    (set! y (- y 6)))
  (move-down [_]
    (set! y (+ y 6)))
  (set [_ x' y']
    (set! x x')
    (set! y y')))
