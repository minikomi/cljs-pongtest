(ns ecscljspong.entities
  (:require [ecscljspong.util :as u]
            [ecscljspong.events :as ev]
            [ecscljspong.components :as com]))


(defn create-rectangle [em w h]
  (let [rect (js/PIXI.Graphics.)]
    (.lineStyle rect 1 0xffffff 1)
    (.beginFill rect 0xffffff)
    (.drawRect rect (- (/ w 2)) (- (/ h 2)) w h)
    (.endFill rect)
    (.addChild (u/get-global em :stage) rect)
    rect))

(defn create-circle [r]
  (let [circ (js/PIXI.Graphics.)]
    (.lineStyle circ 1 0xffffff 1)
    (.beginFill circ 0xffffff)
    (.drawCircle circ 0 0 r)
    (.endFill circ)
    circ))

(defn make-score [em]
  (let [score (.createEntity em)]
    (u/add-component score
                     :score
                     (volatile! 0))))

(defn make-ball [em]
  (let [ball (.createEntity em)
        r (u/get-global em :renderer)
        x (/ (u/get-width em) 2)
        y (/ (u/get-height em) 2)
        circ (create-circle 8)]
    (.addChild (u/get-global em :stage) circ)
    (doto ball
      (u/add-tag :ball)
      (u/add-component :circ circ)
      (u/add-component :velocity (com/Velocity. 0 0))
      (u/add-component :position (com/Position. x y)))))

(defn make-player-paddle [em]
  (let [paddle (.createEntity em)
        x (- (u/get-width em) 10)
        y (/ (u/get-height em) 2)
        rect (create-rectangle em 10 (/ (u/get-height em) 5))]
    (doto paddle
      (u/add-tag :player)
      (u/add-tag :paddle)
      (u/add-component :rect rect)
      (u/add-component :position (com/Position. x y)))))

(defn make-ai-paddle [em]
  (let [paddle (.createEntity em)
        r (u/get-global em :renderer)
        x (- (.-width (r)) 10)
        y (/ (.-height r) 2)
        rect (create-rectangle em 10 (/ (.-height r) 5))]
    (doto paddle
      (u/add-tag :ai)
      (u/add-tag :paddle)
      (u/add-component :speed 2)
      (u/add-component :rect rect)
      (u/add-component :position (com/Position. x y)))))

(defn create-entities [em]
  (doto em
    make-ball
    make-player-paddle))
