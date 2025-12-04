import Footer from "@/components/Footer";
import Header from "@/components/Header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ChartContainer, ChartLegend, ChartLegendContent, ChartTooltip, ChartTooltipContent } from "@/components/ui/chart";
import { Separator } from "@/components/ui/separator";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useToast } from "@/components/ui/use-toast";
import { apiClient } from "@/lib/api";
import { useQuery } from "@tanstack/react-query";
import { formatDistanceToNow } from "date-fns";
import { es } from "date-fns/locale";
import { AlertCircle, BarChart3, LayoutDashboard, Loader2, Shield, UserCog } from "lucide-react";
import { Bar, BarChart, CartesianGrid, Line, LineChart, XAxis, YAxis } from "recharts";
import { useCallback, useEffect, useMemo } from "react";

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
  moderationStatus?: "PENDING" | "APPROVED" | "DISABLED";
  images: string[];
  createdAt: string;
  updatedAt: string;
  wishlist: string[];
}

interface TimeSeriesPoint {
  label: string;
  value: number;
}

interface AdminMetrics {
  totalUsers: number;
  activeUsers: number;
  completedTrades: number;
  activeTrades: number;
  tradeActivity: TimeSeriesPoint[];
  signupsActivity: TimeSeriesPoint[];
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

  const {
    data: metrics,
    isLoading: isLoadingMetrics,
    isError: isMetricsError,
    error: metricsError,
    refetch: refetchMetrics,
  } = useQuery<AdminMetrics>({
    queryKey: ["admin", "metrics"],
    queryFn: () => apiClient.get("/api/admin/metrics"),
  });

  const summaryCards = useMemo(() => {
    const totalItems = items.length;
    const activeItems = items.filter(
      (item) => item.available !== false && item.moderationStatus !== "DISABLED",
    ).length;

    return [
      {
        label: "Usuarios conectados",
        value: metrics?.activeUsers ?? "–",
        helper: `${metrics?.totalUsers ?? 0} cuentas registradas`,
        icon: UserCog,
      },
      {
        label: "Trueques completados",
        value: metrics?.completedTrades ?? "–",
        helper: `${metrics?.activeTrades ?? 0} en progreso`,
        icon: LayoutDashboard,
      },
      {
        label: "Publicaciones totales",
        value: totalItems,
        helper: `${activeItems} disponibles para trueque`,
        icon: BarChart3,
      },
      {
        label: "Interés acumulado",
        value: items.reduce((total, item) => total + (item.wishlist?.length ?? 0), 0),
        helper: "Deseos totales en publicaciones",
        icon: Shield,
      },
    ];
  }, [items, metrics]);

  const activitySeries = useMemo(() => {
    if (!metrics) return [];

    const signupsByLabel = new Map(metrics.signupsActivity.map((point) => [point.label, point.value]));

    return metrics.tradeActivity.map((point) => ({
      hour: point.label,
      trades: point.value,
      signups: signupsByLabel.get(point.label) ?? 0,
    }));
  }, [metrics]);

  const tradeStatusSeries = useMemo(
    () => [
      {
        name: "Trueques",
        completados: metrics?.completedTrades ?? 0,
        activos: metrics?.activeTrades ?? 0,
      },
    ],
    [metrics],
  );

  const activityChartConfig = useMemo(
    () => ({
      trades: { label: "Trueques", color: "hsl(var(--chart-1))" },
      signups: { label: "Altas", color: "hsl(var(--chart-2))" },
    }),
    [],
  );

  const tradeStatusConfig = useMemo(
    () => ({
      completados: { label: "Completados", color: "hsl(var(--chart-1))" },
      activos: { label: "Activos", color: "hsl(var(--chart-3))" },
    }),
    [],
  );

  const handleDisableItem = useCallback(
    async (item: AdminItem) => {
      const confirmed = window.confirm(`¿Deshabilitar la publicación "${item.title}"?`);
      if (!confirmed) return;

      try {
        await apiClient.patch(`/api/admin/items/${item.id}/disable`);
        toast({ title: "Publicación deshabilitada", description: "El ítem ya no estará disponible." });
        refetchItems();
      } catch (error: any) {
        toast({
          title: "No se pudo deshabilitar",
          description: error?.message ?? "Intenta nuevamente más tarde.",
          variant: "destructive",
        });
      }
    },
    [refetchItems, toast],
  );

  const handleDeleteItem = useCallback(
    async (item: AdminItem) => {
      const confirmed = window.confirm(
        `¿Eliminar definitivamente la publicación "${item.title}"? Esta acción no se puede deshacer.`,
      );
      if (!confirmed) return;

      try {
        await apiClient.delete(`/api/admin/items/${item.id}`);
        toast({ title: "Publicación eliminada", description: "El ítem fue eliminado del catálogo." });
        refetchItems();
      } catch (error: any) {
        toast({
          title: "No se pudo eliminar",
          description: error?.message ?? "Revisa la conexión e inténtalo otra vez.",
          variant: "destructive",
        });
      }
    },
    [refetchItems, toast],
  );

  useEffect(() => {
    if (!isUsersError && !isItemsError && !isMetricsError) {
      return;
    }

    const description =
      (usersError || itemsError || metricsError)?.message ?? "No pudimos cargar la consola de administración.";
    toast({
      title: "Error al cargar datos de administración",
      description,
      variant: "destructive",
    });
  }, [isItemsError, isMetricsError, isUsersError, itemsError, metricsError, toast, usersError]);

  const isAnyLoading = isLoadingItems || isLoadingUsers || isLoadingMetrics;
  const isTableLoading = isLoadingItems || isLoadingUsers;

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
            {isAnyLoading && (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 animate-spin" />
                Sincronizando datos...
              </div>
            )}
          </div>

          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {summaryCards.map((metric) => {
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

          <div className="grid gap-6 lg:grid-cols-7">
            <Card className="shadow-sm lg:col-span-4">
              <CardHeader className="space-y-1">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <CardTitle>Actividad en las últimas horas</CardTitle>
                    <CardDescription>Trueques iniciados y nuevos registros por hora.</CardDescription>
                  </div>
                  <Button variant="outline" size="sm" onClick={() => refetchMetrics()} disabled={isLoadingMetrics}>
                    {isLoadingMetrics ? <Loader2 className="h-4 w-4 animate-spin" /> : "Actualizar"}
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {isLoadingMetrics && !metrics ? (
                  <div className="flex flex-col items-center justify-center gap-3 py-10 text-muted-foreground">
                    <Loader2 className="h-6 w-6 animate-spin" />
                    <p>Sincronizando métricas...</p>
                  </div>
                ) : null}

                {isMetricsError ? (
                  <div className="flex items-center gap-3 rounded-lg border border-destructive/40 bg-destructive/10 p-3 text-destructive">
                    <AlertCircle className="h-5 w-5" />
                    <div className="text-sm flex-1">
                      <p className="font-medium">No se pudieron cargar las métricas</p>
                      <p className="text-destructive/80">{metricsError?.message ?? "Inténtalo nuevamente."}</p>
                    </div>
                    <Button variant="outline" size="sm" onClick={() => refetchMetrics()}>
                      Reintentar
                    </Button>
                  </div>
                ) : null}

                {!isLoadingMetrics && !isMetricsError && metrics ? (
                  <ChartContainer config={activityChartConfig} className="h-[320px]">
                    <LineChart data={activitySeries} margin={{ left: 0, right: 12 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis
                        dataKey="hour"
                        tickLine={false}
                        axisLine={false}
                        tickMargin={8}
                        minTickGap={16}
                        fontSize={12}
                      />
                      <YAxis allowDecimals={false} tickLine={false} axisLine={false} tickMargin={12} fontSize={12} />
                      <ChartTooltip
                        cursor={{ strokeDasharray: "3 3" }}
                        content={<ChartTooltipContent indicator="line" className="bg-background" />}
                      />
                      <Line type="monotone" dataKey="trades" stroke="var(--color-trades)" strokeWidth={3} dot={false} />
                      <Line
                        type="monotone"
                        dataKey="signups"
                        stroke="var(--color-signups)"
                        strokeWidth={2}
                        dot={false}
                        strokeDasharray="4 4"
                      />
                      <ChartLegend
                        verticalAlign="bottom"
                        content={<ChartLegendContent className="-mb-2 mt-2 text-muted-foreground" />}
                      />
                    </LineChart>
                  </ChartContainer>
                ) : null}
              </CardContent>
            </Card>

            <Card className="shadow-sm lg:col-span-3">
              <CardHeader className="space-y-1">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <CardTitle>Estado de trueques</CardTitle>
                    <CardDescription>Panorama de completados y transacciones activas.</CardDescription>
                  </div>
                  <Button variant="outline" size="sm" onClick={() => refetchMetrics()} disabled={isLoadingMetrics}>
                    {isLoadingMetrics ? <Loader2 className="h-4 w-4 animate-spin" /> : "Refrescar"}
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {isLoadingMetrics && !metrics ? (
                  <div className="flex flex-col items-center justify-center gap-3 py-8 text-muted-foreground">
                    <Loader2 className="h-6 w-6 animate-spin" />
                    <p>Obteniendo estado global...</p>
                  </div>
                ) : null}

                {isMetricsError ? (
                  <div className="flex items-center gap-3 rounded-lg border border-destructive/40 bg-destructive/10 p-3 text-destructive">
                    <AlertCircle className="h-5 w-5" />
                    <div className="text-sm flex-1">
                      <p className="font-medium">No se pudo cargar el estado de trueques</p>
                      <p className="text-destructive/80">{metricsError?.message ?? "Inténtalo nuevamente."}</p>
                    </div>
                    <Button variant="outline" size="sm" onClick={() => refetchMetrics()}>
                      Reintentar
                    </Button>
                  </div>
                ) : null}

                {!isLoadingMetrics && !isMetricsError && metrics ? (
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4 text-sm">
                      <div className="rounded-lg border bg-muted/40 p-3">
                        <p className="text-muted-foreground">Trueques completados</p>
                        <p className="text-2xl font-bold text-foreground">{metrics.completedTrades}</p>
                      </div>
                      <div className="rounded-lg border bg-muted/40 p-3">
                        <p className="text-muted-foreground">Trueques activos</p>
                        <p className="text-2xl font-bold text-foreground">{metrics.activeTrades}</p>
                      </div>
                    </div>
                    <ChartContainer config={tradeStatusConfig} className="h-[260px]">
                      <BarChart data={tradeStatusSeries} barSize={36} margin={{ left: 8, right: 8 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} />
                        <XAxis dataKey="name" tickLine={false} axisLine={false} tickMargin={8} />
                        <YAxis allowDecimals={false} tickLine={false} axisLine={false} tickMargin={8} width={32} />
                        <ChartTooltip content={<ChartTooltipContent />} />
                        <Bar dataKey="completados" fill="var(--color-completados)" radius={6} />
                        <Bar dataKey="activos" fill="var(--color-activos)" radius={6} />
                        <ChartLegend
                          verticalAlign="bottom"
                          content={<ChartLegendContent className="-mb-2 mt-2 text-muted-foreground" />}
                        />
                      </BarChart>
                    </ChartContainer>
                  </div>
                ) : null}
              </CardContent>
            </Card>
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
                {(isLoadingUsers && users.length === 0) || isTableLoading ? (
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
                {(isLoadingItems && items.length === 0) || isTableLoading ? (
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
                        <TableHead>Moderación</TableHead>
                        <TableHead>Ubicación</TableHead>
                        <TableHead>Creación</TableHead>
                        <TableHead className="text-right">Acciones</TableHead>
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
                          <TableCell>
                            <Badge
                              variant={
                                item.moderationStatus === "DISABLED"
                                  ? "destructive"
                                  : item.moderationStatus === "PENDING"
                                    ? "secondary"
                                    : "default"
                              }
                            >
                              {item.moderationStatus ?? "DESCONOCIDO"}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-muted-foreground">
                            {item.location ?? "Sin ubicación"}
                          </TableCell>
                          <TableCell className="text-muted-foreground">{formatRelativeDate(item.createdAt)}</TableCell>
                          <TableCell className="text-right space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              disabled={item.moderationStatus === "DISABLED"}
                              onClick={() => handleDisableItem(item)}
                            >
                              Deshabilitar
                            </Button>
                            <Button variant="destructive" size="sm" onClick={() => handleDeleteItem(item)}>
                              Eliminar
                            </Button>
                          </TableCell>
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
