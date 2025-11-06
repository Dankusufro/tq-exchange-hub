import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useToast } from "@/components/ui/use-toast";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  useNotifications,
  type Notification,
  type NotificationCategory,
} from "@/hooks/use-notifications";
import { useFavorites } from "@/hooks/use-favorites";
import { useAuth } from "@/providers/AuthProvider";
import { cn } from "@/lib/utils";
import SearchBar from "@/components/SearchBar";
import { Link, useNavigate } from "react-router-dom";
import { formatDistanceToNow } from "date-fns";
import { es } from "date-fns/locale";
import {
  AlertTriangle,
  ArrowLeftRight,
  Bell,
  Heart,
  Loader2,
  Info,
  Menu,
  MessageCircle,
  Plus,
  User,
  Star,
  TrendingUp,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

const notificationTypeIcons: Record<NotificationCategory, LucideIcon> = {
  trade: ArrowLeftRight,
  message: MessageCircle,
  system: Info,
  alert: AlertTriangle,
};

const notificationTypeColors: Record<NotificationCategory, string> = {
  trade: "text-emerald-500",
  message: "text-blue-500",
  system: "text-muted-foreground",
  alert: "text-amber-500",
};

const formatNotificationTime = (value: string | null | undefined) => {
  if (!value) {
    return "Hace un momento";
  }

  try {
    return formatDistanceToNow(new Date(value), { addSuffix: true, locale: es });
  } catch (error) {
    console.error("Failed to format notification timestamp", error);
    return "Hace un momento";
  }
};

interface NotificationListProps {
  title: string;
  emptyMessage: string;
  notifications: Notification[];
  unreadCount: number;
  onMarkAllAsRead: () => Promise<void>;
  onNotificationClick: (id: string) => Promise<void>;
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
        <Button
          variant="ghost"
          size="sm"
          className="h-7 px-2 text-xs"
          onClick={() => {
            void onMarkAllAsRead();
          }}
        >
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
                onClick={() => {
                  void onNotificationClick(notification.id);
                }}
              >
                <div className="mt-1">
                  <Icon className={cn("h-5 w-5", notificationTypeColors[notification.category])} />
                </div>

                <div className="space-y-1">
                  <p className="text-sm font-medium text-foreground">{notification.title}</p>
                  <p className="text-xs leading-snug text-muted-foreground">{notification.description}</p>
                  <p className="text-xs text-muted-foreground">{formatNotificationTime(notification.createdAt)}</p>
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

const getInitials = (name: string | null | undefined) => {
  if (!name) {
    return "?";
  }

  const [first = "", second = ""] = name.split(" ");
  return `${first.charAt(0)}${second.charAt(0)}`.toUpperCase();
};

const Header = () => {
  const { notifications, markAsRead, markNotificationsAsRead } = useNotifications();
  const { user, signOut } = useAuth();
  const { toast } = useToast();
  const {
    favorites,
    isLoading: favoritesLoading,
    error: favoritesError,
  } = useFavorites();
  const navigate = useNavigate();

  const messageNotifications = notifications.filter((notification) => notification.category === "message");
  const generalNotifications = notifications.filter((notification) => notification.category !== "message");

  const messageUnreadCount = messageNotifications.filter((notification) => !notification.read).length;
  const generalUnreadCount = generalNotifications.filter((notification) => !notification.read).length;
  const ratingLabel =
    user?.rating !== null && user?.rating !== undefined
      ? user.rating.toFixed(1).replace(/\.0$/, "")
      : null;

  const handleSignOut = async () => {
    try {
      await signOut();
      toast({
        title: "Sesión cerrada",
        description: "Has cerrado sesión correctamente.",
      });
    } catch (error) {
      const description =
        error instanceof Error
          ? error.message
          : "No pudimos cerrar tu sesión. Inténtalo nuevamente más tarde.";
      toast({
        title: "Error al cerrar sesión",
        description,
        variant: "destructive",
      });
    }
  };

  const renderFavoritesBadge = () => {
    if (favoritesLoading) {
      return (
        <Badge className="absolute -top-1 -right-1 h-5 w-5 p-0 flex items-center justify-center text-xs">
          <Loader2 className="h-3 w-3 animate-spin" />
        </Badge>
      );
    }

    if (favoritesError) {
      return (
        <Badge className="absolute -top-1 -right-1 h-5 w-5 p-0 flex items-center justify-center text-xs bg-destructive text-destructive-foreground">
          !
        </Badge>
      );
    }

    if (favorites.length > 0) {
      return (
        <Badge className="absolute -top-1 -right-1 h-5 w-5 p-0 flex items-center justify-center text-xs">
          {favorites.length}
        </Badge>
      );
    }

    return null;
  };

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
          <SearchBar
            className="hidden md:flex flex-1 max-w-md mx-8"
            placeholder="Buscar productos o servicios..."
            syncWithUrl
          />

          {/* Navigation */}
          <div className="flex items-center gap-2">
            <Button variant="outline" className="hidden sm:flex items-center gap-2" asChild>
              <Link to="/listings/new">
                <Plus className="h-4 w-4" />
                Publicar
              </Link>
            </Button>
            <Button variant="outline" size="icon" className="sm:hidden" asChild>
              <Link to="/listings/new" aria-label="Crear una nueva publicación">
                <Plus className="h-4 w-4" />
              </Link>
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

            <Button
              variant="ghost"
              size="sm"
              className="relative"
              asChild
            >
              <Link to="/favorites" aria-label="Abrir favoritos">
                <Heart className="h-5 w-5" />
                {renderFavoritesBadge()}
              </Link>
            </Button>
            

            {user ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="sm" className="flex items-center gap-2" aria-label="Menú de usuario">
                    <Avatar className="h-8 w-8">
                      <AvatarImage src={user.avatarUrl ?? undefined} alt={user.displayName ?? "Usuario"} />
                      <AvatarFallback>{getInitials(user.displayName)}</AvatarFallback>
                    </Avatar>
                    <span className="hidden sm:flex flex-col text-left leading-tight">
                      <span className="text-sm font-medium text-foreground">{user.displayName}</span>
                      <span className="text-xs text-muted-foreground">Mi cuenta</span>
                    </span>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-48">
                  <DropdownMenuLabel className="space-y-1">
                    <p className="text-sm font-semibold text-foreground">{user.displayName}</p>
                    <p className="text-xs text-muted-foreground">Gestiona tu perfil y tu sesión</p>
                    {(user.totalTrades !== null || ratingLabel) && (
                      <p className="text-xs text-muted-foreground flex items-center gap-2">
                        {user.totalTrades !== null && (
                          <span className="flex items-center gap-1">
                            <TrendingUp className="h-3 w-3" />
                            {user.totalTrades} trueques
                          </span>
                        )}
                        {user.totalTrades !== null && ratingLabel && <span className="opacity-60">•</span>}
                        {ratingLabel && (
                          <span className="flex items-center gap-1">
                            <Star className="h-3 w-3 text-amber-500" />
                            {ratingLabel}/5
                          </span>
                        )}
                      </p>
                    )}
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onSelect={(event) => {
                      event.preventDefault();
                      navigate("/profile");
                    }}
                  >
                    Mi perfil
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onSelect={(event) => {
                      event.preventDefault();
                      void handleSignOut();
                    }}
                    className="text-destructive focus:text-destructive"
                  >
                    Cerrar sesión
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <Button variant="ghost" size="sm" asChild>
                <Link to="/auth" aria-label="Iniciar sesión">
                  <User className="h-5 w-5" />
                </Link>
              </Button>
            )}
            

            <Button variant="ghost" size="sm" className="md:hidden">
              <Menu className="h-5 w-5" />
            </Button>
          </div>
        </div>

        {/* Mobile search */}
        <div className="pb-4 md:hidden">
          <SearchBar placeholder="Buscar productos o servicios..." syncWithUrl />
        </div>
      </div>
    </header>
  );
};

export default Header;
