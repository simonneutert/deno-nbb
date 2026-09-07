(ns main-test
  (:require [mini-greet.greeting :as greet]
            [mini-greet.import :as data]))

(js/Deno.test
 "greets a named person"
 (fn []
   (assert (= "Hello, Ada!" (greet/greeting {:name "Ada"})))))

(js/Deno.test
 "greets every person"
 (fn []
   (assert (= ["Hello, Ada!" "Hello, Grace!"]
              (greet/greetings [{:name "Ada"} {:name "Grace"}] {})))))

(js/Deno.test
 "parses equivalent CSV and JSON data"
 (fn []
   (let [{:keys [csv json]} (data/load-data)]
     (assert (= 3 (count csv)))
     (assert (= csv json)))))
