import Header from "@/components/Header";
import Footer from "@/components/Footer";
import ProductCard from "@/components/ProductCard";
import { useFavorites } from "@/hooks/use-favorites";
import { Button } from "@/components/ui/button";
import { HeartOff } from "lucide-react";
import { Link } from "react-router-dom";

const Favorites = () => {
  const { favorites, isLoading } = useFavorites();

  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1 bg-muted/30 py-12">
        <div className="container mx-auto px-4 space-y-8">
          <div className="space-y-2 text-center">
            <h1 className="text-3xl font-bold text-foreground">Mis favoritos</h1>
            <p className="text-muted-foreground">
              Guarda aquí los productos que más te interesan para revisarlos más tarde.
            </p>
          </div>

          {isLoading ? (
            <div className="flex flex-col items-center justify-center gap-3 py-24 text-muted-foreground">
              <div className="animate-spin rounded-full h-12 w-12 border-2 border-muted-foreground/30 border-t-primary" />
              <p>Cargando tus favoritos...</p>
            </div>
          ) : favorites.length > 0 ? (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {favorites.map((favorite) => (
                <ProductCard key={favorite.id} {...favorite} />
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center gap-4 rounded-lg border border-dashed border-muted-foreground/40 bg-background/60 py-16">
              <div className="rounded-full bg-muted p-4">
                <HeartOff className="h-8 w-8 text-muted-foreground" />
              </div>
              <div className="space-y-1 text-center">
                <h2 className="text-xl font-semibold text-foreground">Tu lista está vacía</h2>
                <p className="text-muted-foreground max-w-md">
                  Aún no has agregado productos a tus favoritos. Explora el catálogo y guarda los que más te gusten.
                </p>
              </div>
              <Button asChild>
                <Link to="/">Descubrir productos</Link>
              </Button>
            </div>
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default Favorites;
