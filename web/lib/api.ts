/** Thrown for any non-2xx answer, so screens can tell a missing song (404) from a failure a retry can fix. */
export class ApiError extends Error {
  constructor(readonly status: number) {
    super(`API request failed with ${status}`);
    this.name = "ApiError";
  }
}

/** Same origin: the edge (production) or the Vite proxy (development) sends /api to the backend. */
export async function getJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, { ...init, headers: { Accept: "application/json", ...init?.headers } });
  if (!response.ok) throw new ApiError(response.status);
  return (await response.json()) as T;
}

export const isNotFound = (error: unknown) => error instanceof ApiError && error.status === 404;
