import { screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";

import Categories from "../Categories";
import { renderWithQueryClient } from "@/test/render-with-query-client";
import type { CategorySummary } from "@/hooks/use-categories";

vi.mock("@/lib/api", () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

const { apiClient } = await import("@/lib/api");

const mockGet = apiClient.get as ReturnType<typeof vi.fn>;

describe("Categories", () => {
  beforeEach(() => {
    mockGet.mockReset();
  });

  it("shows a loading indicator while requesting categories", () => {
    mockGet.mockReturnValue(new Promise(() => {}));

    renderWithQueryClient(<Categories />);

    expect(screen.getByText("Cargando categorías...")).toBeInTheDocument();
  });

  it("renders the categories returned by the API", async () => {
    const categories: CategorySummary[] = [
      {
        id: "cat-1",
        name: "Electrónicos",
        description: "Gadgets y tecnología",
        icon: "lucide:smartphone",
        createdAt: null,
        itemsCount: 1532,
      },
      {
        id: "cat-2",
        name: "Servicios",
        description: "Talentos disponibles",
        icon: "lucide:wrench",
        createdAt: null,
        itemsCount: 87,
      },
    ];

    mockGet.mockResolvedValue(categories);

    renderWithQueryClient(<Categories />);

    await waitFor(() => {
      expect(screen.getByText("Electrónicos")).toBeInTheDocument();
      expect(screen.getByText("1,532 items")).toBeInTheDocument();
      expect(screen.getByText("Servicios")).toBeInTheDocument();
      expect(screen.getByText("87 items")).toBeInTheDocument();
    });

    expect(mockGet).toHaveBeenCalledWith("/api/categories", { auth: false });
  });
});
