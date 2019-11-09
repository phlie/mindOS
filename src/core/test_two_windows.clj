;; This app is meant to test out how to load 2 windows at the same time
;; as well as load the proper CSS
(ns core.test-two-windows
  (:require [cljfx.api :as fx]))

;; A simple atom with just one variable
(def *state
  (atom {:name "Matt"}))

;; The root window which is pretty standard but does load the css and display
;; the *state atom :name
(defn root [{:keys []}]
  {:fx/type :stage
   :showing true
   :always-on-top true
   :width 500
   :height 500
   :scene {:fx/type :scene
           :stylesheets #{"style.css"}
           :root {:fx/type :v-box
                  :children [{:fx/type :label
                              :text (get @*state :name)}]}}})

;; The other window which is very straight forward
(defn other [{:keys []}]
  {:fx/type :stage
   :showing true
   :always-on-top true
   :width 250
   :height 250
   :scene {:fx/type :scene
           :stylesheets #{"style.css"}
           :root {:fx/type :v-box
                  :children [{:fx/type :label
                              :text "Other window"}]}}})

;; The root renderer
(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)))

;; The second window renderer
(def rend-two
  (fx/create-renderer
  :middleware (fx/wrap-map-desc assoc :fx/type other)))

;; Renders the two windows
(fx/mount-renderer *state renderer)
(fx/mount-renderer *state rend-two)
