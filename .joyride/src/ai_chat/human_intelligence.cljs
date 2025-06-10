(ns ai-chat.human-intelligence
  (:require [promesa.core :as p]
            ["vscode" :as vscode]))

(defn ask!+
  "Shows a VS Code quick pick with provided
   * `question` as `title` (be brief)
   * `question-elaboration` as `placeHolder` (be brief)
   * `items` QuickPickItems
   * an optional keyword arg `:canSelectMany true`

   Returns selected item(s) or `:hi/cancelled` if cancelled or :hi/timeout if timed out."
  [question question-elaboration items & {:keys [canSelectMany]}]
  (let [quick-pick (vscode/window.createQuickPick)
        !state (atom {})]

    (set! (.-title quick-pick) question)
    (set! (.-placeholder quick-pick) question-elaboration)
    (set! (.-ignoreFocusOut quick-pick) true)
    (set! (.-items quick-pick) (clj->js items))
    (when canSelectMany
      (set! (.-canSelectMany quick-pick) true))

    (def quick-pick quick-pick)
    (def !timeout-id !state)
    (.show quick-pick)

    (p/create
     (fn [resolve _reject]
       (swap! !state assoc ::timeout-id (js/setTimeout
                                         (fn []
                                           (swap! !state assoc ::timed-out true)
                                           (.dispose quick-pick))
                                         3000))
       (.onDidAccept quick-pick
                     (fn []
                       (swap! !state assoc ::selected-items (seq (.-selectedItems quick-pick)))
                       (.dispose quick-pick)))
       (.onDidHide quick-pick
                   (fn []
                     (js/clearTimeout (::timeout-id @!state))
                     (resolve
                      (cond
                        (::timed-out @!state) :hi/timeout
                        (::selected-items @!state) (::selected-items @!state)
                        :else :hi/cancelled))))))))

(comment
  (p/let [answer (ask!+ "hello" "because" [{:label "foo"}

                                           {:label "bar"}
                                           {:label "baz"
                                            :description "elaborate on baz"}]
                        :canSelectMany true)]
    (def answer answer))
  (meta #'ask!+)
  :rcf)

