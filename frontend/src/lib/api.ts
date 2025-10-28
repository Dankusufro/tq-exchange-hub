const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

if (!API_BASE_URL) {
  throw new Error("VITE_API_BASE_URL is not defined");
}

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
};

type RequestOptions = {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
  auth?: boolean;
};

type TokenListener = (tokens: AuthTokens | null) => void;

const TOKEN_STORAGE_KEY = "exchangehub.tokens";

const isJsonContent = (response: Response) => {
  const contentType = response.headers.get("content-type");
  return contentType?.includes("application/json");
};

const serializeBody = (body: unknown) => {
  if (body === undefined || body === null) {
    return undefined;
  }

  if (body instanceof FormData) {
    return body;
  }

  return JSON.stringify(body);
};

export class APIClient {
  private accessToken: string | null = null;

  private refreshToken: string | null = null;

  private listeners: Set<TokenListener> = new Set();

  private refreshPromise: Promise<AuthTokens | null> | null = null;

  constructor() {
    if (typeof window !== "undefined") {
      const storedTokens = window.localStorage.getItem(TOKEN_STORAGE_KEY);
      if (storedTokens) {
        try {
          const parsed = JSON.parse(storedTokens) as AuthTokens;
          this.accessToken = parsed.accessToken ?? null;
          this.refreshToken = parsed.refreshToken ?? null;
        } catch (error) {
          console.error("Failed to parse stored auth tokens", error);
          window.localStorage.removeItem(TOKEN_STORAGE_KEY);
        }
      }
    }
  }

  subscribe(listener: TokenListener) {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  getTokens(): AuthTokens | null {
    if (!this.accessToken || !this.refreshToken) {
      return null;
    }

    return {
      accessToken: this.accessToken,
      refreshToken: this.refreshToken,
    };
  }

  setTokens(tokens: AuthTokens | null) {
    if (!tokens) {
      this.accessToken = null;
      this.refreshToken = null;
      if (typeof window !== "undefined") {
        window.localStorage.removeItem(TOKEN_STORAGE_KEY);
      }
      this.notifyListeners(null);
      return;
    }

    this.accessToken = tokens.accessToken;
    this.refreshToken = tokens.refreshToken;

    if (typeof window !== "undefined") {
      window.localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(tokens));
    }

    this.notifyListeners(tokens);
  }

  async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const { method = "GET", body, headers = {}, auth = true } = options;

    const serializedBody = serializeBody(body);

    const requestHeaders: Record<string, string> = {
      Accept: "application/json",
      ...headers,
    };

    if (!(serializedBody instanceof FormData)) {
      requestHeaders["Content-Type"] = requestHeaders["Content-Type"] ?? "application/json";
    }

    if (auth && this.accessToken) {
      requestHeaders.Authorization = `Bearer ${this.accessToken}`;
    }

    const requestInit: RequestInit = {
      method,
      headers: requestHeaders,
      body: serializedBody,
    };

    let response = await fetch(`${API_BASE_URL}${path}`, requestInit);

    if (response.status === 401 && auth && this.refreshToken) {
      const refreshedTokens = await this.refreshAccessToken();

      if (refreshedTokens?.accessToken) {
        requestInit.headers = {
          ...requestHeaders,
          Authorization: `Bearer ${refreshedTokens.accessToken}`,
        };

        response = await fetch(`${API_BASE_URL}${path}`, requestInit);
      }
    }

    if (!response.ok) {
      const errorMessage = await this.parseErrorMessage(response);
      throw new Error(errorMessage);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    if (isJsonContent(response)) {
      return (await response.json()) as T;
    }

    return (await response.text()) as T;
  }

  get<T>(path: string, options: Omit<RequestOptions, "method"> = {}) {
    return this.request<T>(path, { ...options, method: "GET" });
  }

  post<T>(path: string, body?: unknown, options: Omit<RequestOptions, "method" | "body"> = {}) {
    return this.request<T>(path, { ...options, method: "POST", body });
  }

  put<T>(path: string, body?: unknown, options: Omit<RequestOptions, "method" | "body"> = {}) {
    return this.request<T>(path, { ...options, method: "PUT", body });
  }

  patch<T>(path: string, body?: unknown, options: Omit<RequestOptions, "method" | "body"> = {}) {
    return this.request<T>(path, { ...options, method: "PATCH", body });
  }

  delete<T>(path: string, options: Omit<RequestOptions, "method"> = {}) {
    return this.request<T>(path, { ...options, method: "DELETE" });
  }

  private async refreshAccessToken(): Promise<AuthTokens | null> {
    if (!this.refreshToken) {
      return null;
    }

    if (!this.refreshPromise) {
      this.refreshPromise = this.performRefresh().finally(() => {
        this.refreshPromise = null;
      });
    }

    return this.refreshPromise;
  }

  private async performRefresh(): Promise<AuthTokens | null> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ refreshToken: this.refreshToken }),
      });

      if (!response.ok) {
        this.setTokens(null);
        return null;
      }

      const data = (await response.json()) as { accessToken?: string; refreshToken?: string };

      if (!data.accessToken || !data.refreshToken) {
        this.setTokens(null);
        return null;
      }

      const tokens: AuthTokens = {
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
      };

      this.setTokens(tokens);

      return tokens;
    } catch (error) {
      console.error("Failed to refresh access token", error);
      this.setTokens(null);
      return null;
    }
  }

  private async parseErrorMessage(response: Response) {
    if (isJsonContent(response)) {
      try {
        const parsed = await response.json();
        if (typeof parsed === "string") {
          return parsed;
        }

        if (parsed?.message) {
          return parsed.message as string;
        }

        if (parsed?.error) {
          return parsed.error as string;
        }

        if (parsed?.errors && Array.isArray(parsed.errors)) {
          return parsed.errors.join(", ");
        }
      } catch (error) {
        console.error("Failed to parse error response", error);
      }
    }

    const text = await response.text();
    return text || `Request failed with status ${response.status}`;
  }

  private notifyListeners(tokens: AuthTokens | null) {
    this.listeners.forEach((listener) => {
      listener(tokens);
    });
  }
}

export const apiClient = new APIClient();
