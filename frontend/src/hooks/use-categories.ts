import { useQuery } from "@tanstack/react-query";

import { apiClient } from "@/lib/api";

export interface CategorySummary {
  id: string;
  name: string;
  description: string | null;
  icon: string | null;
  createdAt: string | null;
  itemsCount: number;
}

export const useCategories = () =>
  useQuery({
    queryKey: ["categories"],
    queryFn: async () =>
      apiClient.get<CategorySummary[]>("/api/categories", { auth: false }),
    staleTime: 1000 * 60,
  });

export type UseCategoriesResult = ReturnType<typeof useCategories>;
