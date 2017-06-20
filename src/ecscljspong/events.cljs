(ns ecscljspong.events
  (:require
    [ecscljspong.util :as u]
    [goog.events :as events])
  (:import [goog.events EventType]))

(def event-bus (volatile! []))
(def mouse-pressed (volatile! false))
(def mouse-position (volatile! nil))
(def keys-held (volatile! #{}))

(defn event!
  ([event-type]
   (vswap! event-bus conj [event-type nil]))
  ([event-type data]
   (vswap! event-bus conj [event-type data])))

(defn init-events [stage em]
  (set! (.-interactive stage) true)
  (set! (.-hitArea stage)
        (js/PIXI.Rectangle. 0 0 (u/get-width em) (u/get-height em)))

  (events/listen js/window
                 EventType.KEYDOWN
                 (fn [ev]
                   (event! :key-down (.. ev -event_ -key))))

  (events/listen js/window
                 EventType.KEYUP
                 (fn [ev]
                   (event! :key-up (.. ev -event_ -key))))

  (.on stage "mousemove"
       (fn [ev]
         (event! :mouse-move {:x (.. ev -data -global -x)
                              :y (.. ev -data -global -y)})))
  (.on stage "mousedown"
       (fn [ev]
         (event! :mouse-down {:x (.. ev -data -global -x)
                              :y (.. ev -data -global -y)})))
  (.on stage "mouseup"
       (fn [ev] (event! :mouse-up))))

(def event-handlers
  {:key-down
   (fn key-down-handler [_ k]
     (vswap! keys-held conj k))
   :key-up
   (fn key-down-handler [_ k]
     (println k)
     (vswap! keys-held disj k))
   :mouse-down
   (fn mouse-down-handler [_ data]
     (vreset! mouse-position data)
     (vreset! mouse-pressed true))
   :mouse-up
   (fn mouse-up-handler [_ _]
     (vreset! mouse-pressed false))
   :mouse-move
   (fn mouse-move-hanlder [_ data]
     (vreset! mouse-position data))})
