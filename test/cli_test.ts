const projectRoot = new URL("../", import.meta.url);
const decoder = new TextDecoder();

async function runCli(...args: string[]) {
  const result = await new Deno.Command(Deno.execPath(), {
    args: ["task", "dev", ...args],
    cwd: projectRoot,
    stdout: "piped",
    stderr: "piped",
  }).output();

  return {
    code: result.code,
    stdout: decoder.decode(result.stdout),
    stderr: decoder.decode(result.stderr),
  };
}

function assert(condition: boolean, message: string) {
  if (!condition) throw new Error(message);
}

Deno.test("--all emits a greeting for every CSV row", async () => {
  const result = await runCli("--all");

  assert(result.code === 0, `expected exit 0, got ${result.code}`);
  for (const name of ["Ada", "Grace", "Margaret"]) {
    assert(
      result.stdout.includes(`Hello, ${name}!`),
      `missing greeting for ${name}`,
    );
  }
});

Deno.test("unknown options are concise usage errors", async () => {
  const result = await runCli("--nmae", "Ada");

  assert(result.code === 2, `expected exit 2, got ${result.code}`);
  assert(
    result.stderr.includes("Error: Unknown option: --nmae"),
    result.stderr,
  );
  assert(!result.stderr.includes("unsafe-proto"), result.stderr);
});

Deno.test("missing option values are concise usage errors", async () => {
  const result = await runCli("--name");

  assert(result.code === 2, `expected exit 2, got ${result.code}`);
  assert(result.stderr.includes("Error:"), result.stderr);
  assert(result.stderr.includes("--name"), result.stderr);
  assert(!result.stderr.includes("unsafe-proto"), result.stderr);
});
