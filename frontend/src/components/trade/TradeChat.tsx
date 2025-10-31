import { useEffect, useMemo, useRef, useState } from "react";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { apiClient } from "@/lib/api";
import { useChatChannel, type ChatMessage } from "@/hooks/use-chat-channel";
import { useAuth } from "@/providers/AuthProvider";
import { useMutation, useQuery } from "@tanstack/react-query";
import { format } from "date-fns";
import { es } from "date-fns/locale";
import { Loader2, SendHorizonal, Wifi, WifiOff } from "lucide-react";
import { useToast } from "@/components/ui/use-toast";

interface MessageDto {
  id: string;
  tradeId: string;
  senderId: string;
  content: string;
  createdAt: string;
}

interface TradeChatProps {
  tradeId: string | null;
  className?: string;
}

const formatMessageTimestamp = (value: string) => {
  try {
    return format(new Date(value), "PPpp", { locale: es });
  } catch (error) {
    console.error("Failed to format chat timestamp", error);
    return "Hace un momento";
  }
};

const sortMessages = (messages: MessageDto[]) =>
  [...messages].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
  );

const TradeChat = ({ tradeId, className }: TradeChatProps) => {
  const { session } = useAuth();
  const { toast } = useToast();
  const profileId = session?.profile.id ?? null;
  const [messages, setMessages] = useState<MessageDto[]>([]);
  const [draft, setDraft] = useState("");
  const scrollRef = useRef<HTMLDivElement | null>(null);

  const enabled = Boolean(tradeId && profileId);

  const messagesQuery = useQuery({
    queryKey: ["tradeMessages", tradeId],
    queryFn: async () => {
      const response = await apiClient.get<MessageDto[]>(`/api/messages/trade/${tradeId}`);
      return sortMessages(response);
    },
    enabled,
  });

  useEffect(() => {
    if (messagesQuery.data) {
      setMessages(messagesQuery.data);
    } else if (!enabled) {
      setMessages([]);
    }
  }, [enabled, messagesQuery.data]);

  const { state: connectionState } = useChatChannel({
    tradeId: tradeId ?? "",
    enabled,
    onMessage: (message: ChatMessage) => {
      setMessages((previous) => {
        if (previous.some((existing) => existing.id === message.id)) {
          return previous;
        }
        return sortMessages([...previous, message]);
      });
    },
  });

  useEffect(() => {
    if (!scrollRef.current) {
      return;
    }

    scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
  }, [messages]);

  const sendMessageMutation = useMutation({
    mutationFn: async (content: string) => {
      if (!tradeId) {
        throw new Error("No se seleccionó ningún trueque");
      }

      const payload = { tradeId, content };
      await apiClient.post<MessageDto>("/api/messages", payload);
    },
    onError: (error) => {
      const description =
        error instanceof Error
          ? error.message
          : "No se pudo enviar tu mensaje. Inténtalo nuevamente.";
      toast({
        title: "Error al enviar mensaje",
        description,
        variant: "destructive",
      });
    },
    onSuccess: () => {
      setDraft("");
    },
  });

  const connectionBadge = useMemo(() => {
    const isConnected = connectionState === "connected";
    const Icon = isConnected ? Wifi : WifiOff;
    const variant = isConnected ? "secondary" : "outline";
    const label = isConnected ? "Conectado" : connectionState === "connecting" ? "Conectando" : "Sin conexión";

    return (
      <Badge variant={variant} className="flex items-center gap-1">
        <Icon className="h-3.5 w-3.5" />
        {label}
      </Badge>
    );
  }, [connectionState]);

  const handleSubmit: React.FormEventHandler<HTMLFormElement> = async (event) => {
    event.preventDefault();
    const trimmed = draft.trim();
    if (!trimmed || !tradeId || sendMessageMutation.isPending) {
      return;
    }

    await sendMessageMutation.mutateAsync(trimmed);
  };

  return (
    <Card className={cn("h-full", className)}>
      <CardHeader className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-1">
          <CardTitle>Chat del trueque</CardTitle>
          <CardDescription>
            Conversa en tiempo real con la otra persona involucrada en este intercambio.
          </CardDescription>
        </div>
        {connectionBadge}
      </CardHeader>
      <CardContent className="flex h-full flex-col gap-4">
        {!tradeId ? (
          <div className="flex h-full flex-col items-center justify-center rounded-md border border-dashed p-8 text-center text-sm text-muted-foreground">
            Selecciona un trueque activo para habilitar la conversación.
          </div>
        ) : (
          <>
            <div ref={scrollRef} className="h-64 overflow-y-auto rounded-md border">
              <div className="flex flex-col gap-3 p-4">
                {messages.length === 0 && !messagesQuery.isLoading ? (
                  <p className="text-sm text-muted-foreground">Todavía no hay mensajes en este trueque.</p>
                ) : null}

                {messagesQuery.isError && (
                  <p className="text-sm text-destructive">
                    No pudimos cargar el historial del chat. Intenta nuevamente en unos minutos.
                  </p>
                )}

                {messages.map((message) => {
                  const isOwn = message.senderId === profileId;
                  return (
                    <div key={message.id} className={cn("flex", isOwn ? "justify-end" : "justify-start") }>
                      <div
                        className={cn(
                          "max-w-[80%] rounded-lg px-3 py-2 text-sm shadow-sm",
                          isOwn ? "bg-primary text-primary-foreground" : "bg-muted text-foreground",
                        )}
                      >
                        <p className="whitespace-pre-wrap break-words">{message.content}</p>
                        <span className="mt-2 block text-xs opacity-80">
                          {formatMessageTimestamp(message.createdAt)}
                        </span>
                      </div>
                    </div>
                  );
                })}

                {messagesQuery.isLoading && (
                  <div className="flex justify-center py-4 text-sm text-muted-foreground">
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Cargando mensajes...
                  </div>
                )}
              </div>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-3">
              <Textarea
                placeholder="Escribe tu mensaje..."
                value={draft}
                onChange={(event) => setDraft(event.target.value)}
                disabled={sendMessageMutation.isPending || !enabled}
                className="min-h-[100px]"
              />
              <div className="flex justify-end">
                <Button
                  type="submit"
                  className="gap-2"
                  disabled={
                    !draft.trim() ||
                    sendMessageMutation.isPending ||
                    connectionState === "disconnected" ||
                    !enabled
                  }
                >
                  {sendMessageMutation.isPending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <SendHorizonal className="h-4 w-4" />
                  )}
                  Enviar
                </Button>
              </div>
            </form>
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default TradeChat;
