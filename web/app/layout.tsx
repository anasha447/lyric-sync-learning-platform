import type { Metadata, Viewport } from "next";
// Self-hosted fonts (ADR-0010): bundled with our assets, never loaded from Google.
import "@fontsource-variable/plus-jakarta-sans";
import "@fontsource-variable/inter";
import "@fontsource-variable/jetbrains-mono";
import "./globals.css";
import { Providers } from "./providers";
import { Sidebar } from "@/components/shell/sidebar";
import { TabBar } from "@/components/shell/tab-bar";

export const metadata: Metadata = {
  title: { default: "lyricsync", template: "%s · lyricsync" },
  description: "Learn German through music. Lyrics light up in time with the song; every word explains itself.",
};

export const viewport: Viewport = {
  themeColor: "#12081F",
  colorScheme: "dark",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <Providers>
          <a
            href="#main"
            className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-pill focus:bg-violet focus:px-6 focus:py-3 focus:text-label focus:text-ink"
          >
            Skip to content
          </a>
          <Sidebar />
          <main
            id="main"
            className="min-h-dvh px-4 pt-8 pb-[calc(var(--height-tab-bar)+2rem)] md:pl-[calc(var(--width-sidebar-collapsed)+24px)] md:pr-6 md:pb-12 lg:pl-[calc(var(--width-sidebar)+32px)] lg:pr-8"
          >
            <div className="mx-auto w-full max-w-(--width-content)">{children}</div>
          </main>
          <TabBar />
        </Providers>
      </body>
    </html>
  );
}
