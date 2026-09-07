// The nbb bundle is loaded dynamically so this resource root is available to
// ClojureScript before its top-level forms run. Deno rewrites import.meta.dirname
// to the embedded virtual filesystem when this entrypoint is compiled.
globalThis.miniGreetRoot = import.meta.dirname;

await import("./main.bundle.js");

declare global {
  var miniGreetRoot: string | undefined;
}
