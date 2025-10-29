import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { http, HttpResponse } from "msw";
import { setupServer } from "msw/node";
import { ReactNode } from "react";
import { beforeAll, beforeEach, afterEach, afterAll, describe, expect, it, vi } from "vitest";

import useTradeRequests from "./use-trade-requests";

type Profile = {
  id: string;
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
  location: string | null;
  phone: string | null;
  rating: number | null;
  totalTrades: number | null;
  createdAt: string | null;
  updatedAt: string | null;
};

type Session = {
  tokens: {
    accessToken: string;
    refreshToken: string;
  };
  profile: Profile;
};

const server = setupServer();

const signOutMock = vi.fn<[], Promise<void>>();

const authState: {
  user: Profile | null;
  session: Session | null;
  signOut: typeof signOutMock;
  signIn: ReturnType<typeof vi.fn>;
  signUp: ReturnType<typeof vi.fn>;
} = {
  user: null,
  session: null,
  signOut: signOutMock,
  signIn: vi.fn(),
  signUp: vi.fn(),
};

vi.mock("@/providers/AuthProvider", () => ({
  useAuth: () => authState,
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

const baseProfile: Profile = {
  id: "profile-1",
  displayName: "Test User",
  bio: null,
  avatarUrl: null,
  location: null,
  phone: null,
  rating: null,
  totalTrades: null,
  createdAt: null,
  updatedAt: null,
};

beforeAll(() => {
  server.listen();
});

beforeEach(() => {
  signOutMock.mockReset();
  signOutMock.mockResolvedValue();
  const profile: Profile = { ...baseProfile };
  authState.user = profile;
  authState.session = {
    tokens: { accessToken: "access-token", refreshToken: "refresh-token" },
    profile,
  };
});

afterEach(() => {
  server.resetHandlers();
});

afterAll(() => {
  server.close();
});

describe("useTradeRequests", () => {
  it("fetches trade requests applying multiple status filters", async () => {
    let receivedStatuses: string[] = [];

    server.use(
      http.get("http://localhost:8080/api/trades", ({ request }) => {
        receivedStatuses = request.url.searchParams.getAll("status");
        return HttpResponse.json([
          {
            id: "1",
            ownerId: "profile-1",
            requesterId: "profile-2",
            ownerItemId: "item-1",
            requesterItemId: null,
            message: "Hola",
            status: "PENDING",
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
          {
            id: "2",
            ownerId: "profile-1",
            requesterId: "profile-3",
            ownerItemId: "item-2",
            requesterItemId: null,
            message: null,
            status: "ACCEPTED",
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
        ]);
      }),
    );

    const wrapper = createWrapper();
    const { result } = renderHook(() => useTradeRequests({ status: ["pending", "accepted"] }), {
      wrapper,
    });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.requests).toHaveLength(2);
    expect(receivedStatuses).toEqual(["PENDING", "ACCEPTED"]);
  });

  it("signs out the user when the server responds with 401", async () => {
    signOutMock.mockResolvedValue();

    server.use(
      http.get("http://localhost:8080/api/trades", () =>
        HttpResponse.json(
          {
            message: "Sesión expirada",
          },
          { status: 401 },
        ),
      ),
    );

    const wrapper = createWrapper();
    const { result } = renderHook(() => useTradeRequests(), { wrapper });

    await waitFor(() => expect(result.current.error).not.toBeNull());

    expect(signOutMock).toHaveBeenCalled();
    expect(result.current.requests).toHaveLength(0);
  });

  it("does not trigger the request when there is no active session", async () => {
    authState.user = null;
    authState.session = null;
    const handler = vi.fn(() => HttpResponse.json([]));

    server.use(http.get("http://localhost:8080/api/trades", handler));

    const wrapper = createWrapper();
    const { result } = renderHook(() => useTradeRequests({ status: "pending" }), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(handler).not.toHaveBeenCalled();
    expect(result.current.requests).toHaveLength(0);
    expect(result.current.error).toBeNull();
  });
});
