import { ArrowRight } from "lucide-react";

import ProductCard from "./ProductCard";
import { Button } from "@/components/ui/button";
import heroImage from "@/assets/hero-barter.jpg";
import { useHighlightedItems } from "@/hooks/use-highlighted-items";

const FeaturedProducts = () => {
  const { data, isLoading, isError, error } = useHighlightedItems({ size: 6 });
  const products = data?.items ?? [];

  const renderContent = () => {
    if (isLoading) {
      return (
        <div className="text-center text-muted-foreground py-10">
          Cargando intercambios destacados...
        </div>
      );
    }

    if (isError) {
      return (
        <div className="text-center text-destructive py-10">
          {error instanceof Error ? error.message : "No se pudieron cargar los destacados."}
        </div>
      );
    }

    if (products.length === 0) {
      return (
        <div className="text-center text-muted-foreground py-10">
          Aún no hay intercambios destacados. ¡Sé el primero en publicar!
        </div>
      );
    }

    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
        {products.map((item) => (
          <ProductCard
            key={item.id}
            id={item.id}
            ownerId={item.owner?.id ?? ""}
            title={item.title}
            description={item.description}
            image={item.mainImageUrl ?? heroImage}
            category={item.categoryName}
            condition={item.condition}
            location={item.location ?? "Ubicación no disponible"}
            userRating={item.rating ?? item.owner?.rating ?? 0}
            userName={item.owner?.displayName ?? "Anónimo"}
            lookingFor={item.wishlist ?? []}
          />
        ))}
      </div>
    );
  };

  return (
    <section className="py-16 bg-background">
      <div className="container mx-auto px-4">
        <div className="text-center space-y-4 mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-foreground">
            Intercambios destacados
          </h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Descubre los productos y servicios más populares de nuestra comunidad
          </p>
        </div>

        {renderContent()}

        <div className="text-center">
          <Button size="lg" className="px-8">
            Ver todos los productos
            <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
        </div>
      </div>
    </section>
  );
};

export default FeaturedProducts;
