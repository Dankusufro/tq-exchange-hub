import { useMemo, useState } from "react";
import { Download, Loader2, Mail } from "lucide-react";

import useTradeReceipt from "@/hooks/use-trade-receipt";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/components/ui/use-toast";

interface TradeReceiptActionsProps {
  tradeId: string;
}

const TradeReceiptActions = ({ tradeId }: TradeReceiptActionsProps) => {
  const { downloadReceipt, emailReceipt, isDownloading, isSending } = useTradeReceipt();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [email, setEmail] = useState("");

  const isEmailValid = useMemo(() => {
    if (!email) return false;
    return /.+@.+\..+/.test(email);
  }, [email]);

  const handleDownload = async () => {
    try {
      await downloadReceipt(tradeId);
      toast({
        title: "Comprobante descargado",
        description: "Guardamos el comprobante en tu dispositivo.",
      });
    } catch (error) {
      const description =
        error instanceof Error ? error.message : "No pudimos descargar el comprobante.";
      toast({
        title: "Error al descargar",
        description,
        variant: "destructive",
      });
    }
  };

  const handleOpenEmailDialog = () => {
    if (isSending) return;
    setIsDialogOpen(true);
  };

  const handleSendEmail = async () => {
    if (!isEmailValid) {
      toast({
        title: "Correo no válido",
        description: "Revisa la dirección de correo electrónico antes de enviar.",
        variant: "destructive",
      });
      return;
    }

    try {
      await emailReceipt(tradeId, email);
      setIsDialogOpen(false);
      setEmail("");
      toast({
        title: "Comprobante enviado",
        description: "Enviaremos el PDF al correo indicado.",
      });
    } catch (error) {
      const description =
        error instanceof Error ? error.message : "No pudimos enviar el comprobante.";
      toast({
        title: "Error al enviar",
        description,
        variant: "destructive",
      });
    }
  };

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap gap-2">
        <Button onClick={handleDownload} disabled={isDownloading} className="gap-2">
          {isDownloading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
          Descargar comprobante
        </Button>
        <Button variant="outline" onClick={handleOpenEmailDialog} disabled={isSending} className="gap-2">
          {isSending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Mail className="h-4 w-4" />}
          Enviar por correo
        </Button>
      </div>

      <Dialog open={isDialogOpen} onOpenChange={(open) => !isSending && setIsDialogOpen(open)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Enviar comprobante por correo</DialogTitle>
            <DialogDescription>
              Ingresa la dirección de correo electrónico a la que enviaremos el comprobante en PDF.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor="receipt-email">Correo electrónico</Label>
              <Input
                id="receipt-email"
                type="email"
                placeholder="usuario@correo.com"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                autoFocus
              />
            </div>
          </div>
          <DialogFooter className="gap-2 sm:gap-0">
            <Button variant="outline" onClick={() => setIsDialogOpen(false)} disabled={isSending}>
              Cancelar
            </Button>
            <Button onClick={handleSendEmail} disabled={isSending || !isEmailValid} className="gap-2">
              {isSending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Mail className="h-4 w-4" />}
              Enviar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default TradeReceiptActions;
