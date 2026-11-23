import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";

const design = readFileSync(resolve(__dirname, "../../DESIGN.md"), "utf8");
const theme = readFileSync(resolve(__dirname, "../app/theme.css"), "utf8");

/** Reads one flat block (e.g. `colors:`) of DESIGN.md's YAML frontmatter. */
function frontmatterBlock(name: string): Record<string, string> {
  const frontmatter = design.split(/^---$/m)[1];
  const lines = frontmatter.split(/\r?\n/);
  const start = lines.findIndex((l) => l === `${name}:`);
  const entries: Record<string, string> = {};
  for (const line of lines.slice(start + 1)) {
    const match = /^ {2}([\w-]+): "?([^"]+)"?$/.exec(line);
    if (!match) break;
    entries[match[1]] = match[2];
  }
  return entries;
}

function themeVar(name: string): string | undefined {
  return new RegExp(`--${name}:\\s*([^;]+);`).exec(theme)?.[1].trim();
}

const normalise = (v: string) => v.toLowerCase().replace(/\s+/g, "");

describe("theme.css matches DESIGN.md", () => {
  const colors = frontmatterBlock("colors");

  it("reads the frontmatter", () => {
    expect(Object.keys(colors).length).toBeGreaterThanOrEqual(17);
  });

  it.each(Object.entries(colors))("colour %s", (name, value) => {
    expect(normalise(themeVar(`color-${name}`) ?? "missing")).toBe(normalise(value));
  });

  it.each(Object.entries(frontmatterBlock("rounded")))("radius %s", (name, value) => {
    expect(themeVar(`radius-${name}`)).toBe(value);
  });

  it("spacing steps are multiples of the 4 px base", () => {
    expect(themeVar("spacing")).toBe("4px");
    for (const [step, value] of Object.entries(frontmatterBlock("spacing"))) {
      expect(`${Number(step) * 4}px`).toBe(value);
    }
  });
});
