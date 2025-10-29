import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Send } from "lucide-react";
import { apiClient } from "@/lib/api";
import { toast } from "@/hooks/use-toast";

interface MessageModalProps {
  isOpen: boolean;
  onClose: () => void;
  productId: string;
  ownerId: string;
  productTitle: string;
  userName: string;
}

const MessageModal = ({
  isOpen,
  onClose,
  productId,
  ownerId,
  productTitle,
  userName,
}: MessageModalProps) => {
  const [message, setMessage] = useState("");
  const [offer, setOffer] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resetForm = () => {
    setMessage("");
    setOffer("");
  };

  const handleSend = async () => {
    if (isSubmitting) {
      return;
    }

    const trimmedOffer = offer.trim();
    const trimmedMessage = message.trim();

    if (!trimmedOffer) {
      return;
    }

    if (!productId || !ownerId) {
      toast({
        title: "No se pudo enviar tu propuesta",
        description: "Falta información del intercambio. Intenta más tarde.",
        variant: "destructive",
      });
      return;
    }

    const messageParts = [trimmedOffer];
    if (trimmedMessage) {
      messageParts.push(trimmedMessage);
    }

    const composedMessage = messageParts.join("\n\n");

    setIsSubmitting(true);

    try {
      await apiClient.post("/api/trades", {
        ownerItemId: productId,
        message: composedMessage,
      });

      toast({
        title: "Propuesta enviada",
        description: `Tu solicitud fue enviada a ${userName}.`,
      });

      resetForm();
      onClose();
    } catch (error) {
      const description =
        error instanceof Error
          ? error.message
          : "No se pudo enviar la propuesta. Inténtalo nuevamente.";

      toast({
        title: "No se pudo enviar tu propuesta",
        description,
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Proponer Intercambio</DialogTitle>
          <DialogDescription>
            Envía un mensaje a {userName} para intercambiar "{productTitle}"
          </DialogDescription>
        </DialogHeader>
        
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="offer">¿Qué ofreces a cambio?</Label>
            <Input
              id="offer"
              placeholder="Ej: Bicicleta mountain bike, Libros de programación..."
              value={offer}
              onChange={(e) => setOffer(e.target.value)}
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="message">Mensaje (opcional)</Label>
            <Textarea
              id="message"
              placeholder="Escribe un mensaje adicional sobre tu propuesta de intercambio..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              className="min-h-[100px]"
            />
          </div>
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={onClose}>
            Cancelar
          </Button>
          <Button
            onClick={handleSend}
            disabled={!offer.trim() || isSubmitting}
            className="gap-2"
          >
            <Send className="h-4 w-4" />
            Enviar Propuesta
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default MessageModal;