import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { 
  Search, 
  Plus, 
  MessageCircle, 
  Bell, 
  User,
  Menu,
  Heart
} from "lucide-react";

const Header = () => {
  return (
    <header className="bg-card border-b sticky top-0 z-50 backdrop-blur-sm bg-card/95">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <div className="bg-gradient-primary rounded-lg p-2">
              <div className="text-white font-bold text-xl">TQ</div>
            </div>
            <div className="hidden sm:block">
              <div className="font-bold text-lg text-foreground">TruequePlus</div>
              <div className="text-xs text-muted-foreground -mt-1">Intercambia sin límites</div>
            </div>
          </div>

          {/* Search bar - Desktop */}
          <div className="hidden md:flex flex-1 max-w-md mx-8">
            <div className="relative w-full">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input 
                placeholder="Buscar productos o servicios..."
                className="pl-10 h-10"
              />
            </div>
          </div>

          {/* Navigation */}
          <div className="flex items-center gap-2">
            <Button variant="outline" className="hidden sm:flex items-center gap-2">
              <Plus className="h-4 w-4" />
              Publicar
            </Button>
            
            <Button variant="ghost" size="sm" className="relative">
              <MessageCircle className="h-5 w-5" />
              <Badge className="absolute -top-1 -right-1 h-5 w-5 p-0 flex items-center justify-center text-xs">
                3
              </Badge>
            </Button>
            
            <Button variant="ghost" size="sm" className="relative">
              <Bell className="h-5 w-5" />
              <Badge className="absolute -top-1 -right-1 h-5 w-5 p-0 flex items-center justify-center text-xs">
                5
              </Badge>
            </Button>
            
            <Button variant="ghost" size="sm">
              <Heart className="h-5 w-5" />
            </Button>
            
            <Button variant="ghost" size="sm">
              <User className="h-5 w-5" />
            </Button>
            
            <Button variant="ghost" size="sm" className="md:hidden">
              <Menu className="h-5 w-5" />
            </Button>
          </div>
        </div>

        {/* Mobile search */}
        <div className="pb-4 md:hidden">
          <div className="relative">
            <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
            <Input 
              placeholder="Buscar productos o servicios..."
              className="pl-10 h-10"
            />
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;