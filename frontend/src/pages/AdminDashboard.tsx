import Footer from "@/components/Footer";
import Header from "@/components/Header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useToast } from "@/components/ui/use-toast";
import { apiClient } from "@/lib/api";
import { useQuery } from "@tanstack/react-query";
import { formatDistanceToNow } from "date-fns";
import { es } from "date-fns/locale";
import { AlertCircle, BarChart3, LayoutDashboard, Loader2, Shield, UserCog } from "lucide-react";
import { useEffect, useMemo } from "react";

interface AdminUser {
  id: string;
  email: string;
  role: "ADMIN" | "USER";
  displayName: string;
  createdAt: string;
}

interface AdminItem {
  id: string;
  ownerId: string;
  categoryId: string;
  title: string;
  description: string;
  condition: string;
  estimatedValue: number | null;
  available: boolean | null;
  service: boolean | null;
  location: string | null;
  images: string[];
  createdAt: string;
  updatedAt: string;
  wishlist: string[];
}

const formatRelativeDate = (value: string) =>
  formatDistanceToNow(new Date(value), { addSuffix: true, locale: es });

const AdminDashboard = () => {
  const { toast } = useToast();

  const {
    data: users = [],
    isLoading: isLoadingUsers,
    isError: isUsersError,
    error: usersError,
    refetch: refetchUsers,
  } = useQuery<AdminUser[]>({
    queryKey: ["admin", "users"],
    queryFn: () => apiClient.get("/api/admin/users"),
  });

  const {
    data: items = [],
    isLoading: isLoadingItems,
    isError: isItemsError,
    error: itemsError,
    refetch: refetchItems,
  } = useQuery<AdminItem[]>({
    queryKey: ["admin", "items"],
    queryFn: () => apiClient.get("/api/admin/items"),
  });

  const metrics = useMemo(() => {
    const totalUsers = users.length;
    const adminCount = users.filter((user) => user.role === "ADMIN").length;
    const totalItems = items.length;
    const activeItems = items.filter((item) => item.available !== false).length;

    return [
      {
        label: "Usuarios registrados",
        value: totalUsers,
        helper: `${adminCount} administradores activos`,
        icon: UserCog,
      },
      {
        label: "Publicaciones totales",
        value: totalItems,
        helper: `${activeItems} disponibles para trueque`,
        icon: LayoutDashboard,
      },
      {
        label: "Servicios vs productos",
        value: `${items.filter((item) => item.service).length} / ${items.filter((item) => !item.service).length}`,
        helper: "Servicios / Productos",
        icon: BarChart3,
      },
      {
        label: "Interés acumulado",
        value: items.reduce((total, item) => total + (item.wishlist?.length ?? 0), 0),
        helper: "Deseos totales en publicaciones",
        icon: Shield,
      },
    ];
  }, [items, users]);

  useEffect(() => {
    if (!isUsersError && !isItemsError) {
      return;
    }

    const description = (usersError || itemsError)?.message ?? "No pudimos cargar la consola de administración.";
    toast({
      title: "Error al cargar datos de administración",
      description,
      variant: "destructive",
    });
  }, [isItemsError, isUsersError, itemsError, toast, usersError]);

  const isLoading = isLoadingItems || isLoadingUsers;

  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1 bg-muted/30 py-10">
        <div className="container mx-auto px-4 space-y-8">
          <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div className="space-y-1">
              <p className="text-sm font-medium text-primary">Panel de administración</p>
              <h1 className="text-3xl font-bold text-foreground">Dashboard</h1>
              <p className="text-muted-foreground max-w-2xl">
                Supervisa la actividad global de TruequePlus, revisa usuarios y controla el inventario de
                publicaciones en tiempo real.
              </p>
            </div>
            {(isLoadingUsers || isLoadingItems) && (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 animate-spin" />
                Sincronizando datos...
              </div>
            )}
          </div>

          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {metrics.map((metric) => {
              const Icon = metric.icon;
              return (
                <Card key={metric.label} className="shadow-sm">
                  <CardHeader className="flex flex-row items-center justify-between pb-2">
                    <CardTitle className="text-sm font-medium text-muted-foreground">
                      {metric.label}
                    </CardTitle>
                    <Icon className="h-5 w-5 text-primary" />
                  </CardHeader>
                  <CardContent className="space-y-1">
                    <p className="text-3xl font-bold">{metric.value}</p>
                    <p className="text-xs text-muted-foreground">{metric.helper}</p>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          <div className="grid gap-6 lg:grid-cols-5">
            <Card className="shadow-sm lg:col-span-2">
              <CardHeader className="space-y-1">
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Usuarios</CardTitle>
                    <CardDescription>Gestiona las cuentas registradas y sus roles.</CardDescription>
                  </div>
                  <Button variant="outline" size="sm" onClick={() => refetchUsers()} disabled={isLoadingUsers}>
                    {isLoadingUsers ? <Loader2 className="h-4 w-4 animate-spin" /> : "Actualizar"}
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {(isLoadingUsers && users.length === 0) || isLoading ? (
                  <div className="flex flex-col items-center justify-center gap-3 py-10 text-muted-foreground">
                    <Loader2 className="h-6 w-6 animate-spin" />
                    <p>Cargando usuarios...</p>
                  </div>
                ) : null}

                {isUsersError ? (
                  <div className="flex items-center gap-3 rounded-lg border border-destructive/40 bg-destructive/10 p-3 text-destructive">
                    <AlertCircle className="h-5 w-5" />
                    <div className="text-sm flex-1">
                      <p className="font-medium">No se pudieron cargar los usuarios</p>
                      <p className="text-destructive/80">{usersError?.message ?? "Inténtalo nuevamente."}</p>
                    </div>
                    <Button variant="outline" size="sm" onClick={() => refetchUsers()}>
                      Reintentar
                    </Button>
                  </div>
                ) : null}

                {!isLoadingUsers && !isUsersError && (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Nombre</TableHead>
                        <TableHead>Correo</TableHead>
                        <TableHead>Rol</TableHead>
                        <TableHead>Alta</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {users.map((user) => (
                        <TableRow key={user.id}>
                          <TableCell>
                            <div className="flex flex-col">
                              <span className="font-medium text-foreground">{user.displayName}</span>
                              <span className="text-xs text-muted-foreground">{user.id.slice(0, 8)}</span>
                            </div>
                          </TableCell>
                          <TableCell className="text-muted-foreground">{user.email}</TableCell>
                          <TableCell>
                            <Badge variant={user.role === "ADMIN" ? "default" : "secondary"}>{user.role}</Badge>
                          </TableCell>
                          <TableCell className="text-muted-foreground">{formatRelativeDate(user.createdAt)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>

            <Card className="shadow-sm lg:col-span-3">
              <CardHeader className="space-y-1">
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Publicaciones</CardTitle>
                    <CardDescription>Monitorea el estado y disponibilidad de las publicaciones.</CardDescription>
                  </div>
                  <Button variant="outline" size="sm" onClick={() => refetchItems()} disabled={isLoadingItems}>
                    {isLoadingItems ? <Loader2 className="h-4 w-4 animate-spin" /> : "Actualizar"}
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {(isLoadingItems && items.length === 0) || isLoading ? (
                  <div className="flex flex-col items-center justify-center gap-3 py-10 text-muted-foreground">
                    <Loader2 className="h-6 w-6 animate-spin" />
                    <p>Cargando publicaciones...</p>
                  </div>
                ) : null}

                {isItemsError ? (
                  <div className="flex items-center gap-3 rounded-lg border border-destructive/40 bg-destructive/10 p-3 text-destructive">
                    <AlertCircle className="h-5 w-5" />
                    <div className="text-sm flex-1">
                      <p className="font-medium">No se pudieron cargar las publicaciones</p>
                      <p className="text-destructive/80">{itemsError?.message ?? "Inténtalo nuevamente."}</p>
                    </div>
                    <Button variant="outline" size="sm" onClick={() => refetchItems()}>
                      Reintentar
                    </Button>
                  </div>
                ) : null}

                {!isLoadingItems && !isItemsError && (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Título</TableHead>
                        <TableHead>Tipo</TableHead>
                        <TableHead>Estado</TableHead>
                        <TableHead>Ubicación</TableHead>
                        <TableHead>Creación</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {items.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell className="max-w-[240px]">
                            <div className="flex flex-col">
                              <span className="font-medium text-foreground line-clamp-1">{item.title}</span>
                              <span className="text-xs text-muted-foreground line-clamp-2">{item.description}</span>
                            </div>
                          </TableCell>
                          <TableCell>
                            <Badge variant={item.service ? "secondary" : "outline"}>
                              {item.service ? "Servicio" : "Producto"}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            <Badge variant={item.available === false ? "destructive" : "default"}>
                              {item.available === false ? "No disponible" : "Activa"}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-muted-foreground">
                            {item.location ?? "Sin ubicación"}
                          </TableCell>
                          <TableCell className="text-muted-foreground">{formatRelativeDate(item.createdAt)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
          </div>

          <Separator />

          <div className="text-xs text-muted-foreground">
            Los datos se actualizan automáticamente al cambiar de sesión. Utiliza los botones de actualizar para
            forzar una sincronización manual.
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default AdminDashboard;
