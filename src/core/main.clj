;; This app demonstrates how to setup a cljfx app, how to get a button
;; to save typed text when the button is pressed, and how to append to
;; a atom (state) variable

(ns core.main
  (:require [cljfx.api :as fx]))

;; Define application state
(def *state
  (atom {:title "App title"
         :messages []}))

;; A custom text-field input that saves the text typed into it
(defn title-input [{:keys [title]}]
  {:fx/type :text-field
   :on-text-changed #(swap! *state assoc :title %)
   :text title})

;; A custom button that conjoins the current messages with the text-field input
(defn push-message [{:keys []}]
  {:fx/type :button
   :text "Speak"
   ;; Needs to be a function that accepts exactly 1 argument
   :on-action (fn [_]
                ;; Appends the mesages with the current value of :title
                (swap! *state assoc :messages (conj (get @*state :messages) (get @*state :title))))}) 
               ;; (swap! *state assoc :messages (get @*state :title)))})

;; The only window in the app and the root window
(defn root [{:keys [title messages]}]
  {:fx/type :stage
   :showing true
   :width 1000
   :height 500
   :title "Message Recorder"
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type :label
                              :text "Window title input"}
                             ;; The custom text-field
                             {:fx/type title-input
                              :title title}
                             ;; The custom button
                             {:fx/type push-message}
                             {:fx/type :label
                              ;; Displays all of the messages seperated by a newline
                              :text (clojure.string/join "\n" messages)}]}}})

;; Create renderer with middleware that maps incoming data - description -
;; to component description that can be used to render JavaFX state.
;; Here description is just passed as an argument to function component.

;; The renderer which when called wraps the state into the :keys of root
(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)))

;; Convenient way to add watch to an atom + immediately render app
;; Render the app
(fx/mount-renderer *state renderer)
