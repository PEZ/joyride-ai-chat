(ns ai-chat.human-intelligence
  (:require [promesa.core :as p]
            ["vscode" :as vscode]))

(defn ask!+
  "Shows a VS Code quick pick with provided
   * `question` as `title` (be brief)
   * `question-elaboration` as `placeHolder` (be brief)
   * `items` as items, can be a mix of strings of QuickPickItems
   * `quick-pick-options` is keyword args corresponding to QuickPickOptions
      e.g.: `:canPickMany true`

   Returns selected item(s) or `:hi/cancelled` if cancelled."
  [question question-elaboration items & {:as quick-pick-options}]
  (let [options (merge {:title question
                        :ignoreFocusOut true
                        :placeHolder question-elaboration}
                       quick-pick-options)]
    (p/let [choice (vscode/window.showQuickPick
                    (clj->js items)
                    (clj->js options))]
      (if choice
        choice
        :hi/cancelled))))

(comment
    (p/let [answer (ask!+ "hello" "because" ["foo"
                                           "bar"
                                           #js {:label "baz"
                                                :description "elaborate on baz"}]
                          :canPickMany true)]
    (def answer answer))
  :rcf)

