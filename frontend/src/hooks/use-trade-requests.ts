import { useCallback, useEffect, useMemo, useState } from "react";

import { apiClient } from "@/lib/api";

export type TradeRequestStatus = "accepted" | "rejected" | "pending";

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
  status: "PENDING" | "ACCEPTED" | "REJECTED";
  createdAt: string;
  updatedAt: string;
}

interface UseTradeRequestsOptions {
  status?: TradeRequestStatus | TradeRequestStatus[];
}

interface UseTradeRequestsResult {
  requests: TradeRequest[];
  isLoading: boolean;
  error: string | null;
  acceptRequest: (id: string) => Promise<void>;
  rejectRequest: (id: string) => Promise<void>;
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

const useTradeRequests = (options?: UseTradeRequestsOptions): UseTradeRequestsResult => {
  const [requests, setRequests] = useState<TradeRequest[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const statusFilter = useMemo(() => options?.status, [options?.status]);

  const fetchRequests = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const query = new URLSearchParams();

      if (statusFilter && !Array.isArray(statusFilter)) {
        query.set("status", statusFilter.toUpperCase());
      }

      const endpoint = `/api/trades${query.size > 0 ? `?${query.toString()}` : ""}`;

      const response = await apiClient.get<TradeDto[]>(endpoint);
      setRequests(response.map(normalizeTrade));
    } catch (requestError) {
      const message = requestError instanceof Error ? requestError.message : "No se pudo obtener las solicitudes";
      setError(message);
      setRequests([]);
    } finally {
      setIsLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => {
    void fetchRequests();
  }, [fetchRequests]);

  const updateStatusLocally = useCallback((updatedRequest: TradeRequest) => {
    setRequests((previous) =>
      previous.map((request) => (request.id === updatedRequest.id ? updatedRequest : request)),
    );
  }, []);

  const acceptRequest = useCallback<UseTradeRequestsResult["acceptRequest"]>(
    async (id) => {
      try {
        const response = await apiClient.post<TradeDto>(`/api/trades/${id}/accept`);
        updateStatusLocally(normalizeTrade(response));
      } catch (requestError) {
        const message = requestError instanceof Error ? requestError.message : "No se pudo aceptar la solicitud";
        throw new Error(message);
      }
    },
    [updateStatusLocally],
  );

  const rejectRequest = useCallback<UseTradeRequestsResult["rejectRequest"]>(
    async (id) => {
      try {
        const response = await apiClient.post<TradeDto>(`/api/trades/${id}/reject`);
        updateStatusLocally(normalizeTrade(response));
      } catch (requestError) {
        const message = requestError instanceof Error ? requestError.message : "No se pudo rechazar la solicitud";
        throw new Error(message);
      }
    },
    [updateStatusLocally],
  );

  return {
    requests,
    isLoading,
    error,
    acceptRequest,
    rejectRequest,
    refresh: fetchRequests,
  };
};

export default useTradeRequests;
