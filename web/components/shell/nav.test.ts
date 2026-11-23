import { describe, expect, it } from "vitest";
import { isActive, sidebarItems, tabItems } from "./nav";

describe("isActive", () => {
  it("matches Home only on /", () => {
    expect(isActive("/", "/")).toBe(true);
    expect(isActive("/library", "/")).toBe(false);
  });

  it("matches a section and its sub-pages", () => {
    expect(isActive("/library", "/library")).toBe(true);
    expect(isActive("/library/a2", "/library")).toBe(true);
    expect(isActive("/library-old", "/library")).toBe(false);
  });
});

describe("navigation", () => {
  it("sidebar: Home, Library, Review, Vocabulary", () => {
    expect(sidebarItems.map((i) => i.label)).toEqual(["Home", "Library", "Review", "Vocabulary"]);
  });

  it("phone tab bar: Home, Library, Review, Profile", () => {
    expect(tabItems.map((i) => i.label)).toEqual(["Home", "Library", "Review", "Profile"]);
  });
});
