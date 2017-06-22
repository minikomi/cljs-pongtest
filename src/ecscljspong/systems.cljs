(ns ecscljspong.systems
  (:require [ecscljspong.util :as u]
            [ecscljspong.events :as ev]
            [ecscljspong.entities :as ent]))

(defn process-events [em]
  (let [stage (u/get-global em :stage)
        current-events @ev/event-bus]
    (doseq [[ev-type data] @ev/event-bus]
      (when-let [h (ev/event-handlers ev-type)]
        (h em data)))
    (vreset! ev/event-bus [])))

(defn handle-inputs [em]
  (let [held? @ev/keys-held
        gs (u/get-global em :game-state)]
    (case @gs
      :playing
      (do
        (when (held? "ArrowUp")
          (let [p (first (u/query-tag em :player))
                pos (u/get-component p :position)]
            (when (< 0 (pos :y))
              (.move-up pos))))
        (when (held? "ArrowDown")
          (let [p (first (u/query-tag em :player))
                pos (u/get-component p :position)]
            (when (> (u/get-height em) (pos :y))
              (.move-down pos)))))
      (do
        (when (held? " ")
          (let [ball (first (u/query-tag em :ball))
                w (u/get-width em)
                h (u/get-height em)]
            (.set (u/get-component ball :velocity)
                  (if (> 0.5 (rand)) -4 4)
                  (- (rand-int 8) 4))
            (.set (u/get-component ball :position) (/ w 2) (/ h 2))
            (reset! gs :playing)))))))

(defn check-ball-position [em]
  (let [gs (u/get-global em :game-state)
        ball (first (u/query-tag em :ball))
        pos (u/get-component ball :position)
        vel (u/get-component ball :velocity)
        w (u/get-width em)
        h (u/get-height em)]
    (when (or (>= 0 (pos :y))
              (<= h (pos :y)))
      (set! (.-y pos) (u/clamp (pos :y) 0 h))
      (.bounce-y vel))
    (when (<= w (pos :x))
      (reset! gs :you-lose))
    (when (>= 0 (pos :x))
      (reset! gs :you-win))))

(defn check-paddle-collision [em])

(defn move-ball [em]
  (let [gs (u/get-global em :game-state)]
    (when (= :playing @gs)
      (let [ball (first (u/query-tag em :ball))
            pos (u/get-component ball :position)
            vel (u/get-component ball :velocity)]
        (.set pos (+ (vel :dx) (pos :x))
                  (+ (vel :dy) (pos :y)))))))

(defn ai-paddle [em])

(defn render-scene [em]
  (let [gs @(u/get-global em :game-state)
        renderer (u/get-global em :renderer)
        stage (u/get-global em :stage)]
    (doseq [e (u/query-components em [:circ])]
      (let [c (u/get-component e :circ)
            pos (u/get-component e :position)]
        (.set (.-position c) (pos :x) (pos :y))))
    (doseq [e (u/query-components em [:rect])]
      (let [r (u/get-component e :rect)
            pos (u/get-component e :position)]
        (.set (.-position r) (pos :x) (pos :y))))
    (.render renderer stage)))

(defn run-systems [em]
  (doto em
    process-events
    handle-inputs
    check-ball-position
    move-ball
    render-scene))
