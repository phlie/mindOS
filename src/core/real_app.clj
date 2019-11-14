;; The time is required for the clock
;; The core.async is also required for the clock
;; to be in its own thread
(ns core.real-app
  (:require [cljfx.api :as fx]
            [clj-time.core :as t]
            [clojure.core.async :as async])
  ;; Used to get the key inputs
  (:import [javafx.scene.input KeyCode KeyEvent]))

;; Right now just one state variable for the time
(def *state
  (atom {:time ""
         ;; The current active text in the command bar
         :command-text ""
         ;; A variable designed to hold the last entered message to be used in actions
         :last-message ""
         ;; An array holding all of the entered commands
         :entered-commands ["First Message"]}))

;; This is where the main application front-end code will go
(defn main-content
  [{:keys [entered-commands]}]
  {:fx/type :v-box
   :style-class "main-content"
   :children [{:fx/type :label
               ;; For now it just displays all the entered commands each on their own line
               :text (clojure.string/join "\n" entered-commands)}]})

;; Currently used for the time so it displays "01" instead of "1"
(defn if-length-one
  [time-part]
  (if (< time-part 10)
    (str "0" time-part)
    (str time-part)))

;; Used to convert the time to be a 12 hour clock
(defn hours-to-twelve
  [current-hour]
  (if (< 12 (read-string current-hour))
    (str (- (read-string current-hour) 12))
    (if (= "00" current-hour)
      "12"
      current-hour)))

;; This function converts the time into a string
(defn format-time
  [time]
  (let [hours (if-length-one (t/hour time))
        minutes (if-length-one (t/minute time))
        seconds (if-length-one (t/second time))]
    ;; The last bit is to dispaly am or pm and hours-to-twelve is also used for the same thing
    (str (hours-to-twelve hours) ":" minutes ":" seconds (if (>= (read-string hours) 12)
                                                           "pm"
                                                             "am"))))

;; This is the function that actually saves the current time in *state
(defn now []
  (let [current-time (t/now)]
    (format-time current-time)
    (swap! *state assoc :time (format-time current-time))))

;; This is for the command bar
(defn text-input
  [{:keys [typed-text]}]
  {:fx/type :text-field
   :text typed-text
   :prompt-text "Command Bar."
   ;; When the text is changed it calls the ::command-typed event which sets command-text
   :on-text-changed {:event/type ::command-typed}
   ;; When the enter key is pressed it saves the entered text into the command array and
   ;; resuts the text in the field
   :on-key-pressed {:event/type ::command-entered}})

;; A standard stage
(defn root [{:keys [time command-text last-message entered-commands]}]
  {:fx/type :stage
   :showing true
   :always-on-top true
   ;; Looks about right with the Illustrator background
   :width 1000
   :height 540
   :title "When App"
   :scene {:fx/type :scene
           ;; Used to load the proper styling
           :stylesheets #{"app.css"}
           ;; This on-key-pressed is used to an emulation of the devices buttons, the function keys
           :on-key-pressed {:event/type ::device-buttons}
           :root {:fx/type :anchor-pane
                  :children [{:fx/type :label
                              ;; Used to position the element
                              :anchor-pane/left 10
                              :anchor-pane/top 10
                              :text "Reminders"}
                             {:fx/type text-input
                              :anchor-pane/left 400
                              :anchor-pane/top 25
                              ;; Gets passed the command text which it then displays
                              :typed-text command-text}
                             {:fx/type :label
                              :anchor-pane/right 10
                              :anchor-pane/top 10
                              :style-class "time"
                              :text time}
                             {:fx/type main-content
                              ;; Can't seem to put these within the main-content function
                              :anchor-pane/left 30
                              :anchor-pane/top 70
                              :anchor-pane/right 30
                              :anchor-pane/bottom 15
                              ;; The command array
                              :entered-commands entered-commands}]}}})

;; This function is designed to emulate the devices buttons
(defn device-buttons-handler
  [event]
  ;; Needs to be converted into a string to use a case statement
  (case (str (.getCode ^KeyEvent (:fx/event event)))
    "F1" (println "Button 1 pressed")
    "F2" (println "Button 2 pressed")
    "F3" (println "Button 3 pressed")
    "F4" (println "Button 4 pressed")
    "F5" (println "Button 5 pressed")
    ;; On a command being entered, add to the command array and reset the
    ;; text-field text
    ;; For now this resides within the button presses but in the future it proably will only work when the text-field is active
    "ENTER" (do (swap! *state assoc :entered-commands (conj (get @*state :entered-commands) (get @*state :command-text)))
                (swap! *state assoc :command-text ""))))


;; The custom event handler which uses a case to see what action is needed
(defn map-event-handler [event]
  (case (:event/type event)
    ;; This is for typing text in the command bar
    ::command-typed (swap! *state assoc :command-text (:fx/event event))
    ::command-entered nil   ;; In case a devices button has been pressed
    ::device-buttons (device-buttons-handler event)))

;; A standard renderer
(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)
   :opts {:fx.opt/map-event-handler map-event-handler}))

;; This function is used to get the current in async
(async/go-loop [seconds 1]
  (now)
  (async/<! (async/timeout 1000))
  (recur (inc seconds)))

;; Finally render the window
(fx/mount-renderer *state renderer)
