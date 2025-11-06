import { Button } from "@/components/ui/button";
import { ArrowRight } from "lucide-react";
import heroImage from "@/assets/hero-barter.jpg";
import SearchBar from "@/components/SearchBar";

const Hero = () => {
  return (
    <section className="relative min-h-[80vh] flex items-center justify-center bg-gradient-hero overflow-hidden">
      <div className="container mx-auto px-4 grid lg:grid-cols-2 gap-12 items-center z-10">
        <div className="text-center lg:text-left space-y-8">
          <div className="space-y-4">
            <h1 className="text-4xl md:text-6xl font-bold text-primary-foreground leading-tight">
              Intercambia
              <span className="block text-white/90">sin dinero</span>
            </h1>
            <p className="text-xl text-white/80 max-w-lg">
              Únete a la comunidad de trueque más grande. Intercambia bienes y servicios 
              de forma segura y organizada.
            </p>
          </div>
          
          <SearchBar
            className="max-w-md gap-4"
            placeholder="¿Qué buscas intercambiar?"
            inputClassName="h-12 bg-white/90 border-white/20 text-foreground placeholder:text-muted-foreground pl-12"
            iconClassName="left-4 top-1/2 h-5 w-5"
            actionSlot={
              <Button
                type="submit"
                size="lg"
                className="bg-white text-primary hover:bg-white/90 h-12 px-6"
              >
                Buscar
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            }
          />

          <div className="flex flex-wrap gap-6 text-white/80">
            <div className="text-center">
              <div className="text-2xl font-bold text-white">10,000+</div>
              <div className="text-sm">Usuarios activos</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-white">25,000+</div>
              <div className="text-sm">Intercambios exitosos</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-white">500+</div>
              <div className="text-sm">Categorías</div>
            </div>
          </div>
        </div>

        <div className="hidden lg:block">
          <img 
            src={heroImage} 
            alt="Personas intercambiando productos en la plataforma TQ"
            className="w-full max-w-lg mx-auto rounded-2xl shadow-2xl"
          />
        </div>
      </div>
      
      {/* Background decoration */}
      <div className="absolute inset-0 bg-gradient-to-r from-primary/20 to-transparent"></div>
      <div className="absolute top-20 right-20 w-64 h-64 bg-white/10 rounded-full blur-3xl"></div>
      <div className="absolute bottom-20 left-20 w-48 h-48 bg-white/5 rounded-full blur-2xl"></div>
    </section>
  );
};

export default Hero;