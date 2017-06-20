(ns ecscljspong.core
  (:require
   [cljsjs.pixi]
   [reagent.core :as r]
   [ecscljspong.events :as ev]
   [ecscljspong.systems :as sys]
   [ecscljspong.entities :as ent]
   [ecscljspong.util :as u]
   [ecs.EntityManager :as EM]))

(enable-console-print!)

(defn game []
  (let [dom-node (atom false)]
    (r/create-class
     {:display-name "game"
      :component-did-mount
      (fn [this]
        (reset! dom-node (r/dom-node this))
        (let [renderer (.autoDetectRenderer js/PIXI 800 600)
              stage (js/PIXI.Container.)
              em (EM/Manager. {:renderer renderer
                               :stage stage})
              loop-fn (fn loop []
                        (when @dom-node
                          (js/requestAnimationFrame loop))
                        (sys/run-systems em))]
          ;; init
          (ev/init-events stage em)
          (ent/create-entities em)
          (.appendChild @dom-node (.-view renderer))
          ;; big bang
          (loop-fn)))
      :component-will-unmount
      (fn [_]
        (reset! dom-node false))
      :reagent-render
      (fn []
        [:div {:id "game"}])})))

(defn init []
  (r/render [game]
            (.getElementById js/document "app")))
