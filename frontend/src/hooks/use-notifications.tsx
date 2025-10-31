import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";

import { useQuery } from "@tanstack/react-query";

import { apiClient, buildWebSocketUrl, type ApiError } from "@/lib/api";
import { buildFrame, createSubscriptionId, parseFrames } from "@/lib/stomp";
import { useAuth } from "@/providers/AuthProvider";

export type NotificationCategory = "trade" | "message" | "system" | "alert";

export interface Notification {
  id: string;
  title: string;
  description: string;
  createdAt: string;
  read: boolean;
  category: NotificationCategory;
  tradeId?: string | null;
  messageId?: string | null;
  href?: string;
}

interface NotificationDto {
  id: string;
  type: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  tradeId?: string | null;
  messageId?: string | null;
}

interface NotificationsContextValue {
  notifications: Notification[];
  unreadCount: number;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  markNotificationsAsRead: (ids: string[]) => Promise<void>;
  addNotification: (
    notification: Omit<Notification, "id" | "read" | "createdAt"> & {
      id?: string;
      read?: boolean;
      createdAt?: string;
    },
  ) => void;
}

const notificationTypeMap: Record<string, NotificationCategory> = {
  MESSAGE: "message",
  TRADE: "trade",
};

const normalizeNotification = (dto: NotificationDto): Notification => ({
  id: dto.id,
  title: dto.title,
  description: dto.message,
  createdAt: dto.createdAt,
  read: Boolean(dto.read),
  category: notificationTypeMap[dto.type] ?? "system",
  tradeId: dto.tradeId ?? null,
  messageId: dto.messageId ?? null,
});

const createNotificationId = () => {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }

  return Math.random().toString(36).slice(2, 12);
};

const isUnauthorizedError = (error: unknown): error is ApiError =>
  typeof error === "object" && error !== null && (error as ApiError).status === 401;

const NotificationsContext = createContext<NotificationsContextValue | undefined>(undefined);

export const NotificationsProvider = ({ children }: { children: ReactNode }) => {
  const { session, signOut } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const profileId = session?.profile.id ?? null;
  const token = session?.tokens.accessToken ?? null;
  const isBrowser = typeof window !== "undefined";
  const socketRef = useRef<WebSocket | null>(null);
  const reconnectRef = useRef<number | null>(null);

  const handleRequestError = useCallback(
    async (error: unknown, fallbackMessage: string) => {
      if (isUnauthorizedError(error)) {
        await signOut();
        return;
      }

      console.error(fallbackMessage, error);
    },
    [signOut],
  );

  const notificationsQuery = useQuery<Notification[]>({
    queryKey: ["notifications", profileId],
    queryFn: async () => {
      try {
        const response = await apiClient.get<NotificationDto[]>("/api/notifications");
        return response.map(normalizeNotification);
      } catch (error) {
        await handleRequestError(error, "No se pudieron cargar las notificaciones");
        return [];
      }
    },
    enabled: Boolean(profileId),
    staleTime: 30_000,
  });

  useEffect(() => {
    if (!profileId) {
      setNotifications([]);
      return;
    }

    if (notificationsQuery.data) {
      setNotifications(notificationsQuery.data);
    }
  }, [notificationsQuery.data, profileId]);

  const updateNotifications = useCallback((incoming: Notification) => {
    setNotifications((previous) => {
      const index = previous.findIndex((notification) => notification.id === incoming.id);
      if (index >= 0) {
        const next = [...previous];
        next[index] = { ...next[index], ...incoming };
        return next;
      }

      return [incoming, ...previous];
    });
  }, []);

  const cleanupSocket = useCallback(() => {
    if (reconnectRef.current && typeof window !== "undefined") {
      window.clearTimeout(reconnectRef.current);
      reconnectRef.current = null;
    }

    const socket = socketRef.current;
    if (!socket) {
      return;
    }

    try {
      if (socket.readyState === WebSocket.OPEN && profileId) {
        const subscriptionId = createSubscriptionId("notifications", profileId);
        const unsubscribeFrame = buildFrame("UNSUBSCRIBE", { id: subscriptionId });
        socket.send(unsubscribeFrame);
        const disconnectFrame = buildFrame("DISCONNECT", {});
        socket.send(disconnectFrame);
      }
    } catch (error) {
      console.error("Error closing notifications socket", error);
    }

    socket.close();
    socketRef.current = null;
  }, [profileId]);

  useEffect(() => {
    if (!profileId || !token || !isBrowser) {
      cleanupSocket();
      return;
    }

    let isDisposed = false;
    const subscriptionId = createSubscriptionId("notifications", profileId);

    const connect = () => {
      if (isDisposed) {
        return;
      }

      const socket = new WebSocket(buildWebSocketUrl("/ws"));
      socketRef.current = socket;

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
              const subscribeFrame = buildFrame("SUBSCRIBE", {
                id: subscriptionId,
                destination: `/topic/profiles/${profileId}/notifications`,
              });
              socket.send(subscribeFrame);
              break;
            }
            case "MESSAGE": {
              try {
                const payload = JSON.parse(frame.body) as NotificationDto;
                updateNotifications(normalizeNotification(payload));
              } catch (error) {
                console.error("Failed to parse notification payload", error);
              }
              break;
            }
            case "ERROR": {
              console.error("Notifications socket error", frame.body);
              break;
            }
            default:
              break;
          }
        });
      };

      socket.onclose = () => {
        socketRef.current = null;
        if (isDisposed) {
          return;
        }

        if (typeof window !== "undefined") {
          reconnectRef.current = window.setTimeout(() => {
            connect();
          }, 5000);
        }
      };

      socket.onerror = (event) => {
        console.error("WebSocket error on notifications channel", event);
      };
    };

    connect();

    return () => {
      isDisposed = true;
      cleanupSocket();
    };
  }, [cleanupSocket, isBrowser, profileId, token, updateNotifications]);

  const markNotificationsAsRead = useCallback(
    async (ids: string[]) => {
      if (ids.length === 0) {
        return;
      }

      try {
        await apiClient.post<void>("/api/notifications/read", { ids });
        setNotifications((previous) =>
          previous.map((notification) =>
            ids.includes(notification.id) ? { ...notification, read: true } : notification,
          ),
        );
      } catch (error) {
        await handleRequestError(error, "No se pudieron actualizar las notificaciones");
      }
    },
    [handleRequestError],
  );

  const markAsRead = useCallback(
    async (id: string) => {
      await markNotificationsAsRead([id]);
    },
    [markNotificationsAsRead],
  );

  const markAllAsRead = useCallback(async () => {
    if (!notifications.some((notification) => !notification.read)) {
      return;
    }

    try {
      await apiClient.post<void>("/api/notifications/read-all");
      setNotifications((previous) => previous.map((notification) => ({ ...notification, read: true })));
    } catch (error) {
      await handleRequestError(error, "No se pudieron actualizar las notificaciones");
    }
  }, [handleRequestError, notifications]);

  const addNotification = useCallback<NotificationsContextValue["addNotification"]>(
    (notification) => {
      const next: Notification = {
        ...notification,
        id: notification.id ?? createNotificationId(),
        createdAt: notification.createdAt ?? new Date().toISOString(),
        read: notification.read ?? false,
      };
      updateNotifications(next);
    },
    [updateNotifications],
  );

  const unreadCount = useMemo(
    () => notifications.reduce((count, notification) => (notification.read ? count : count + 1), 0),
    [notifications],
  );

  const value = useMemo(
    () => ({
      notifications,
      unreadCount,
      markAsRead,
      markAllAsRead,
      markNotificationsAsRead,
      addNotification,
    }),
    [notifications, unreadCount, markAsRead, markAllAsRead, markNotificationsAsRead, addNotification],
  );

  return <NotificationsContext.Provider value={value}>{children}</NotificationsContext.Provider>;
};

export const useNotifications = () => {
  const context = useContext(NotificationsContext);

  if (context === undefined) {
    throw new Error("useNotifications debe utilizarse dentro de un NotificationsProvider");
  }

  return context;
};
