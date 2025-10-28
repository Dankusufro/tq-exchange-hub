import { useCallback, useEffect, useMemo, useState } from "react";
import { supabase } from "@/lib/supabase";
import type { Tables } from "@/integrations/supabase/types";

export type TradeRequest = Tables<"trades">;

type TradeRequestStatus = "accepted" | "rejected" | "pending";

interface UseTradeRequestsResult {
  requests: TradeRequest[];
  isLoading: boolean;
  error: string | null;
  acceptRequest: (id: string) => Promise<void>;
  rejectRequest: (id: string) => Promise<void>;
  refresh: () => Promise<void>;
}

const useTradeRequests = (options?: { status?: TradeRequestStatus | TradeRequestStatus[] }): UseTradeRequestsResult => {
  const [requests, setRequests] = useState<TradeRequest[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const statusFilter = useMemo(() => options?.status, [options?.status]);

  const fetchRequests = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    let query = supabase
      .from("trades")
      .select("*")
      .order("created_at", { ascending: false });

    if (statusFilter) {
      if (Array.isArray(statusFilter)) {
        query = query.in("status", statusFilter);
      } else {
        query = query.eq("status", statusFilter);
      }
    }

    const { data, error } = await query;

    if (error) {
      setError(error.message);
      setRequests([]);
    } else {
      setRequests(data ?? []);
    }

    setIsLoading(false);
  }, [statusFilter]);

  useEffect(() => {
    fetchRequests();
  }, [fetchRequests]);

  const updateStatus = useCallback(
    async (id: string, status: TradeRequestStatus) => {
      const { error } = await supabase
        .from("trades")
        .update({ status })
        .eq("id", id);

      if (error) {
        throw new Error(error.message);
      }

      setRequests((previous) =>
        previous.map((request) =>
          request.id === id
            ? {
                ...request,
                status,
              }
            : request,
        ),
      );
    },
    [],
  );

  const acceptRequest = useCallback(
    async (id: string) => {
      await updateStatus(id, "accepted");
    },
    [updateStatus],
  );

  const rejectRequest = useCallback(
    async (id: string) => {
      await updateStatus(id, "rejected");
    },
    [updateStatus],
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
