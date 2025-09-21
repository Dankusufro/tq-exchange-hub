import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { 
  Facebook, 
  Twitter, 
  Instagram, 
  Mail,
  MapPin,
  Phone,
  ExternalLink
} from "lucide-react";

const Footer = () => {
  return (
    <footer className="bg-card border-t">
      <div className="container mx-auto px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Logo and description */}
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <div className="bg-gradient-primary rounded-lg p-2">
                <div className="text-white font-bold text-xl">TQ</div>
              </div>
              <div>
                <div className="font-bold text-lg text-foreground">TruequePlus</div>
                <div className="text-xs text-muted-foreground">Intercambia sin límites</div>
              </div>
            </div>
            <p className="text-sm text-muted-foreground">
              La plataforma de trueque más confiable y segura de Latinoamérica. 
              Intercambia bienes y servicios con nuestra comunidad.
            </p>
            <div className="flex gap-2">
              <Button variant="ghost" size="sm">
                <Facebook className="h-4 w-4" />
              </Button>
              <Button variant="ghost" size="sm">
                <Twitter className="h-4 w-4" />
              </Button>
              <Button variant="ghost" size="sm">
                <Instagram className="h-4 w-4" />
              </Button>
            </div>
          </div>

          {/* Quick links */}
          <div className="space-y-4">
            <h3 className="font-semibold text-foreground">Enlaces rápidos</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Cómo funciona</a></li>
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Categorías</a></li>
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Comunidad</a></li>
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Blog</a></li>
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Ayuda</a></li>
            </ul>
          </div>

          {/* Support */}
          <div className="space-y-4">
            <h3 className="font-semibold text-foreground">Soporte</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Centro de ayuda</a></li>
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Términos de uso</a></li>
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Política de privacidad</a></li>
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Seguridad</a></li>
              <li><a href="#" className="text-muted-foreground hover:text-primary transition-colors">Contacto</a></li>
            </ul>
          </div>

          {/* Newsletter */}
          <div className="space-y-4">
            <h3 className="font-semibold text-foreground">Mantente al día</h3>
            <p className="text-sm text-muted-foreground">
              Recibe notificaciones sobre nuevos productos y ofertas especiales.
            </p>
            <div className="space-y-2">
              <Input placeholder="Tu email" />
              <Button className="w-full">
                Suscribirse
                <Mail className="ml-2 h-4 w-4" />
              </Button>
            </div>
            
            <div className="space-y-2 text-xs text-muted-foreground">
              <div className="flex items-center gap-2">
                <MapPin className="h-3 w-3" />
                <span>México, CDMX</span>
              </div>
              <div className="flex items-center gap-2">
                <Phone className="h-3 w-3" />
                <span>+52 55 1234 5678</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="h-3 w-3" />
                <span>hola@truequeplus.com</span>
              </div>
            </div>
          </div>
        </div>

        <div className="border-t mt-8 pt-8 flex flex-col sm:flex-row justify-between items-center gap-4">
          <div className="text-sm text-muted-foreground">
            © 2024 TruequePlus. Todos los derechos reservados.
          </div>
          <div className="flex items-center gap-4 text-xs text-muted-foreground">
            <span>Hecho con ❤️ en México</span>
            <Button variant="ghost" size="sm" className="h-auto p-0 text-xs">
              Powered by Lovable
              <ExternalLink className="ml-1 h-3 w-3" />
            </Button>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;