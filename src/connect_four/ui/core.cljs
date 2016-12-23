(ns connect-four.ui.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cognitect.transit :as t]
            )
  (:import [goog Uri]
           [goog.net Jsonp]))

(enable-console-print!)
(def app-state (atom {:status "game.waiting"
                      :running false
                      :last-move nil
                      :current-player nil
                      :player-one-name "Team One"
                      :player-two-name "Team Two"
                      :board [
                        [0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0]
                      ]}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)
(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defn get-player-team-name [player props]
  (if (= :player-one player)
    (:player-one-name props)
    (:player-two-name props)))

(defn get-current-player-team-name [props]
  (get-player-team-name (:current-player props) props))

(defn get-current-player-class-name [props]
  (if (= :player-one (:current-player props))
    "cf-message-final cf-message-player-one"
    "cf-message-final cf-message-player-two"))

(defn get-message-class [props]
  (let [team-name (get-current-player-team-name props)]
    (case (:status props)
      "player.won" (get-current-player-class-name props)
      "player.error" (get-current-player-class-name props)
      "game.tied" "cf-message-final cf-message-tie"
      ""
    )))

(defn create-message [props]
  (let [team-name (get-current-player-team-name props)]
    (case (:status props)
      "game.waiting" "Connecting to server ..."
      "game.connection-failed" "Failed to connect to the server."
      "game.connection-lost" "The server connection was lost."
      "game.tied" "Game tied!"
      "player.won" (str team-name " won!")
      "player.error" (str team-name " lost due to an error!")
      ""
    )))

(defui MessageArea
  static om/IQuery
    (query [this] [:status :current-player :player-one-name :player-two-name])
  Object
    (render [this]
      (let [props (om/props this)]
        (dom/div #js {:className "cf-message-area"}
          (dom/div #js {:className (str "cf-message " (get-message-class props))}
                   (create-message props))))))

(defn build-sidebar [player props]
  (let [{:keys [player-two-name current-player]} props
        class-name (if (= player :player-one) "cf-team-one-sidebar" "cf-team-two-sidebar")
        team-name (get-player-team-name player props)
        running (:running props)
        current-player (:current-player props)]
    (if running
      (dom/div #js {:className (str "cf-team-sidebar " class-name)}
        (dom/div #js {:className "cf-team-name"} team-name)
        (if (= player current-player)
          (dom/div #js {:className "cf-spinner"}
            (dom/div #js {:className "cf-bounce-1"} nil)
            (dom/div #js {:className "cf-bounce-2"} nil)
            (dom/div #js {:className "cf-bounce-3"} nil))
          (dom/div #js {:className "cf-spinner"} nil)))
      (dom/div #js {:className (str "cf-team-sidebar " class-name)}))))

(defui TeamOneSidebar
  static om/IQuery
    (query [this] [:player-one-name :current-player :running])
  Object
    (render [this]
      (build-sidebar :player-one (om/props this))))

(defui TeamTwoSidebar
  static om/IQuery
    (query [this] [:player-two-name :current-player :running])
  Object
    (render [this]
      (build-sidebar :player-two (om/props this))))

(defn cell-decorator [player row col last-move]
  (let [current {:player player :row row :column col}]
    (when (= current last-move)
      "cf-current-move")))

(defn build-player-piece [player decorator]
  (let [player-class (str "cf-game-board-piece-player-" player)
        class-names (str "cf-game-board-piece" " " player-class " " decorator)]
    (dom/div #js {:className class-names} nil)))

(defn build-cells [board last-move]
  (map-indexed
    (fn [row-index row]
      (apply dom/div #js {:className "cf-game-board-row"}
        (map-indexed
          (fn [cell-index cell]
            (dom/div #js {:className "cf-game-board-cell"}
              (case cell
                1 (build-player-piece 1 (cell-decorator :player-one row-index cell-index last-move))
                2 (build-player-piece 2 (cell-decorator :player-two row-index cell-index last-move))
                nil)))
          row)))
    board))

(defui GameBoard
  static om/IQuery
    (query [this] [:board :last-move])
  Object
    (render [this]
      (let [{:keys [board last-move]} (om/props this)]
        (dom/div #js {:className "cf-game-board"}
          (build-cells board last-move)))))

(def parser (om/parser {:read read :mutate mutate}))
(def reconciler (om/reconciler {:state app-state :parser parser}))

(om/add-root! (om/reconciler {:state app-state :parser parser})
              MessageArea
              (gdom/getElementByClass "cf-message-area-anchor"))
(om/add-root! (om/reconciler {:state app-state :parser parser})
              TeamOneSidebar
              (gdom/getElementByClass "cf-team-one-sidebar-anchor"))
(om/add-root! (om/reconciler {:state app-state :parser parser})
              GameBoard
              (gdom/getElementByClass "cf-game-board"))
(om/add-root! (om/reconciler {:state app-state :parser parser})
              TeamTwoSidebar
              (gdom/getElementByClass "cf-team-two-sidebar-anchor"))

(let [host (.-host js/location)
      socket (js/WebSocket. (str "ws://" host "/api/game-state"))]
  (set!
    (.-onerror socket)
    (fn [error]
      (om/merge! reconciler {:status "game.connection-failed"})))
  (set!
    (.-onclose socket)
    (fn [close]
      (om/merge! reconciler {:status "game.connection-lost"
                             :running false})))
  (set!
    (.-onmessage socket)
    (fn [event]
      (let [reader (js/FileReader.)]
        (.addEventListener reader "loadend"
          #(let [state (t/read (t/reader :json) (.-result reader))]
            #_(println "Merging state:" state)
            (om/merge! reconciler state)))
        (.readAsText reader (.-data event))))))