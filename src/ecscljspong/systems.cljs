(ns ecscljspong.systems
  (:require [ecscljspong.util :as u]
            [ecscljspong.events :as ev]
            [ecscljspong.entities :as ent]))

;; Events
;; --------------------------------------------------------------------

(defn process-events [em]
  (let [stage (u/get-global em :stage)
        current-events @ev/event-bus]
    (doseq [[ev-type data] @ev/event-bus]
      (when-let [h (ev/event-handlers ev-type)]
        (h em data)))
    (vreset! ev/event-bus [])))

;; Inputs
;; --------------------------------------------------------------------

(defn handle-playing-inputs [held? em]
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

(defn handle-default-inputs [held? gs em]
  (when (held? " ")
    (let [ball (first (u/query-tag em :ball))
          w (u/get-width em)
          h (u/get-height em)]
      (.set (u/get-component ball :velocity)
            (if (> 0.5 (rand)) -4 4)
            (- (rand-int 8) 4))
      (.set (u/get-component ball :position) (/ w 2) (/ h 2))
      (reset! gs :playing))))

(defn handle-inputs [em]
  (let [held? @ev/keys-held
        gs (u/get-global em :game-state)]
    (case @gs
      :playing (handle-playing-inputs held? em)
      (handle-default-inputs held? gs em))))

;; Bounce / check win
;; --------------------------------------------------------------------

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

;; Check paddle reflect
;; --------------------------------------------------------------------

(defn check-collision [b p]
  (let [;; paddle ---------------------------------
        p-pos         (u/get-component p :position)
        p-rect        (u/get-component p :rect)
        p-rect-h      (.-height p-rect)
        p-rect-w      (.-width p-rect)
        p-top-edge    (- (p-pos :y) (/ p-rect-h 2))
        p-bottom-edge (+ (p-pos :y) (/ p-rect-h 2))
        p-left-edge   (- (p-pos :x) (/ p-rect-w) 2)
        p-right-edge  (+ (p-pos :x) (/ p-rect-w) 2)
        ;; ball -----------------------------------
        b-pos         (u/get-component b :position)
        b-x           (b-pos :x)
        b-y           (b-pos :y)]
    (and (< p-left-edge b-x)
         (> p-right-edge b-x)
         (< p-top-edge b-y)
         (> p-bottom-edge b-y))))

(defn check-paddle-collision [em]
  (println "paddle")
  (when (= :playing @(u/get-global em :game-state))
    (let [ball (first (u/query-tag em :ball))
          pos (u/get-component ball :position)
          vel (u/get-component ball :velocity)
          w (u/get-width em)
          h (u/get-height em)]
      (if (< 0 (vel :dx))
        (when (check-collision ball (first (u/query-tag em :player)))
          (.bounce-x vel))
        (when (check-collision ball (first (u/query-tag em :ai)))
          (.bounce-x vel))))))

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
    check-paddle-collision
    check-ball-position
    move-ball
    render-scene))
