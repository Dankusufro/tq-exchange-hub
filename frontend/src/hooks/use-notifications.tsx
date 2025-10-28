import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";

export type NotificationCategory = "trade" | "message" | "system" | "alert";

export interface Notification {
  id: string;
  title: string;
  description: string;
  time: string;
  read: boolean;
  category: NotificationCategory;
  href?: string;
}

interface NotificationsContextValue {
  notifications: Notification[];
  unreadCount: number;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  markNotificationsAsRead: (ids: string[]) => void;
  addNotification: (notification: Omit<Notification, "id" | "read"> & { id?: string; read?: boolean }) => void;
}

const createNotificationId = () => Math.random().toString(36).slice(2, 10);

const initialNotifications: Notification[] = [
  {
    id: "1",
    title: "Nueva propuesta de intercambio",
    description: "Carlos propone intercambiar su bicicleta por tu guitarra Fender.",
    time: "Hace 5 minutos",
    read: false,
    category: "trade"
  },
  {
    id: "2",
    title: "Oferta destacada",
    description: "Laura aumentó su oferta por tu cámara Canon EOS 80D.",
    time: "Hace 25 minutos",
    read: false,
    category: "alert"
  },
  {
    id: "3",
    title: "Nuevo mensaje",
    description: "Ana: \"Hola, ¿sigues interesado en intercambiar la tablet?\"",
    time: "Hace 1 hora",
    read: false,
    category: "message"
  },
  {
    id: "4",
    title: "Intercambio completado",
    description: "Confirmaste el intercambio con Miguel por la colección de libros.",
    time: "Hace 3 horas",
    read: true,
    category: "system"
  },
  {
    id: "5",
    title: "Recomendación personalizada",
    description: "Descubrimos nuevos trueques en tu categoría favorita de Electrónicos.",
    time: "Ayer",
    read: true,
    category: "system"
  }
];

const NotificationsContext = createContext<NotificationsContextValue | undefined>(undefined);

export const NotificationsProvider = ({ children }: { children: ReactNode }) => {
  const [notifications, setNotifications] = useState<Notification[]>(initialNotifications);

  const markNotificationsAsRead = useCallback((ids: string[]) => {
    if (ids.length === 0) return;

    setNotifications((previous) =>
      previous.map((notification) =>
        ids.includes(notification.id) ? { ...notification, read: true } : notification
      )
    );
  }, []);

  const markAsRead = useCallback(
    (id: string) => {
      markNotificationsAsRead([id]);
    },
    [markNotificationsAsRead]
  );

  const markAllAsRead = useCallback(() => {
    setNotifications((previous) => previous.map((notification) => ({ ...notification, read: true })));
  }, []);

  const addNotification = useCallback(
    (notification: Omit<Notification, "id" | "read"> & { id?: string; read?: boolean }) => {
      setNotifications((previous) => [
        {
          ...notification,
          id: notification.id ?? createNotificationId(),
          read: notification.read ?? false
        },
        ...previous
      ]);
    },
    []
  );

  const unreadCount = useMemo(
    () => notifications.reduce((count, notification) => (notification.read ? count : count + 1), 0),
    [notifications]
  );

  const value = useMemo(
    () => ({
      notifications,
      unreadCount,
      markAsRead,
      markAllAsRead,
      markNotificationsAsRead,
      addNotification
    }),
    [notifications, unreadCount, markAsRead, markAllAsRead, markNotificationsAsRead, addNotification]
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
