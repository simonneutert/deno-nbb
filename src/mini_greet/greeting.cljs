(ns mini-greet.greeting
  (:require [clojure.string :as str]))

(defn greeting [{:keys [name caps]}]
  (let [message (str "Hello, " (or name "world") "!")]
    (if caps (str/upper-case message) message)))

(defn greetings [people opts]
  (mapv #(greeting (assoc opts :name (:name %))) people))
