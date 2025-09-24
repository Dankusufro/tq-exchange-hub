import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ScrollArea } from "@/components/ui/scroll-area";
import { useNotifications, type Notification, type NotificationCategory } from "@/hooks/use-notifications";
import { cn } from "@/lib/utils";
import { Link } from "react-router-dom";
import {
  AlertTriangle,
  ArrowLeftRight,
  Bell,
  Heart,
  Info,
  Menu,
  MessageCircle,
  Plus,
  Search,
  User
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

const notificationTypeIcons: Record<NotificationCategory, LucideIcon> = {
  trade: ArrowLeftRight,
  message: MessageCircle,
  system: Info,
  alert: AlertTriangle
};

const notificationTypeColors: Record<NotificationCategory, string> = {
  trade: "text-emerald-500",
  message: "text-blue-500",
  system: "text-muted-foreground",
  alert: "text-amber-500"
};

interface NotificationListProps {
  title: string;
  emptyMessage: string;
  notifications: Notification[];
  unreadCount: number;
  onMarkAllAsRead: () => void;
  onNotificationClick: (id: string) => void;
  viewAllLabel?: string;
}

const NotificationList = ({
  title,
  emptyMessage,
  notifications,
  unreadCount,
  onMarkAllAsRead,
  onNotificationClick,
  viewAllLabel = "Ver todas las notificaciones"
}: NotificationListProps) => (
  <PopoverContent align="end" className="w-80 p-0 shadow-lg">
    <div className="flex items-center justify-between border-b px-4 py-3">
      <div>
        <p className="text-sm font-semibold text-foreground">{title}</p>
        <p className="text-xs text-muted-foreground">
          {unreadCount > 0 ? `${unreadCount} sin leer` : "Todo al día"}
        </p>
      </div>
      {unreadCount > 0 && (
        <Button variant="ghost" size="sm" className="h-7 px-2 text-xs" onClick={onMarkAllAsRead}>
          Marcar todo
        </Button>
      )}
    </div>

    {notifications.length > 0 ? (
      <ScrollArea className="max-h-80">
        <div className="divide-y">
          {notifications.map((notification) => {
            const Icon = notificationTypeIcons[notification.category];

            return (
              <button
                key={notification.id}
                type="button"
                className={cn(
                  "flex w-full items-start gap-3 px-4 py-3 text-left transition-colors hover:bg-muted/60 focus:outline-none focus:bg-muted/60",
                  !notification.read && "bg-muted/40"
                )}
                onClick={() => onNotificationClick(notification.id)}
              >
                <div className="mt-1">
                  <Icon className={cn("h-5 w-5", notificationTypeColors[notification.category])} />
                </div>

                <div className="space-y-1">
                  <p className="text-sm font-medium text-foreground">{notification.title}</p>
                  <p className="text-xs leading-snug text-muted-foreground">{notification.description}</p>
                  <p className="text-xs text-muted-foreground">{notification.time}</p>
                </div>

                {!notification.read && <span className="ml-auto mt-1 h-2 w-2 rounded-full bg-primary" />}
              </button>
            );
          })}
        </div>
      </ScrollArea>
    ) : (
      <div className="px-4 py-6 text-center text-sm text-muted-foreground">{emptyMessage}</div>
    )}

    <div className="border-t px-4 py-2">
      <Button variant="ghost" size="sm" className="w-full">
        {viewAllLabel}
      </Button>
    </div>
  </PopoverContent>
);

const Header = () => {
  const { notifications, markAsRead, markNotificationsAsRead } = useNotifications();

  const messageNotifications = notifications.filter((notification) => notification.category === "message");
  const generalNotifications = notifications.filter((notification) => notification.category !== "message");

  const messageUnreadCount = messageNotifications.filter((notification) => !notification.read).length;
  const generalUnreadCount = generalNotifications.filter((notification) => !notification.read).length;

  return (
    <header className="bg-card border-b sticky top-0 z-50 backdrop-blur-sm bg-card/95">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <div className="bg-gradient-primary rounded-lg p-2">
              <div className="text-white font-bold text-xl">TQ</div>
            </div>
            <div className="hidden sm:block">
              <div className="font-bold text-lg text-foreground">TruequePlus</div>
              <div className="text-xs text-muted-foreground -mt-1">Intercambia sin límites</div>
            </div>
          </div>

          {/* Search bar - Desktop */}
          <div className="hidden md:flex flex-1 max-w-md mx-8">
            <div className="relative w-full">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Buscar productos o servicios..."
                className="pl-10 h-10"
              />
            </div>
          </div>

          {/* Navigation */}
          <div className="flex items-center gap-2">
            <Button variant="outline" className="hidden sm:flex items-center gap-2">
              <Plus className="h-4 w-4" />
              Publicar
            </Button>
            
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="ghost" size="sm" className="relative" aria-label="Abrir mensajes">
                  <MessageCircle className="h-5 w-5" />
                  {messageUnreadCount > 0 && (
                    <Badge className="absolute -top-1 -right-1 h-5 w-5 p-0 flex items-center justify-center text-xs">
                      {messageUnreadCount}
                    </Badge>
                  )}
                </Button>
              </PopoverTrigger>
              <NotificationList
                title="Mensajes"
                emptyMessage="No tienes nuevos mensajes."
                notifications={messageNotifications}
                unreadCount={messageUnreadCount}
                onMarkAllAsRead={() =>
                  markNotificationsAsRead(messageNotifications.map((notification) => notification.id))
                }
                onNotificationClick={markAsRead}
                viewAllLabel="Ir al buzón de mensajes"
              />
            </Popover>

            <Popover>
              <PopoverTrigger asChild>
                <Button variant="ghost" size="sm" className="relative" aria-label="Abrir notificaciones">
                  <Bell className="h-5 w-5" />
                  {generalUnreadCount > 0 && (
                    <Badge className="absolute -top-1 -right-1 h-5 w-5 p-0 flex items-center justify-center text-xs">
                      {generalUnreadCount}
                    </Badge>
                  )}
                </Button>
              </PopoverTrigger>
              <NotificationList
                title="Notificaciones"
                emptyMessage="No tienes notificaciones pendientes."
                notifications={generalNotifications}
                unreadCount={generalUnreadCount}
                onMarkAllAsRead={() =>
                  markNotificationsAsRead(generalNotifications.map((notification) => notification.id))
                }
                onNotificationClick={markAsRead}
              />
            </Popover>

            <Button variant="ghost" size="sm">
              <Heart className="h-5 w-5" />
            </Button>
            

            <Button variant="ghost" size="sm" asChild>
              <Link to="/auth" aria-label="Iniciar sesión">
                <User className="h-5 w-5" />
              </Link>
            </Button>
            

            <Button variant="ghost" size="sm" className="md:hidden">
              <Menu className="h-5 w-5" />
            </Button>
          </div>
        </div>

        {/* Mobile search */}
        <div className="pb-4 md:hidden">
          <div className="relative">
            <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Buscar productos o servicios..."
              className="pl-10 h-10"
            />
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
