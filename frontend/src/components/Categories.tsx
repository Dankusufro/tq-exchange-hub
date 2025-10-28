import { Button } from "@/components/ui/button";
import { 
  BookOpen, 
  Shirt, 
  Smartphone, 
  Home, 
  Car, 
  Palette, 
  Dumbbell, 
  Wrench,
  ArrowRight 
} from "lucide-react";

const categories = [
  { name: "Libros y Educación", icon: BookOpen, count: "2,340" },
  { name: "Ropa y Accesorios", icon: Shirt, count: "1,890" },
  { name: "Electrónicos", icon: Smartphone, count: "1,567" },
  { name: "Hogar y Jardín", icon: Home, count: "3,210" },
  { name: "Vehículos", icon: Car, count: "890" },
  { name: "Arte y Manualidades", icon: Palette, count: "1,234" },
  { name: "Deportes", icon: Dumbbell, count: "987" },
  { name: "Servicios", icon: Wrench, count: "2,100" },
];

const Categories = () => {
  return (
    <section className="py-16 bg-accent/30">
      <div className="container mx-auto px-4">
        <div className="text-center space-y-4 mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-foreground">
            Explora por categorías
          </h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Encuentra exactamente lo que buscas o descubre nuevas oportunidades de intercambio
          </p>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          {categories.map((category) => {
            const Icon = category.icon;
            return (
              <Button
                key={category.name}
                variant="outline"
                className="h-auto p-6 flex flex-col items-center gap-3 hover:bg-primary/5 hover:border-primary/20 transition-all duration-300 bg-card"
              >
                <Icon className="h-8 w-8 text-primary" />
                <div className="text-center">
                  <div className="font-semibold text-sm text-card-foreground">
                    {category.name}
                  </div>
                  <div className="text-xs text-muted-foreground">
                    {category.count} items
                  </div>
                </div>
              </Button>
            );
          })}
        </div>

        <div className="text-center">
          <Button variant="outline" size="lg">
            Ver todas las categorías
            <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
        </div>
      </div>
    </section>
  );
};

export default Categories;