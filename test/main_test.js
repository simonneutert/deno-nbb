import { addClassPath, loadFile } from "nbb";

addClassPath("src");
await loadFile("test/main_test.cljs");
