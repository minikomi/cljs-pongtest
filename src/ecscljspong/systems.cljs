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
        gs    (u/get-global em :game-state)]
    (case @gs
      :playing (handle-playing-inputs held? em)
      (handle-default-inputs held? gs em))))

;; Bounce / check win
;; --------------------------------------------------------------------

(defn check-ball-position [em]
  (let [gs   (u/get-global em :game-state)
        w    (u/get-width em)
        h    (u/get-height em)
        ball (u/get-tagged em :ball)
        pos  (u/get-component ball :position)
        vel  (u/get-component ball :velocity)]
    (when (or (>= 0 (pos :y))
              (<= h (pos :y)))
      (set! (.-y pos) (u/clamp (pos :y) 0 h))
      (.bounce-y vel))
    (when (<= w (pos :x))
      (reset! gs :you-lose))
    (when (>= 0 (pos :x))
      (reset! gs :you-win))))

;; paddle reflect
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

(defn calc-spin [b p]
  (let [b-pos (u/get-component b :position)
        p-pos (u/get-component p :position)]
   (*
    (+ (- (b-pos :y) (p-pos :y)))
    0.03)))

(defn check-paddle-collision [em]
  (when (= :playing @(u/get-global em :game-state))
    (let [w    (u/get-width em)
          h    (u/get-height em)
          ball (u/get-tagged em :ball)
          pos  (u/get-component ball :position)
          vel  (u/get-component ball :velocity)
          player (u/get-tagged em :player)
          ai     (u/get-tagged em :ai)]
      (if (< 0 (vel :dx))
        (when (check-collision ball player)
          (.bounce-x vel)
          (.adjust-y vel (calc-spin ball player)
                         ))
        (when (check-collision ball (u/get-tagged em :ai))
          (.bounce-x vel)
          (.adjust-y vel (calc-spin ball ai)))))))

;; ball movement
;; --------------------------------------------------------------------

(defn move-ball [em]
  (let [gs (u/get-global em :game-state)]
    (when (= :playing @gs)
      (let [ball (u/get-tagged em :ball)
            pos  (u/get-component ball :position)
            vel  (u/get-component ball :velocity)]
        (.set pos (+ (vel :dx) (pos :x))
              (+ (vel :dy) (pos :y)))))))

;; AI
;; --------------------------------------------------------------------

(defn ai-paddle [em])

;; Render
;; --------------------------------------------------------------------

(defn render-scene [em]
  (let [gs       @(u/get-global em :game-state)
        renderer (u/get-global em :renderer)
        stage    (u/get-global em :stage)]
    ;; set ball pos
    (doseq [e (u/query-components em [:circ])]
      (let [c   (u/get-component e :circ)
            pos (u/get-component e :position)]
        (.set (.-position c) (pos :x) (pos :y))))
    ;; set paddles pos
    (doseq [e (u/query-components em [:rect])]
      (let [r   (u/get-component e :rect)
            pos (u/get-component e :position)]
        (.set (.-position r) (pos :x) (pos :y))))
    ;; render
    (.render renderer stage)))

(defn run-systems [em]
  (doto em
    process-events
    handle-inputs
    check-paddle-collision
    check-ball-position
    move-ball
    render-scene))
