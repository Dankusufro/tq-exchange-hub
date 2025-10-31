import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { formatDistanceToNow } from "date-fns";
import { es } from "date-fns/locale";
import { Check, Clock, X, LogIn, Ban, Download, Mail } from "lucide-react";

import useTradeRequests, { type TradeRequest } from "@/hooks/use-trade-requests";
import { useAuth } from "@/providers/AuthProvider";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { toast } from "@/components/ui/use-toast";

const statusCopy: Record<string, string> = {
  pending: "Pendiente",
  accepted: "Aceptada",
  rejected: "Rechazada",
  cancelled: "Cancelada",
};

const TradeRequests = () => {
  const { user } = useAuth();
  const {
    requests,
    isLoading,
    isFetching,
    error,
    acceptRequest,
    rejectRequest,
    cancelRequest,
    downloadReceipt,
    sendReceiptByEmail,
  } = useTradeRequests({
    status: ["pending", "accepted", "rejected", "cancelled"],
  });
  const [selectedRequestId, setSelectedRequestId] = useState<string | null>(null);
  const [receiptActionId, setReceiptActionId] = useState<string | null>(null);

  const sortedRequests = useMemo(
    () =>
      [...requests].sort((a, b) => {
        if (a.status === b.status) {
          return new Date(b.created_at).getTime() - new Date(a.created_at).getTime();
        }

        if (a.status === "pending") return -1;
        if (b.status === "pending") return 1;
        if (a.status === "accepted") return -1;
        if (b.status === "accepted") return 1;
        return 0;
      }),
    [requests],
  );

  const handleAction = async (id: string, action: "accept" | "reject") => {
    try {
      setSelectedRequestId(id);

      if (action === "accept") {
        await acceptRequest(id);
        toast({
          title: "Solicitud aceptada",
          description: "Has aceptado la propuesta de intercambio.",
        });
      } else {
        await rejectRequest(id);
        toast({
          title: "Solicitud rechazada",
          description: "Has rechazado la propuesta de intercambio.",
        });
      }
    } catch (requestError) {
      const description = requestError instanceof Error ? requestError.message : "Ocurrió un error inesperado";
      toast({
        title: "No se pudo actualizar la solicitud",
        description,
        variant: "destructive",
      });
    } finally {
      setSelectedRequestId(null);
    }
  };

  const handleCancel = async (id: string) => {
    try {
      setSelectedRequestId(id);
      await cancelRequest(id);
      toast({
        title: "Solicitud cancelada",
        description: "Has cancelado tu propuesta de intercambio.",
      });
    } catch (requestError) {
      const description =
        requestError instanceof Error ? requestError.message : "Ocurrió un error inesperado";
      toast({
        title: "No se pudo cancelar la solicitud",
        description,
        variant: "destructive",
      });
    } finally {
      setSelectedRequestId(null);
    }
  };

  const handleDownloadReceipt = async (request: TradeRequest) => {
    try {
      const blob = await downloadReceipt(request.id);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      const shortId = request.id.slice(0, 8);
      link.download = `comprobante-trueque-${shortId}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast({
        title: "Comprobante descargado",
        description: "Se generó el comprobante en PDF para este trueque.",
      });
    } catch (error) {
      const description =
        error instanceof Error ? error.message : "No pudimos generar el comprobante en este momento.";
      toast({
        title: "Error al generar comprobante",
        description,
        variant: "destructive",
      });
    }
  };

  const handleSendReceipt = async (id: string) => {
    try {
      setReceiptActionId(id);
      await sendReceiptByEmail(id);
      toast({
        title: "Comprobante enviado",
        description: "Hemos enviado el comprobante por correo a los participantes del trueque.",
      });
    } catch (error) {
      const description =
        error instanceof Error
          ? error.message
          : "No pudimos enviar el comprobante por correo electrónico.";
      toast({
        title: "Error al enviar comprobante",
        description,
        variant: "destructive",
      });
    } finally {
      setReceiptActionId(null);
    }
  };

  if (!user) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Solicitudes de intercambio</CardTitle>
          <CardDescription>Inicia sesión para gestionar tus solicitudes pendientes.</CardDescription>
        </CardHeader>
        <CardContent>
          <Button asChild className="gap-2">
            <Link to="/auth">
              <LogIn className="h-4 w-4" />
              Iniciar sesión
            </Link>
          </Button>
        </CardContent>
      </Card>
    );
  }

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Solicitudes de intercambio</CardTitle>
          <CardDescription>Estamos cargando tus solicitudes...</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-sm text-muted-foreground">Cargando...</div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Solicitudes de intercambio</CardTitle>
          <CardDescription>Ocurrió un problema al obtener las solicitudes.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-sm text-destructive">{error}</div>
        </CardContent>
      </Card>
    );
  }

  if (sortedRequests.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Solicitudes de intercambio</CardTitle>
          <CardDescription>Cuando alguien te proponga un intercambio aparecerá aquí.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-sm text-muted-foreground">No tienes solicitudes por ahora.</div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="shadow-card-hover border-border/50">
      <CardHeader>
        <CardTitle>Solicitudes de intercambio</CardTitle>
        <CardDescription>
          Gestiona las propuestas que has recibido recientemente.
          {isFetching && <span className="ml-2 text-xs text-muted-foreground">Actualizando...</span>}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {sortedRequests.map((request, index) => {
          const isOwner = request.owner_id === user.id;
          const isRequester = request.requester_id === user.id;
          const isPending = request.status === "pending";
          const isAccepted = request.status === "accepted";

          return (
            <div key={request.id} className="space-y-3">
              <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-semibold text-foreground">Propuesta #{request.id.slice(0, 6)}</h3>
                    <Badge variant="outline" className="capitalize">
                      {statusCopy[request.status] ?? request.status}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {request.message ? request.message : "Sin mensaje adicional."}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    Recibida {formatDistanceToNow(new Date(request.created_at), { addSuffix: true, locale: es })}
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  {isOwner && isPending && (
                    <>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleAction(request.id, "reject")}
                        disabled={selectedRequestId === request.id}
                        className="gap-2"
                      >
                        <X className="h-4 w-4" />
                        Rechazar
                      </Button>
                      <Button
                        size="sm"
                        onClick={() => handleAction(request.id, "accept")}
                        disabled={selectedRequestId === request.id}
                        className="gap-2"
                      >
                        <Check className="h-4 w-4" />
                        Aceptar
                      </Button>
                    </>
                  )}
                  {isRequester && isPending && (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleCancel(request.id)}
                      disabled={selectedRequestId === request.id}
                      className="gap-2"
                    >
                      <Ban className="h-4 w-4" />
                      Cancelar
                    </Button>
                  )}
                  {isAccepted && (
                    <>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleDownloadReceipt(request)}
                        className="gap-2"
                      >
                        <Download className="h-4 w-4" />
                        Descargar comprobante
                      </Button>
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => handleSendReceipt(request.id)}
                        disabled={receiptActionId === request.id}
                        className="gap-2"
                      >
                        <Mail className="h-4 w-4" />
                        Enviar por correo
                      </Button>
                    </>
                  )}
                </div>
              </div>
              <div className="flex flex-wrap gap-3 text-xs text-muted-foreground">
                <span className="inline-flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  Última actualización: {formatDistanceToNow(new Date(request.updated_at), { addSuffix: true, locale: es })}
                </span>
                <span>ID del propietario: {request.owner_id}</span>
                <span>ID del solicitante: {request.requester_id}</span>
              </div>
              {index < sortedRequests.length - 1 && <Separator />}
            </div>
          );
        })}
      </CardContent>
    </Card>
  );
};

export default TradeRequests;
