import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Send } from "lucide-react";

interface MessageModalProps {
  isOpen: boolean;
  onClose: () => void;
  productTitle: string;
  userName: string;
}

const MessageModal = ({ isOpen, onClose, productTitle, userName }: MessageModalProps) => {
  const [message, setMessage] = useState("");
  const [offer, setOffer] = useState("");

  const handleSend = () => {
    // TODO: Implement message sending logic
    console.log("Enviando mensaje:", { message, offer, productTitle, userName });
    setMessage("");
    setOffer("");
    onClose();
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
            disabled={!offer.trim()}
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