const deriveApiBaseUrl = () => {
  const envValue = import.meta.env.VITE_API_BASE_URL?.trim();
  if (envValue) {
    return envValue.replace(/\/$/, "");
  }

  if (typeof window !== "undefined") {
    const { origin, port } = window.location;

    if (["5173", "5174", "4173", "4174"].includes(port)) {
      console.warn(
        "VITE_API_BASE_URL is not defined; assuming local backend at http://localhost:8080.",
      );
      return "http://localhost:8080";
    }

    console.warn("VITE_API_BASE_URL is not defined; falling back to window origin.");
    return origin;
  }

  console.warn("VITE_API_BASE_URL is not defined; falling back to http://localhost:8080.");
  return "http://localhost:8080";
};

export const API_BASE_URL = deriveApiBaseUrl();

export const buildWebSocketUrl = (path: string) => {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;

  try {
    const baseUrl = new URL(API_BASE_URL);
    const protocol = baseUrl.protocol === "https:" ? "wss:" : "ws:";
    return `${protocol}//${baseUrl.host}${normalizedPath}`;
  } catch (error) {
    if (API_BASE_URL.startsWith("https://")) {
      return `wss://${API_BASE_URL.replace(/^https:\/\//, "")}${normalizedPath}`;
    }

    if (API_BASE_URL.startsWith("http://")) {
      return `ws://${API_BASE_URL.replace(/^http:\/\//, "")}${normalizedPath}`;
    }

    return normalizedPath;
  }
};

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
};

type RequestOptions = {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
  auth?: boolean;
  responseType?: "json" | "text" | "blob" | "arrayBuffer";
};

export type ApiError = Error & { status?: number };

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
    const nextAccessToken = tokens?.accessToken ?? null;
    const nextRefreshToken = tokens?.refreshToken ?? null;
    const tokensChanged =
      nextAccessToken !== this.accessToken || nextRefreshToken !== this.refreshToken;

    if (!tokensChanged) {
      if (!tokens && typeof window !== "undefined") {
        window.localStorage.removeItem(TOKEN_STORAGE_KEY);
      }
      return;
    }

    this.accessToken = nextAccessToken;
    this.refreshToken = nextRefreshToken;

    if (!tokens) {
      if (typeof window !== "undefined") {
        window.localStorage.removeItem(TOKEN_STORAGE_KEY);
      }
      this.notifyListeners(null);
      return;
    }

    if (typeof window !== "undefined") {
      window.localStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(tokens));
    }

    this.notifyListeners(tokens);
  }

  async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const { method = "GET", body, headers = {}, auth = true, responseType } = options;

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
      const error = new Error(errorMessage) as ApiError;
      error.status = response.status;
      throw error;
    }

    if (response.status === 204) {
      return undefined as T;
    }

    const resolvedResponseType = responseType ?? (isJsonContent(response) ? "json" : "text");

    switch (resolvedResponseType) {
      case "json":
        return (await response.json()) as T;
      case "blob":
        return (await response.blob()) as T;
      case "arrayBuffer":
        return (await response.arrayBuffer()) as T;
      case "text":
      default:
        return (await response.text()) as T;
    }
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
