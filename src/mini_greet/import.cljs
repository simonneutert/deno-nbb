(ns mini-greet.import
  (:require ["jsr:@std/csv" :refer [parse]]))

(defn- data-path [filename]
  (let [root (or (.-miniGreetRoot js/globalThis) (js/Deno.cwd))]
    (str root "/data/" filename)))

(defn parse-csv [text]
  (js->clj (parse text #js {:skipFirstRow true :strip true})
           :keywordize-keys true))

(defn parse-json [text]
  (js->clj (js/JSON.parse text) :keywordize-keys true))

(defn load-data []
  {:csv (-> "people.csv" data-path js/Deno.readTextFileSync parse-csv)
   :json (-> "people.json" data-path js/Deno.readTextFileSync parse-json)})

(defn log-results!
  ([] (log-results! (load-data)))
  ([{:keys [csv json]}]
   (js/console.log "Parsed CSV:" (clj->js csv))
   (js/console.log "Parsed JSON:" (clj->js json))))
