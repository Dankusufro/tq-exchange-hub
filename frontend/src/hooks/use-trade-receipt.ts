import { useCallback, useState } from "react";

import { apiClient } from "@/lib/api";

interface UseTradeReceiptResult {
  isDownloading: boolean;
  isSending: boolean;
  downloadReceipt: (tradeId: string) => Promise<void>;
  emailReceipt: (tradeId: string, email: string) => Promise<void>;
}

const useTradeReceipt = (): UseTradeReceiptResult => {
  const [isDownloading, setIsDownloading] = useState(false);
  const [isSending, setIsSending] = useState(false);

  const downloadReceipt = useCallback(async (tradeId: string) => {
    setIsDownloading(true);
    try {
      const blob = await apiClient.request<Blob>(`/api/trades/${tradeId}/receipt`, {
        responseType: "blob",
        headers: {
          Accept: "application/pdf",
        },
      });

      const fileName = `trade-${tradeId}-receipt.pdf`;
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } finally {
      setIsDownloading(false);
    }
  }, []);

  const emailReceipt = useCallback(async (tradeId: string, email: string) => {
    setIsSending(true);
    try {
      await apiClient.post(`/api/trades/${tradeId}/receipt/email`, { email });
    } finally {
      setIsSending(false);
    }
  }, []);

  return { isDownloading, isSending, downloadReceipt, emailReceipt };
};

export default useTradeReceipt;
