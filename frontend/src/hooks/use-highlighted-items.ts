import { useQuery } from "@tanstack/react-query";

import { apiClient } from "@/lib/api";

export interface ProfileSummary {
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
}

export interface ItemSummary {
  id: string;
  title: string;
  description: string;
  condition: string;
  location: string | null;
  categoryName: string;
  owner: ProfileSummary;
  rating: number | null;
  wishlist: string[];
  mainImageUrl: string | null;
  available: boolean | null;
  service: boolean | null;
}

interface PageResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

interface HighlightedItemsOptions {
  page?: number;
  size?: number;
}

export const useHighlightedItems = (options?: HighlightedItemsOptions) => {
  const page = options?.page ?? 0;
  const size = options?.size ?? 6;

  return useQuery({
    queryKey: ["highlightedItems", page, size],
    queryFn: async () => {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
      });

      const response = await apiClient.get<PageResponse<ItemSummary>>(
        `/api/items/highlighted?${params.toString()}`,
        { auth: false },
      );

      return {
        items: response.content,
        page: response.number,
        pageSize: response.size,
        totalPages: response.totalPages,
        totalItems: response.totalElements,
      };
    },
    staleTime: 1000 * 60,
  });
};

export type UseHighlightedItemsResult = ReturnType<typeof useHighlightedItems>;
