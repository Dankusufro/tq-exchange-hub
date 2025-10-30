import { useCallback, useEffect, useMemo, useRef, useState } from "react";

import { buildWebSocketUrl } from "@/lib/api";
import { buildFrame, createSubscriptionId, parseFrames } from "@/lib/stomp";
import { useAuth } from "@/providers/AuthProvider";

export interface ChatMessage {
  id: string;
  tradeId: string;
  senderId: string;
  content: string;
  createdAt: string;
}

export type ChatConnectionState = "disconnected" | "connecting" | "connected";

interface UseChatChannelOptions {
  tradeId: string;
  enabled?: boolean;
  onMessage?: (message: ChatMessage) => void;
}

export const useChatChannel = ({ tradeId, enabled = true, onMessage }: UseChatChannelOptions) => {
  const { session } = useAuth();
  const token = session?.tokens.accessToken ?? null;
  const [state, setState] = useState<ChatConnectionState>("disconnected");
  const websocketRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<number | null>(null);
  const messageHandlerRef = useRef<typeof onMessage>(onMessage);
  const subscriptionId = useMemo(() => createSubscriptionId("trade", tradeId), [tradeId]);
  const isBrowser = typeof window !== "undefined";

  useEffect(() => {
    messageHandlerRef.current = onMessage;
  }, [onMessage]);

  const cleanupConnection = useCallback(() => {
    if (reconnectTimeoutRef.current && typeof window !== "undefined") {
      window.clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    const socket = websocketRef.current;
    if (!socket) {
      return;
    }

    try {
      if (socket.readyState === WebSocket.OPEN) {
        const unsubscribeFrame = buildFrame("UNSUBSCRIBE", { id: subscriptionId });
        socket.send(unsubscribeFrame);
        const disconnectFrame = buildFrame("DISCONNECT", {});
        socket.send(disconnectFrame);
      }
    } catch (error) {
      console.error("Error while closing STOMP connection", error);
    }

    socket.close();
    websocketRef.current = null;
    setState("disconnected");
  }, [subscriptionId]);

  useEffect(() => {
    if (!enabled || !tradeId || !token || !isBrowser) {
      cleanupConnection();
      return;
    }

    let isDisposed = false;

    const connect = () => {
      if (isDisposed) {
        return;
      }

      setState("connecting");

      const socket = new WebSocket(buildWebSocketUrl("/ws"));
      websocketRef.current = socket;

      socket.onopen = () => {
        const connectFrame = buildFrame("CONNECT", {
          "accept-version": "1.2",
          "heart-beat": "0,0",
          Authorization: `Bearer ${token}`,
        });
        socket.send(connectFrame);
      };

      socket.onmessage = (event) => {
        const rawPayload = typeof event.data === "string" ? event.data : "";
        const frames = parseFrames(rawPayload);

        frames.forEach((frame) => {
          switch (frame.command) {
            case "CONNECTED": {
              setState("connected");
              const subscribeFrame = buildFrame("SUBSCRIBE", {
                id: subscriptionId,
                destination: `/topic/trades/${tradeId}/messages`,
              });
              socket.send(subscribeFrame);
              break;
            }
            case "MESSAGE": {
              try {
                const payload = JSON.parse(frame.body) as ChatMessage;
                messageHandlerRef.current?.(payload);
              } catch (error) {
                console.error("Failed to parse chat message", error);
              }
              break;
            }
            case "ERROR": {
              console.error("STOMP error", frame.body);
              break;
            }
            default:
              break;
          }
        });
      };

      socket.onclose = () => {
        websocketRef.current = null;
        if (isDisposed) {
          return;
        }

        setState("disconnected");
        if (typeof window !== "undefined") {
          reconnectTimeoutRef.current = window.setTimeout(() => {
            connect();
          }, 5000);
        }
      };

      socket.onerror = (event) => {
        console.error("WebSocket error", event);
      };
    };

    connect();

    return () => {
      isDisposed = true;
      cleanupConnection();
    };
  }, [cleanupConnection, enabled, isBrowser, subscriptionId, token, tradeId]);

  return {
    state,
    isConnected: state === "connected",
  };
};
