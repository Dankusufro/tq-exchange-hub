import { screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

import FeaturedProducts from "../FeaturedProducts";
import { renderWithQueryClient } from "@/test/render-with-query-client";
import type { ItemSummary } from "@/hooks/use-highlighted-items";

vi.mock("@/lib/api", () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

const { apiClient } = await import("@/lib/api");

const mockGet = apiClient.get as ReturnType<typeof vi.fn>;

const createPageResponse = (items: ItemSummary[]) => ({
  content: items,
  number: 0,
  size: items.length,
  totalElements: items.length,
  totalPages: 1,
});

describe("FeaturedProducts", () => {
  beforeEach(() => {
    mockGet.mockReset();
  });

  it("shows a loading message while fetching highlighted items", () => {
    mockGet.mockReturnValue(new Promise(() => {}));

    renderWithQueryClient(<FeaturedProducts />);

    expect(
      screen.getByText("Cargando intercambios destacados..."),
    ).toBeInTheDocument();
  });

  it("renders products provided by the API", async () => {
    const items: ItemSummary[] = [
      {
        id: "item-1",
        title: "Bicicleta de montaña Trek",
        description: "Lista para rutas exigentes.",
        condition: "Muy buena",
        location: "Ciudad de México",
        categoryName: "Deportes",
        owner: {
          id: "owner-1",
          displayName: "Carlos Mendoza",
          bio: null,
          avatarUrl: null,
          location: "Ciudad de México",
          phone: null,
          rating: 4.8,
          totalTrades: 10,
          createdAt: null,
          updatedAt: null,
        },
        rating: 4.8,
        wishlist: ["Laptop", "Cámara fotográfica"],
        mainImageUrl: "https://example.com/bike.jpg",
        available: true,
        service: false,
      },
    ];

    mockGet.mockResolvedValue(createPageResponse(items));

    renderWithQueryClient(<FeaturedProducts />);

    await waitFor(() => {
      expect(screen.getByText("Bicicleta de montaña Trek")).toBeInTheDocument();
    });

    expect(mockGet).toHaveBeenCalledWith(
      "/api/items/highlighted?page=0&size=6",
      { auth: false },
    );
  });

  it("renders an empty state when the API returns no items", async () => {
    mockGet.mockResolvedValue(createPageResponse([]));

    renderWithQueryClient(<FeaturedProducts />);

    await waitFor(() => {
      expect(
        screen.getByText("Aún no hay intercambios destacados. ¡Sé el primero en publicar!"),
      ).toBeInTheDocument();
    });
  });
});
