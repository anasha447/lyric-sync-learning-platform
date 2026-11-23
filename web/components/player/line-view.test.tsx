import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import type { Line } from "@/lib/songs";
import { LineView } from "./line-view";

const line: Line = {
  index: 0,
  text: "Am Brunnen vor dem Tore",
  startMs: 0,
  endMs: 2_000,
  instrumental: false,
  translation: "By the well before the gate",
};

const renderLine = (props: Partial<Parameters<typeof LineView>[0]>) =>
  render(
    <ol>
      <LineView line={line} state="upcoming" showTranslation {...props} />
    </ol>,
  );

describe("LineView", () => {
  it("an upcoming line is muted, with its translation below", () => {
    renderLine({});
    expect(screen.getByText(line.text)).toHaveClass("text-lyric", "text-ink-muted");
    expect(screen.getByText(line.translation!)).toHaveClass("text-teal-light");
    expect(screen.getByRole("listitem")).not.toHaveAttribute("aria-current");
  });

  it("the active line is large, ink and marked current, with the karaoke underline", () => {
    renderLine({ state: "active" });
    const text = screen.getByText(line.text);
    expect(text).toHaveClass("text-lyric-active", "text-ink");
    expect(text).toHaveAttribute("data-underline");
    expect(text.style.backgroundImage).toContain("--color-karaoke");
    expect(screen.getByRole("listitem")).toHaveAttribute("aria-current", "true");
  });

  it("only the active line has the underline (Sound rule)", () => {
    renderLine({ state: "done" });
    const text = screen.getByText(line.text);
    expect(text).not.toHaveAttribute("data-underline");
    expect(text.style.backgroundImage).toBe("");
  });

  it("hides the translation when asked", () => {
    renderLine({ showTranslation: false });
    expect(screen.queryByText(line.translation!)).toBeNull();
  });

  it("an instrumental line shows a note, not text", () => {
    renderLine({ line: { ...line, instrumental: true, text: "", translation: null } });
    expect(screen.getByLabelText("Instrumental")).toHaveTextContent("♪");
  });
});
