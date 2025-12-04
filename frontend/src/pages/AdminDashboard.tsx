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
import {
  AlertCircle,
  AlertTriangle,
  BarChart3,
  CheckCircle2,
  Clock3,
  Leaf,
  LayoutDashboard,
  Loader2,
  MapPin,
  RefreshCcw,
  TrendingUp,
  UserCog,
  XCircle,
} from "lucide-react";
import { Bar, BarChart, CartesianGrid, Line, LineChart, XAxis, YAxis } from "recharts";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

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
  cancelledTrades: number;
  activeTrades: number;
  tradeActivity: TimeSeriesPoint[];
  signupsActivity: TimeSeriesPoint[];
  ecologicalImpactKg: number;
  successRate: number;
  cancellationRate: number;
  locationMetrics: RegionMetrics[];
}

interface RegionMetrics {
  region: string;
  commune: string;
  completedTrades: number;
  cancelledTrades: number;
  totalTrades: number;
}

type AbuseReportStatus = "PENDING" | "RESOLVED" | "SANCTIONED";

type AbuseReportAction = "RESOLVE" | "SUSPEND_USER" | "REMOVE_ITEM";

interface AbuseReport {
  id: string;
  reporterId?: string;
  reporterName?: string;
  reportedUserId?: string;
  reportedUserName?: string;
  reportedItemId?: string;
  reportedItemTitle?: string;
  reason: string;
  status: AbuseReportStatus;
  createdAt: string;
  resolvedAt?: string;
}

interface HealthCheck {
  name: string;
  status: string;
  details?: Record<string, unknown>;
  components?: HealthCheck[];
}

interface AdminHealthSummary {
  status: string;
  checkedAt: string;
  components: HealthCheck[];
}

const formatRelativeDate = (value: string) =>
  formatDistanceToNow(new Date(value), { addSuffix: true, locale: es });

const AdminDashboard = () => {
  const { toast } = useToast();
  const [reportStatusFilter, setReportStatusFilter] = useState<AbuseReportStatus | "ALL">("PENDING");

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
        label: "Éxito de trueques",
        value: metrics ? `${metrics.successRate.toFixed(1)}%` : "–",
        helper: metrics ? `${metrics.cancellationRate.toFixed(1)}% cancelados` : "–",
        icon: TrendingUp,
      },
      {
        label: "Publicaciones totales",
        value: totalItems,
        helper: `${activeItems} disponibles para trueque`,
        icon: BarChart3,
      },
      {
        label: "Impacto ecológico",
        value: metrics ? `${metrics.ecologicalImpactKg.toFixed(1)} kg CO₂e` : "–",
        helper: "Ahorro estimado por reutilización",
        icon: Leaf,
      },
    ];
  }, [items, metrics]);

  const {
    data: reports = [],
    isLoading: isLoadingReports,
    isError: isReportsError,
    error: reportsError,
    refetch: refetchReports,
  } = useQuery<AbuseReport[]>({
    queryKey: ["admin", "reports", reportStatusFilter],
    queryFn: () =>
      apiClient.get(
        `/api/admin/reports${reportStatusFilter === "ALL" ? "" : `?status=${reportStatusFilter}`}`,
      ),
  });

  const {
    data: healthSummary,
    isLoading: isLoadingHealth,
    isError: isHealthError,
    error: healthError,
    refetch: refetchHealth,
  } = useQuery<AdminHealthSummary>({
    queryKey: ["admin", "health"],
    queryFn: () => apiClient.get("/api/admin/health"),
    refetchInterval: 30000,
    refetchIntervalInBackground: true,
  });

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

  const tradeOutcomeSeries = useMemo(
    () => [
      {
        name: "Resultados",
        completados: metrics?.completedTrades ?? 0,
        cancelados: metrics?.cancelledTrades ?? 0,
      },
    ],
    [metrics],
  );

  const regionMetrics = useMemo(() => {
    if (!metrics?.locationMetrics) return [];

    return metrics.locationMetrics.map((entry) => ({
      ...entry,
      successRate:
        entry.totalTrades > 0 ? Number(((entry.completedTrades / entry.totalTrades) * 100).toFixed(1)) : 0,
    }));
  }, [metrics]);

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

  const tradeOutcomeConfig = useMemo(
    () => ({
      completados: { label: "Completados", color: "hsl(var(--chart-1))" },
      cancelados: { label: "Cancelados", color: "hsl(var(--chart-4))" },
    }),
    [],
  );

  const reportStatusOptions = useMemo(
    () => [
      { value: "ALL", label: "Todos" },
      { value: "PENDING", label: "Pendientes" },
      { value: "RESOLVED", label: "Resueltos" },
      { value: "SANCTIONED", label: "Sancionados" },
    ],
    [],
  );

  const getReportBadgeVariant = (status: AbuseReportStatus) => {
    if (status === "PENDING") return "secondary" as const;
    if (status === "SANCTIONED") return "destructive" as const;
    return "default" as const;
  };

  const getErrorMessage = (error: unknown, fallback: string) =>
    error instanceof Error ? error.message : fallback;

  const handleDisableItem = useCallback(
    async (item: AdminItem) => {
      const confirmed = window.confirm(`¿Deshabilitar la publicación "${item.title}"?`);
      if (!confirmed) return;

      try {
        await apiClient.patch(`/api/admin/items/${item.id}/disable`);
        toast({ title: "Publicación deshabilitada", description: "El ítem ya no estará disponible." });
        refetchItems();
      } catch (error: unknown) {
        toast({
          title: "No se pudo deshabilitar",
          description: getErrorMessage(error, "Intenta nuevamente más tarde."),
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
      } catch (error: unknown) {
        toast({
          title: "No se pudo eliminar",
          description: getErrorMessage(error, "Revisa la conexión e inténtalo otra vez."),
          variant: "destructive",
        });
      }
    },
    [refetchItems, toast],
  );

  const handleResolveReport = useCallback(
    async (report: AbuseReport) => {
      try {
        await apiClient.patch(`/api/admin/reports/${report.id}/resolve`);
        toast({ title: "Reporte resuelto", description: "El caso se marcó como atendido." });
        refetchReports();
      } catch (error: unknown) {
        toast({
          title: "No se pudo resolver el reporte",
          description: getErrorMessage(error, "Intenta nuevamente."),
          variant: "destructive",
        });
      }
    },
    [refetchReports, toast],
  );

  const handleSanctionReport = useCallback(
    async (report: AbuseReport, action: AbuseReportAction) => {
      const actionLabel = action === "SUSPEND_USER" ? "suspender" : "eliminar";
      const confirmed = window.confirm(
        `¿Confirmas ${actionLabel} ${action === "SUSPEND_USER" ? "al usuario" : "la publicación"}?`,
      );
      if (!confirmed) return;

      try {
        await apiClient.patch(`/api/admin/reports/${report.id}/sanction`, { action });
        toast({ title: "Sanción aplicada", description: "El reporte fue actualizado." });
        refetchReports();
        refetchItems();
        refetchUsers();
      } catch (error: unknown) {
        toast({
          title: "No se pudo aplicar la sanción",
          description: getErrorMessage(error, "Revisa la conexión e inténtalo de nuevo."),
          variant: "destructive",
        });
      }
    },
    [refetchItems, refetchReports, refetchUsers, toast],
  );

  useEffect(() => {
    if (
      !isUsersError &&
      !isItemsError &&
      !isMetricsError &&
      !isReportsError &&
      !isHealthError
    ) {
      return;
    }

    const description =
      (usersError || itemsError || metricsError || reportsError || healthError)?.message ??
      "No pudimos cargar la consola de administración.";
    toast({
      title: "Error al cargar datos de administración",
      description,
      variant: "destructive",
    });
  }, [
    isItemsError,
    isHealthError,
    isMetricsError,
    isReportsError,
    isUsersError,
    healthError,
    itemsError,
    metricsError,
    reportsError,
    toast,
    usersError,
  ]);

  const isAnyLoading =
    isLoadingItems || isLoadingUsers || isLoadingMetrics || isLoadingReports || isLoadingHealth;
  const isTableLoading = isLoadingItems || isLoadingUsers;

  const healthStatusStyles: Record<
    string,
    { icon: typeof AlertCircle; badgeClass: string; textClass: string }
  > = {
    UP: {
      icon: CheckCircle2,
      badgeClass: "border-emerald-200 bg-emerald-50 text-emerald-700",
      textClass: "text-emerald-700",
    },
    DOWN: {
      icon: XCircle,
      badgeClass: "border-destructive/40 bg-destructive/10 text-destructive",
      textClass: "text-destructive",
    },
    OUT_OF_SERVICE: {
      icon: AlertTriangle,
      badgeClass: "border-amber-200 bg-amber-50 text-amber-800",
      textClass: "text-amber-800",
    },
  };

  const getHealthDetail = (details?: Record<string, unknown>) => {
    if (!details) return "Sin detalles adicionales";

    const message = details.message || details.error || details.status;
    if (typeof message === "string") {
      return message;
    }

    const firstEntry = Object.entries(details)[0];
    if (firstEntry) {
      return `${firstEntry[0]}: ${String(firstEntry[1])}`;
    }

    return "Sin detalles adicionales";
  };

  const renderHealthChecks = (checks: HealthCheck[], path: string[] = []) =>
    checks.map((check, index) => {
      const key = [...path, `${check.name}-${index}`].join("/");
      const theme = healthStatusStyles[check.status] ?? healthStatusStyles.OUT_OF_SERVICE;
      const Icon = theme.icon;

      return (
        <div key={key} className="space-y-2">
          <div className="flex items-start gap-3 rounded-lg border bg-background p-3 shadow-sm">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-muted/60">
              <Icon className={`h-5 w-5 ${theme.textClass}`} />
            </div>
            <div className="flex-1 space-y-1">
              <div className="flex flex-wrap items-center gap-2">
                <p className="font-medium text-foreground">{check.name}</p>
                <Badge className={theme.badgeClass}>{check.status}</Badge>
              </div>
              <p className="text-sm text-muted-foreground">{getHealthDetail(check.details)}</p>
            </div>
          </div>

          {check.components?.length ? (
            <div className="space-y-2 border-l border-muted-foreground/20 pl-6">
              {renderHealthChecks(check.components, [...path, check.name])}
            </div>
          ) : null}
        </div>
      );
    });

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

          <Card className="shadow-sm">
            <CardHeader className="space-y-2">
              <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                <div className="space-y-1">
                  <CardTitle>Salud de servicios</CardTitle>
                  <CardDescription>
                    Consolidado de checks de base de datos, mensajería y dependencias externas.
                  </CardDescription>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant="outline" className="flex items-center gap-1">
                    <Clock3 className="h-4 w-4" />
                    {healthSummary ? formatRelativeDate(healthSummary.checkedAt) : "Sincronizando..."}
                  </Badge>
                  <Button variant="outline" size="sm" onClick={() => refetchHealth()} disabled={isLoadingHealth}>
                    {isLoadingHealth ? (
                      <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                      <div className="flex items-center gap-2">
                        <RefreshCcw className="h-4 w-4" />
                        <span>Refrescar</span>
                      </div>
                    )}
                  </Button>
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                <span>Actualización automática cada 30 segundos.</span>
                {healthSummary ? (
                  <Badge className={(healthStatusStyles[healthSummary.status] ?? healthStatusStyles.OUT_OF_SERVICE).badgeClass}>
                    Estado {healthSummary.status}
                  </Badge>
                ) : null}
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              {isLoadingHealth && !healthSummary ? (
                <div className="flex flex-col items-center justify-center gap-3 py-6 text-muted-foreground">
                  <Loader2 className="h-5 w-5 animate-spin" />
                  <p>Consultando salud de los servicios...</p>
                </div>
              ) : null}

              {isHealthError ? (
                <div className="flex items-center gap-3 rounded-lg border border-destructive/40 bg-destructive/10 p-3 text-destructive">
                  <AlertCircle className="h-5 w-5" />
                  <div className="text-sm flex-1">
                    <p className="font-medium">No se pudo obtener la salud de los servicios</p>
                    <p className="text-destructive/80">{healthError?.message ?? "Intenta refrescar más tarde."}</p>
                  </div>
                  <Button variant="outline" size="sm" onClick={() => refetchHealth()}>
                    Reintentar
                  </Button>
                </div>
              ) : null}

              {!isLoadingHealth && !isHealthError && healthSummary ? (
                <div className="space-y-3">{renderHealthChecks(healthSummary.components)}</div>
              ) : null}
            </CardContent>
          </Card>

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
            <Card className="shadow-sm lg:col-span-3">
              <CardHeader className="space-y-1">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <CardTitle>Éxitos vs cancelaciones</CardTitle>
                    <CardDescription>Resultado de los trueques cerrados recientemente.</CardDescription>
                  </div>
                  <Button variant="outline" size="sm" onClick={() => refetchMetrics()} disabled={isLoadingMetrics}>
                    {isLoadingMetrics ? <Loader2 className="h-4 w-4 animate-spin" /> : "Refrescar"}
                  </Button>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {!isLoadingMetrics && !isMetricsError && metrics ? (
                  <div className="grid gap-4 lg:grid-cols-3">
                    <div className="rounded-lg border bg-muted/40 p-4">
                      <p className="text-muted-foreground">Tasa de éxito</p>
                      <p className="text-3xl font-bold text-foreground">{metrics.successRate.toFixed(1)}%</p>
                      <p className="text-xs text-muted-foreground">{metrics.completedTrades} completados</p>
                    </div>
                    <div className="rounded-lg border bg-muted/40 p-4">
                      <p className="text-muted-foreground">Cancelaciones</p>
                      <p className="text-3xl font-bold text-foreground">{metrics.cancellationRate.toFixed(1)}%</p>
                      <p className="text-xs text-muted-foreground">{metrics.cancelledTrades} cancelados</p>
                    </div>
                    <div className="rounded-lg border bg-muted/40 p-4">
                      <p className="text-muted-foreground">Impacto ecológico</p>
                      <p className="text-3xl font-bold text-foreground">{metrics.ecologicalImpactKg.toFixed(1)} kg</p>
                      <p className="text-xs text-muted-foreground">CO₂e evitados en total</p>
                    </div>
                  </div>
                ) : null}

                {!isLoadingMetrics && !isMetricsError && metrics ? (
                  <ChartContainer config={tradeOutcomeConfig} className="h-[260px]">
                    <BarChart data={tradeOutcomeSeries} margin={{ left: 12, right: 12 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis dataKey="name" tickLine={false} axisLine={false} tickMargin={8} />
                      <YAxis allowDecimals={false} tickLine={false} axisLine={false} tickMargin={8} width={36} />
                      <ChartTooltip content={<ChartTooltipContent />} />
                      <Bar dataKey="completados" stackId="status" fill="var(--color-completados)" radius={6} />
                      <Bar dataKey="cancelados" stackId="status" fill="var(--color-cancelados)" radius={6} />
                      <ChartLegend
                        verticalAlign="bottom"
                        content={<ChartLegendContent className="-mb-2 mt-2 text-muted-foreground" />}
                      />
                    </BarChart>
                  </ChartContainer>
                ) : null}
              </CardContent>
            </Card>

            <Card className="shadow-sm lg:col-span-2">
              <CardHeader className="space-y-1">
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4 text-primary" />
                    <div>
                      <CardTitle>Actividad por región</CardTitle>
                      <CardDescription>Comunas con mayor volumen de trueques cerrados.</CardDescription>
                    </div>
                  </div>
                  <Badge variant="outline">{regionMetrics.length} zonas</Badge>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {regionMetrics.length === 0 ? (
                  <div className="flex flex-col items-center justify-center gap-3 py-8 text-muted-foreground">
                    <AlertCircle className="h-5 w-5" />
                    <p>No hay datos regionales disponibles.</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    <div className="grid grid-cols-4 gap-2 text-xs font-medium text-muted-foreground">
                      <span>Región</span>
                      <span>Comuna</span>
                      <span className="text-right">Total</span>
                      <span className="text-right">Éxito</span>
                    </div>
                    <div className="space-y-2">
                      {regionMetrics.slice(0, 6).map((entry) => (
                        <div
                          key={`${entry.region}-${entry.commune}`}
                          className="grid grid-cols-4 items-center gap-2 rounded-lg border bg-muted/30 px-3 py-2 text-sm"
                        >
                          <span className="truncate font-medium text-foreground" title={entry.region}>
                            {entry.region}
                          </span>
                          <span className="truncate text-muted-foreground" title={entry.commune}>
                            {entry.commune}
                          </span>
                          <span className="text-right font-semibold">{entry.totalTrades}</span>
                          <span className="text-right text-emerald-600">{entry.successRate}%</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
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

          <Card className="shadow-sm">
            <CardHeader className="space-y-1">
              <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div>
                  <CardTitle>Reportes de abuso</CardTitle>
                  <CardDescription>Gestiona denuncias de usuarios y publicaciones.</CardDescription>
                </div>
                <div className="flex flex-wrap items-center gap-3">
                  <Select
                    value={reportStatusFilter}
                    onValueChange={(value) =>
                      setReportStatusFilter(value as AbuseReportStatus | "ALL")
                    }
                  >
                    <SelectTrigger className="w-[180px]">
                      <SelectValue placeholder="Filtrar por estado" />
                    </SelectTrigger>
                    <SelectContent>
                      {reportStatusOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Button variant="outline" size="sm" onClick={() => refetchReports()} disabled={isLoadingReports}>
                    {isLoadingReports ? <Loader2 className="h-4 w-4 animate-spin" /> : "Actualizar"}
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              {(isLoadingReports && reports.length === 0) || (isReportsError && reports.length === 0) ? (
                <div className="flex flex-col items-center justify-center gap-3 py-8 text-muted-foreground">
                  {isLoadingReports ? <Loader2 className="h-6 w-6 animate-spin" /> : <AlertCircle className="h-6 w-6" />}
                  <p>
                    {isLoadingReports
                      ? "Cargando reportes..."
                      : reportsError?.message ?? "No pudimos cargar los reportes."}
                  </p>
                </div>
              ) : null}

              {!isLoadingReports && !isReportsError && (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Motivo</TableHead>
                      <TableHead>Denunciante</TableHead>
                      <TableHead>Objetivo</TableHead>
                      <TableHead>Estado</TableHead>
                      <TableHead>Creado</TableHead>
                      <TableHead>Resuelto</TableHead>
                      <TableHead className="text-right">Acciones</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {reports.map((report) => {
                      const isClosed = report.status !== "PENDING";
                      const targetLabel = report.reportedItemTitle
                        ? `Publicación: ${report.reportedItemTitle}`
                        : report.reportedUserName
                          ? `Usuario: ${report.reportedUserName}`
                          : "Sin objetivo";

                      return (
                        <TableRow key={report.id}>
                          <TableCell className="max-w-[280px]">
                            <div className="flex flex-col">
                              <span className="font-medium text-foreground line-clamp-2">{report.reason}</span>
                              <span className="text-xs text-muted-foreground">ID: {report.id.slice(0, 8)}</span>
                            </div>
                          </TableCell>
                          <TableCell className="text-muted-foreground">
                            {report.reporterName ?? report.reporterId?.slice(0, 8) ?? "–"}
                          </TableCell>
                          <TableCell className="text-muted-foreground">{targetLabel}</TableCell>
                          <TableCell>
                            <Badge variant={getReportBadgeVariant(report.status)}>{report.status}</Badge>
                          </TableCell>
                          <TableCell className="text-muted-foreground">{formatRelativeDate(report.createdAt)}</TableCell>
                          <TableCell className="text-muted-foreground">
                            {report.resolvedAt ? formatRelativeDate(report.resolvedAt) : "–"}
                          </TableCell>
                          <TableCell className="text-right space-x-2">
                            <Button
                              variant="outline"
                              size="sm"
                              disabled={isClosed}
                              onClick={() => handleResolveReport(report)}
                            >
                              Resolver
                            </Button>
                            {report.reportedUserId ? (
                              <Button
                                variant="outline"
                                size="sm"
                                disabled={isClosed}
                                onClick={() => handleSanctionReport(report, "SUSPEND_USER")}
                              >
                                Suspender usuario
                              </Button>
                            ) : null}
                            {report.reportedItemId ? (
                              <Button
                                variant="destructive"
                                size="sm"
                                disabled={isClosed}
                                onClick={() => handleSanctionReport(report, "REMOVE_ITEM")}
                              >
                                Eliminar publicación
                              </Button>
                            ) : null}
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>

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
