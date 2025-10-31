import { useCallback, useMemo } from "react";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { apiClient, type ApiError } from "@/lib/api";
import { useAuth } from "@/providers/AuthProvider";

export type TradeRequestStatus = "accepted" | "rejected" | "pending" | "cancelled";

export type TradeRequest = {
  id: string;
  owner_id: string;
  requester_id: string;
  owner_item_id: string;
  requester_item_id: string | null;
  message: string | null;
  status: TradeRequestStatus;
  created_at: string;
  updated_at: string;
};

interface TradeDto {
  id: string;
  ownerId: string;
  requesterId: string;
  ownerItemId: string;
  requesterItemId: string | null;
  message: string | null;
  status: "PENDING" | "ACCEPTED" | "REJECTED" | "CANCELLED";
  createdAt: string;
  updatedAt: string;
}

interface UseTradeRequestsOptions {
  status?: TradeRequestStatus | TradeRequestStatus[];
}

interface UseTradeRequestsResult {
  requests: TradeRequest[];
  isLoading: boolean;
  isFetching: boolean;
  error: string | null;
  acceptRequest: (id: string) => Promise<void>;
  rejectRequest: (id: string) => Promise<void>;
  cancelRequest: (id: string) => Promise<void>;
  refresh: () => Promise<void>;
}

const normalizeTrade = (trade: TradeDto): TradeRequest => ({
  id: trade.id,
  owner_id: trade.ownerId,
  requester_id: trade.requesterId,
  owner_item_id: trade.ownerItemId,
  requester_item_id: trade.requesterItemId,
  message: trade.message,
  status: trade.status.toLowerCase() as TradeRequestStatus,
  created_at: trade.createdAt,
  updated_at: trade.updatedAt,
});

const isUnauthorizedError = (error: unknown): error is ApiError =>
  typeof error === "object" && error !== null && (error as ApiError).status === 401;

const useTradeRequests = (options?: UseTradeRequestsOptions): UseTradeRequestsResult => {
  const { session, signOut } = useAuth();
  const queryClient = useQueryClient();

  const normalizedStatuses = useMemo(() => {
    if (!options?.status) {
      return undefined;
    }

    const rawStatuses = Array.isArray(options.status) ? options.status : [options.status];
    const uniqueStatuses = Array.from(new Set(rawStatuses));

    return uniqueStatuses.map((status) => status.toUpperCase());
  }, [options?.status]);

  const queryKey = useMemo(
    () => ["tradeRequests", session?.profile.id ?? null, normalizedStatuses ?? []] as const,
    [normalizedStatuses, session?.profile.id],
  );

  const handleRequestError = useCallback(
    async (error: unknown, fallbackMessage: string): Promise<never> => {
      if (isUnauthorizedError(error)) {
        await signOut();
      }

      if (error instanceof Error) {
        throw error;
      }

      throw new Error(fallbackMessage);
    },
    [signOut],
  );

  const fetchTrades = useCallback(async () => {
    try {
      const query = new URLSearchParams();

      normalizedStatuses?.forEach((status) => {
        query.append("status", status);
      });

      const endpoint = `/api/trades${query.size > 0 ? `?${query.toString()}` : ""}`;
      const response = await apiClient.get<TradeDto[]>(endpoint);
      return response.map(normalizeTrade);
    } catch (error) {
      await handleRequestError(error, "No se pudo obtener las solicitudes");
    }
  }, [handleRequestError, normalizedStatuses]);

  const tradesQuery = useQuery<TradeRequest[], Error>({
    queryKey,
    queryFn: fetchTrades,
    enabled: Boolean(session),
  });

  const updateCachedTrade = useCallback(
    (updatedTrade: TradeRequest) => {
      queryClient.setQueryData<TradeRequest[] | undefined>(queryKey, (previous = []) =>
        previous.map((trade) => (trade.id === updatedTrade.id ? updatedTrade : trade)),
      );
    },
    [queryClient, queryKey],
  );

  const acceptMutation = useMutation({
    mutationFn: async (id: string) => {
      try {
        const response = await apiClient.post<TradeDto>(`/api/trades/${id}/accept`);
        return normalizeTrade(response);
      } catch (error) {
        await handleRequestError(error, "No se pudo aceptar la solicitud");
      }
    },
    onSuccess: (updatedTrade) => {
      if (updatedTrade) {
        updateCachedTrade(updatedTrade);
      }
    },
  });

  const rejectMutation = useMutation({
    mutationFn: async (id: string) => {
      try {
        const response = await apiClient.post<TradeDto>(`/api/trades/${id}/reject`);
        return normalizeTrade(response);
      } catch (error) {
        await handleRequestError(error, "No se pudo rechazar la solicitud");
      }
    },
    onSuccess: (updatedTrade) => {
      if (updatedTrade) {
        updateCachedTrade(updatedTrade);
      }
    },
  });

  const acceptRequest = useCallback<UseTradeRequestsResult["acceptRequest"]>(
    async (id) => {
      try {
        await acceptMutation.mutateAsync(id);
      } catch (error) {
        throw error instanceof Error ? error : new Error("No se pudo aceptar la solicitud");
      }
    },
    [acceptMutation],
  );

  const rejectRequest = useCallback<UseTradeRequestsResult["rejectRequest"]>(
    async (id) => {
      try {
        await rejectMutation.mutateAsync(id);
      } catch (error) {
        throw error instanceof Error ? error : new Error("No se pudo rechazar la solicitud");
      }
    },
    [rejectMutation],
  );

  const cancelMutation = useMutation({
    mutationFn: async (id: string) => {
      try {
        const response = await apiClient.post<TradeDto>(`/api/trades/${id}/cancel`);
        return normalizeTrade(response);
      } catch (error) {
        await handleRequestError(error, "No se pudo cancelar la solicitud");
      }
    },
    onSuccess: (updatedTrade) => {
      if (updatedTrade) {
        updateCachedTrade(updatedTrade);
      }
    },
  });

  const cancelRequest = useCallback<UseTradeRequestsResult["cancelRequest"]>(
    async (id) => {
      try {
        await cancelMutation.mutateAsync(id);
      } catch (error) {
        throw error instanceof Error ? error : new Error("No se pudo cancelar la solicitud");
      }
    },
    [cancelMutation],
  );

  const refresh = useCallback(async () => {
    if (!session) {
      return;
    }

    await tradesQuery.refetch();
  }, [session, tradesQuery]);

  const hasSession = Boolean(session);

  return {
    requests: hasSession ? tradesQuery.data ?? [] : [],
    isLoading: hasSession && (tradesQuery.isLoading || tradesQuery.isPending),
    isFetching: hasSession && tradesQuery.isFetching,
    error: hasSession ? tradesQuery.error?.message ?? null : null,
    acceptRequest,
    rejectRequest,
    cancelRequest,
    refresh,
  };
};

export default useTradeRequests;
