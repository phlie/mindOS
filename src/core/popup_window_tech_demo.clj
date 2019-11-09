;; This app is a demo meant to see how to exchange information between two windows

;; All cljfx apps need the cljfx.api as a required file
;; While the import is for using Java's key press code
(ns core.popup-window-tech-demo
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.input KeyCode KeyEvent]))

;; Uses an fx context and three variables, the message for the first wmindow,
;; the text-field in the second window, and whether the second window is open
(def *state
  (atom (fx/create-context
         {:message "Pop it up!"
          :new-text ""
          :open false})))

;; The popup window which takes in a context as its only :key
(defn other-window [{:keys [fx/context]}]
  {:fx/type :stage
   ;; Uses the *state atom context to show or not show
   :showing (fx/sub context :open)
   :always-on-top true
   :width 300
   :height 80
   :scene {:fx/type :scene
           :on-key-pressed {:event/type :event/scene-key-pressed}
           ;; The stylesheets should be in the "src" folder
           :stylesheets #{"style.css"}
           ;; A simple v-box for the display
           :root {:fx/type :v-box
                  :children [{:fx/type :label
                              :text "Type a message: "}
                             {:fx/type :text-field
                              ;; When text is typed save it in the *state variable
                              :on-text-changed #(swap! *state fx/swap-context assoc :new-text %)
                              ;; The text to be displayed should be :new-text
                              :text (fx/sub context :new-text)}]}}})

;; Used to set the :message equal to the second window text and to close the 2nd window
(defn exchange-messages
  []
  (println (:new-text (second (first @*state))))
  (println (fx/sub @*state :message))
  ;; Standard context swap but @*state is used in the same sense as context above
  (swap! *state fx/swap-context assoc :message (fx/sub @*state :new-text))
  ;; Closes the window by not showing it
  (swap! *state fx/swap-context assoc :open false))

;; Takes in the event which contains the key pressed
(defn map-event-handler [e]
  ;; When the key press is ENTER, call exchange-messages
  (when (and (= :event/scene-key-pressed (:event/type e))
             (= KeyCode/ENTER (.getCode ^KeyEvent (:fx/event e))))
    (exchange-messages)))

;; Commented for now but use to be the old renderer
;; (def other-renderer
;;   (fx/create-renderer
;;    :middleware (fx/wrap-map-desc assoc :fx/type other-window)
;; :opts {:fx.opt/map-event-handler map-event-handler}))

;; The new render function that handles both windows render needs
(defn render
  [stage middle?]
  (fx/create-renderer
   ;; the middleware with comp making a composition of the following two functions
   :middleware (comp
                fx/wrap-context-desc
                (fx/wrap-map-desc (fn [_] {:fx/type stage})))
   ;; The two instances of the when statement make sure both the key and value are present
   ;; later I should try to replace the two when's with a macro
:opts {(when middle? :fx.opt/map-event-handler) (when middle? map-event-handler)
       ;; Used to get the context into the windows
       :fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
                                    (fx/fn->lifecycle-with-context %))}))

;; button-action simply opens the 2nd window when called
(defn button-action
  [_]
  (println "Button pushed, maybe?")
  (swap! *state fx/swap-context assoc :open true))

;; A custom component, which is the button to open the second window
(defn pop-up-button [{:keys [button-text]}]
  {:fx/type :button
   :text button-text
   :on-action button-action})

;; The original window
(defn root [{:keys [fx/context]}]
  {:fx/type :stage
   :showing true
   :width 300
   :height 100
   :scene {:fx/type :scene
           :stylesheets #{"style.css"}
           :root {:fx/type :v-box
                  :children [{:fx/type :label
                              ;; Displays the message that is type in in the other window
                              :text (fx/sub context :message)}
                             {:fx/type pop-up-button
                              :button-text "Alert!"}]}}})

;; (def renderer
;;   (fx/create-renderer
;;    :middleware (comp
;;                 fx/wrap-context-desc
;;                 (fx/wrap-map-desc (fn [_] {:fx/type root})))
;;    :opts {:fx.opt/type->lifecycle #(or (fx/keyword->lifecycle %)
;;                                        (fx/fn->lifecycle-with-context %))}))

;; Creates both of the renderers needed
(fx/mount-renderer *state (render root false))
(fx/mount-renderer *state (render other-window true))
