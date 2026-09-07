(ns mini-greet.main
  (:require [babashka.cli :as cli]
            [clojure.string :as str]
            [mini-greet.greeting :as greet]
            [mini-greet.import :as data]))

(def version "0.1.0")

(def usage
  (str/join
   "\n"
   ["mini-greet - a tiny nbb CLI compiled by Deno"
    ""
    "Usage:"
    "  mini-greet [options] [name]"
    ""
    "Options:"
    "  -n, --name NAME   Name to greet (positional name also works)"
    "  -c, --caps        Uppercase the greeting"
    "  -a, --all         Show all greetings"
    "  -h, --help        Show this help"
    "  -v, --version     Show the version"]))

(def cli-spec
  {:name    {:alias :n :coerce :string}
   :caps    {:alias :c :coerce :boolean}
   :all     {:alias :a :coerce :boolean}
   :help    {:alias :h :coerce :boolean}
   :version {:alias :v :coerce :boolean}})

(defn usage-error! [message]
  (js/console.error (str "Error: " message))
  (js/console.error "Run with --help for usage.")
  (set! (.-exitCode js/Deno) 2))

(defn parse-cli [args]
  (try
    {:parsed (cli/parse-args args {:spec cli-spec
                                   :args->opts [:name]
                                   :restrict true
                                   :restrict-args true})}
    (catch :default error
      {:error (.-message error)})))

(defn runtime-args []
  ;; nbb populates *command-line-args* when it evaluates this file directly.
  ;; Its generated JS bundle uses the API instead, so Deno.args is the source
  ;; of arguments after `deno run` or `deno compile`.
  (if (seq *command-line-args*)
    *command-line-args*
    (let [args (vec (.-args js/Deno))]
      (if (and (first args) (.endsWith (first args) ".cljs"))
        (subvec args 1)
        args))))

(defn -main [& args]
  (let [{:keys [error parsed]} (parse-cli args)]
    (if error
      (usage-error! error)
      (let [{:keys [opts]} parsed]
        (cond
          (:help opts)
          (println usage)

          (:version opts)
          (println version)

          (:all opts)
          (let [{:keys [csv] :as imported} (data/load-data)]
            (data/log-results! imported)
            (run! println (greet/greetings csv opts)))

          :else
          (println (greet/greeting opts)))))))

(apply -main (runtime-args))
