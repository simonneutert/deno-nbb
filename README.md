# nbb + Deno mini CLI

A small [ClojureScript](https://clojurescript.org/) CLI that uses
[nbb](https://github.com/babashka/nbb) for evaluation and bundling, and
[Deno](https://deno.com/) for the runtime, permissions, JSR imports, tests, and
standalone executable packaging.

The result is a familiar Babashka-style project with a simple distribution
story: write `.cljs`, develop in a REPL, bundle with nbb, and ship one native
executable with `deno compile`.

![deno-nbb_logo](./deno-nbb_logo.webp)

## Quick start

Requirements:
[Deno](https://docs.deno.com/runtime/getting_started/installation/) and VS Code
with the [Calva](https://calva.io/) extension (optional, for REPL development).

```sh
deno task dev --name Ada
deno task test
deno task compile
./mini-greet --name Ada
```

## How the tools fit together

nbb and Deno have distinct responsibilities:

- **nbb** evaluates ClojureScript, supplies Babashka namespaces, reads the
  project classpath from `nbb.edn`, and creates `main.bundle.js`.
- **Deno** launches nbb, enforces permissions, resolves imports such as
  `jsr:@std/csv`, runs the tests, and compiles JavaScript into a native binary.

Development evaluates `src/mini_greet/main.cljs` directly. A release first
bundles the ClojureScript, then packages that bundle with `runtime.ts`, the
embedded data files, and the Deno runtime. The shipped executable needs no Node
installation, nbb command, or source checkout.

## Project layout

```text
.
├── data/people.csv       # CSV fixture
├── data/people.json      # JSON fixture
├── nbb.edn               # Adds src/ to nbb's classpath
├── deno.json             # Deno tasks and pinned nbb import
├── runtime.ts            # deno compile entrypoint
├── src/mini_greet/
│   ├── greeting.cljs     # greeting function
│   ├── import.cljs       # CSV/JSON loading and parsing
│   └── main.cljs         # CLI options and entrypoint
└── test/
    ├── main_test.cljs    # ClojureScript unit tests
    ├── main_test.js      # nbb test loader
    └── cli_test.js       # Deno CLI integration tests
```

As in a standard Clojure project, the namespace `mini-greet.greeting` lives at
`src/mini_greet/greeting.cljs`: hyphens in namespace segments become underscores
in paths.

## CLI development

```sh
deno task dev --name Ada
deno task dev --caps Clojure
deno task dev --all
deno task dev --help
```

The CLI supports `--name`/`-n`, `--caps`/`-c`, `--all`/`-a`, `--help`/`-h`, and
`--version`/`-v`. `--all` parses both fixtures, logs the collections, and emits
one greeting for every CSV row.

## REPL development with Calva

Start nbb's nREPL server through Deno:

```sh
deno task repl
```

Then in VS Code run **Calva: Connect to a Running REPL Server not in Project**,
choose **ClojureScript**, and connect to `localhost:1337`.

Evaluate forms from the editor:

```clojure
(require '[mini-greet.greeting :as greet])
(greet/greeting {:name "Ada" :caps true})
;; => "HELLO, ADA!"

(require '[mini-greet.import :as data])
(data/load-data)
```

The REPL is for interactive development; the release path is still nbb `bundle`
followed by `deno compile`. nbb's nREPL support is lighter than a full
CIDER/nREPL stack, so some advanced editor features may be unavailable. See
[Calva's nbb guide](https://calva.io/nbb/) for its current integration details.

## Formatting ClojureScript

[`cljfmt`](https://github.com/weavejester/cljfmt) is one option for formatting
the ClojureScript source and tests. After installing its command-line tool,
check or format the project with:

```sh
deno task cljfmt
deno task cljfmt:fix
```

The `cljfmt:fix` task runs `cljfmt fix` over `src` and `test`.

## Importing CSV and JSON

`mini-greet.import` uses Deno's JSR CSV package and the standard JSON parser:

```clojure
(ns mini-greet.import
  (:require ["jsr:@std/csv" :refer [parse]]))

(parse csv-text #js {:skipFirstRow true :strip true})
(js->clj (js/JSON.parse json-text) :keywordize-keys true)
```

The three records in `data/people.csv` and `data/people.json` have matching
`id`, `name`, and `language` fields. The CSV and JSON equivalence is covered by
the unit tests.

## Tests

Run all tests with:

```sh
deno task test
# equivalent: deno test -A
```

The suite contains six tests: three ClojureScript unit tests and three Deno
integration tests covering `--all`, unknown options, and missing option values.
`-A` gives the nbb loader the permissions needed to read and evaluate sources.

## Bundle and compile

```sh
deno task bundle
deno task compile
./mini-greet --all
```

The two build stages are:

```text
src/mini_greet/*.cljs --nbb bundle--> main.bundle.js
main.bundle.js + runtime.ts + data/* --deno compile--> mini-greet
```

`runtime.ts` sets the resource root before loading the bundle. `--include=data`
embeds the fixtures, while `--allow-read` permits the executable and nbb's
initialization to read those resources. Add other `--allow-*` flags only when
the application needs network, environment, subprocess, or write access.

## Optional global nbb command

The Deno tasks do not require a global install. If desired:

```sh
deno install --global --force --allow-read jsr:@babashka/nbb@1.5.212
export PATH="$HOME/.deno/bin:$PATH"
```

This installs nbb's Deno launcher; Node is not required for this workflow.

## Generated files

The following build outputs are ignored by Git:

```text
main.bundle.js
mini-greet
mini-greet.exe
```

Source files, `data/*`, `deno.json`, `deno.lock`, and `nbb.edn` should remain
tracked so another checkout can reproduce the build.
