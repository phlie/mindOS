;; The time is required for the clock
;; The core.async is also required for the clock
;; to be in its own thread
(ns core.real-app
  (:require [cljfx.api :as fx]
            [clj-time.core :as t]
            [clojure.core.async :as async]))

;; Right now just one state variable for the time
(def *state
  (atom {:time ""}))

;; This is where the main application front-end code will go
(defn main-content
  [{:keys []}]
  {:fx/type :v-box
   :style-class "main-content"
   :children [{:fx/type :label
               :text ""}]})

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

;; A standard stage
(defn root [{:keys [time]}]
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
           :root {:fx/type :anchor-pane
                  :children [{:fx/type :label
                              ;; Used to position the element
                              :anchor-pane/left 10
                              :anchor-pane/top 10
                              :text "Reminders"}
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
                              :anchor-pane/bottom 15}]}}})

;; A standard renderer
(def renderer
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)))

;; This function is used to get the current in async
(async/go-loop [seconds 1]
  (now)
  (async/<! (async/timeout 1000))
  (recur (inc seconds)))

;; Finally render the window
(fx/mount-renderer *state renderer)
