import { ArrowRight } from "lucide-react";

import { Button } from "@/components/ui/button";
import { useCategories } from "@/hooks/use-categories";
import { getCategoryIcon } from "@/lib/category-icons";

const formatCount = (value: number) => new Intl.NumberFormat("es-MX").format(value);

const Categories = () => {
  const { data: categories, isLoading, isError, error } = useCategories();

  const renderContent = () => {
    if (isLoading) {
      return (
        <div className="col-span-full text-center text-muted-foreground py-10">
          Cargando categorías...
        </div>
      );
    }

    if (isError) {
      return (
        <div className="col-span-full text-center text-destructive py-10">
          {error instanceof Error ? error.message : "No se pudieron cargar las categorías."}
        </div>
      );
    }

    if (!categories || categories.length === 0) {
      return (
        <div className="col-span-full text-center text-muted-foreground py-10">
          Aún no hay categorías disponibles.
        </div>
      );
    }

    return categories.map((category) => {
      const Icon = getCategoryIcon(category.icon);
      return (
        <Button
          key={category.id}
          variant="outline"
          className="h-auto p-6 flex flex-col items-center gap-3 hover:bg-primary/5 hover:border-primary/20 transition-all duration-300 bg-card"
        >
          <Icon className="h-8 w-8 text-primary" />
          <div className="text-center">
            <div className="font-semibold text-sm text-card-foreground">{category.name}</div>
            <div className="text-xs text-muted-foreground">
              {formatCount(category.itemsCount)} items
            </div>
          </div>
        </Button>
      );
    });
  };

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

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">{renderContent()}</div>

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
