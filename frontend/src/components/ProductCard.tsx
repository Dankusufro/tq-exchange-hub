import { useCallback, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Heart, MessageCircle, Star } from "lucide-react";
import MessageModal from "./MessageModal";
import { useFavorites, type FavoriteItem } from "@/hooks/use-favorites";
import { useToast } from "@/components/ui/use-toast";
import { cn } from "@/lib/utils";

interface ProductCardProps {
  id: string;
  ownerId: string;
  title: string;
  description: string;
  image: string;
  category: string;
  condition: string;
  location: string;
  userRating: number;
  userName: string;
  lookingFor: string[];
}

const ProductCard = ({
  id,
  ownerId,
  title,
  description,
  image,
  category,
  condition,
  location, 
  userRating, 
  userName,
  lookingFor 
}: ProductCardProps) => {
  const [isMessageModalOpen, setIsMessageModalOpen] = useState(false);
  const { isFavorite, toggleFavorite } = useFavorites();
  const { toast } = useToast();

  const handleToggleFavorite = useCallback(async () => {
    const favoriteItem: FavoriteItem = {
      id,
      ownerId,
      title,
      description,
      image,
      category,
      condition,
      location,
      userRating,
      userName,
      lookingFor
    };

    const added = await toggleFavorite(favoriteItem);

    toast({
      title: added ? "Añadido a favoritos" : "Eliminado de favoritos",
      description: added
        ? "Podrás consultar este producto desde tu lista de favoritos."
        : "Ya no verás este producto en tu lista de favoritos.",
    });
  }, [toggleFavorite, id, ownerId, title, description, image, category, condition, location, userRating, userName, lookingFor, toast]);

  const favorite = isFavorite(id);
  return (
    <Card className="group hover:shadow-card-hover transition-all duration-300 hover:-translate-y-1 bg-gradient-card border-border/50">
      <CardContent className="p-0">
        <div className="relative overflow-hidden rounded-t-lg">
          <img 
            src={image} 
            alt={title}
            className="w-full h-48 object-cover group-hover:scale-105 transition-transform duration-300"
          />
          <div className="absolute top-3 left-3">
            <Badge variant="secondary" className="bg-white/90 text-foreground">
              {category}
            </Badge>
          </div>
          <Button
            variant="ghost"
            size="sm"
            className={cn(
              "absolute top-3 right-3 bg-white/90 text-foreground transition-colors",
              favorite && "text-rose-500 hover:text-rose-500"
            )}
            aria-label={favorite ? "Eliminar de favoritos" : "Añadir a favoritos"}
            aria-pressed={favorite}
            onClick={handleToggleFavorite}
          >
            <Heart className={cn("h-4 w-4", favorite && "fill-current")} />
          </Button>
        </div>
        
        <div className="p-4 space-y-3">
          <div className="space-y-1">
            <h3 className="font-semibold text-lg text-card-foreground line-clamp-1">
              {title}
            </h3>
            <p className="text-sm text-muted-foreground line-clamp-2">
              {description}
            </p>
          </div>

          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <span>Estado: {condition}</span>
            <span>{location}</span>
          </div>

          <div className="flex items-center gap-2">
            <div className="flex items-center gap-1">
              <Star className="h-3 w-3 fill-warning text-warning" />
              <span className="text-xs font-medium">{userRating}</span>
            </div>
            <span className="text-xs text-muted-foreground">por {userName}</span>
          </div>

          <div className="space-y-2">
            <p className="text-xs font-medium text-foreground">Busca intercambiar por:</p>
            <div className="flex flex-wrap gap-1">
              {lookingFor.slice(0, 2).map((item, index) => (
                <Badge key={index} variant="outline" className="text-xs px-2 py-0.5">
                  {item}
                </Badge>
              ))}
              {lookingFor.length > 2 && (
                <Badge variant="outline" className="text-xs px-2 py-0.5">
                  +{lookingFor.length - 2}
                </Badge>
              )}
            </div>
          </div>

          <div className="flex gap-2 pt-2">
            <Button 
              className="flex-1 h-9"
              onClick={() => setIsMessageModalOpen(true)}
            >
              Proponer intercambio
            </Button>
            <Button 
              variant="outline" 
              size="sm" 
              className="px-3"
              onClick={() => setIsMessageModalOpen(true)}
            >
              <MessageCircle className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </CardContent>
      
      <MessageModal
        isOpen={isMessageModalOpen}
        onClose={() => setIsMessageModalOpen(false)}
        productId={id}
        ownerId={ownerId}
        productTitle={title}
        userName={userName}
      />
    </Card>
  );
};

export default ProductCard;