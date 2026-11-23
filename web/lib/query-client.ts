import { QueryClient } from "@tanstack/react-query";

/** Query keys for published songs. They change only on republish, and then get a new version. */
export const songKeys = {
  all: ["songs"] as const,
  page: (level: string | undefined, page: number) => ["songs", "page", level ?? "all", page] as const,
  /** The library's pages for one level (or all), loaded one after another. */
  list: (level: string | undefined) => ["songs", "list", level ?? "all"] as const,
  detail: (slug: string) => ["songs", "detail", slug] as const,
};

/**
 * One client per browser tab. Published songs never go stale within a session (ADR-0011);
 * everything else uses a short staleTime and refetches after mutations.
 */
export function makeQueryClient(): QueryClient {
  const client = new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        refetchOnWindowFocus: false,
        retry: 1,
      },
    },
  });
  client.setQueryDefaults(songKeys.all, { staleTime: Infinity, gcTime: 30 * 60_000 });
  return client;
}
