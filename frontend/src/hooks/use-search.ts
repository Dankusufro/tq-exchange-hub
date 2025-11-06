import { useQuery } from "@tanstack/react-query";

import { apiClient } from "@/lib/api";
import type { CategorySummary } from "@/hooks/use-categories";
import type { ItemSummary } from "@/hooks/use-highlighted-items";

export interface SearchResponse {
  query: string;
  categories: CategorySummary[];
  items: ItemSummary[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface UseSearchOptions {
  query: string;
  categoryId?: string | null;
  page?: number;
  size?: number;
  enabled?: boolean;
  minQueryLength?: number;
  allowEmptyQuery?: boolean;
}

export const useSearch = ({
  query,
  categoryId,
  page = 0,
  size = 5,
  enabled,
  minQueryLength = 2,
  allowEmptyQuery = false,
}: UseSearchOptions) => {
  const trimmedQuery = query?.trim() ?? "";
  const shouldFetch =
    enabled ??
    (trimmedQuery.length >= minQueryLength || (allowEmptyQuery && !!categoryId));

  return useQuery({
    queryKey: ["search", trimmedQuery, categoryId, page, size],
    queryFn: async () => {
      const params = new URLSearchParams();
      params.set("page", Math.max(page, 0).toString());
      params.set("size", Math.max(size, 1).toString());
      if (categoryId) {
        params.set("categoryId", categoryId);
      }
      params.set("q", trimmedQuery);

      return apiClient.get<SearchResponse>(`/api/search?${params.toString()}`, { auth: false });
    },
    enabled: shouldFetch,
    staleTime: 1000 * 30,
    retry: false,
  });
};

export type UseSearchResult = ReturnType<typeof useSearch>;
