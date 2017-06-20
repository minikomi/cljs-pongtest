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

(defn render-scene [em]
  (let [renderer (u/get-global em :renderer)
        stage (u/get-global em :stage)]
    ;; move about
    (doseq [e (u/query-components em [:circ])]
      (let [c (u/get-component e :circ)
            pos (u/get-component e :position)]
        (.set (.-position c)
              (pos :x)
              (pos :y))))
    (doseq [e (u/query-components em [:rect])]
      (let [r (u/get-component e :rect)
            pos (u/get-component e :position)]
        (.set (.-position r)
              (pos :x)
              (pos :y))))
    (.render renderer stage)))

(defn handle-inputs [em]
  (let [held? @ev/keys-held]
    (when (held? "ArrowUp")
      (let [p (first (u/query-components em [:player]))
            pos (u/get-component p :position)]
        (when (< 0 (pos :y))
          (.move-up pos))))
    (when (held? "ArrowDown")
      (let [p (first (u/query-components em [:player]))
            pos (u/get-component p :position)]
        (when (> (u/get-height em) (pos :y))
          (.move-down pos))))))

(defn run-systems [em]
  (doto em
    process-events
    handle-inputs
    render-scene))
