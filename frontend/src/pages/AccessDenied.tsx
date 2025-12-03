import Footer from "@/components/Footer";
import Header from "@/components/Header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ShieldAlert, ArrowLeft } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";

const AccessDenied = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1 bg-muted/30 py-12">
        <div className="container mx-auto px-4 max-w-3xl">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="-ml-2 mb-6"
            onClick={() => navigate(-1)}
          >
            <ArrowLeft className="mr-1 h-4 w-4" />
            Volver
          </Button>

          <Card className="shadow-sm">
            <CardHeader className="space-y-3">
              <div className="flex items-center gap-3">
                <div className="rounded-full bg-amber-100 p-3 text-amber-600">
                  <ShieldAlert className="h-6 w-6" />
                </div>
                <div>
                  <CardTitle className="text-2xl">Acceso restringido</CardTitle>
                  <CardDescription>
                    Necesitas permisos de administrador para acceder a esta sección.
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <p className="text-sm text-muted-foreground">
                Si crees que deberías tener acceso, contacta con el equipo de soporte o con el administrador de tu
                organización para solicitar los permisos necesarios.
              </p>
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-4">
                <Button asChild className="w-full sm:w-auto">
                  <Link to="/">Ir al inicio</Link>
                </Button>
                <Button variant="outline" className="w-full sm:w-auto" onClick={() => navigate(0)}>
                  Reintentar
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default AccessDenied;
