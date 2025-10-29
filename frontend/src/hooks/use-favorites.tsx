import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode
} from "react";

const STORAGE_KEY = "tq-favorites";

export interface FavoriteItem {
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

interface FavoritesContextValue {
  favorites: FavoriteItem[];
  isLoading: boolean;
  isFavorite: (productId: string) => boolean;
  addFavorite: (item: FavoriteItem) => Promise<void>;
  removeFavorite: (productId: string) => Promise<void>;
  toggleFavorite: (item: FavoriteItem) => Promise<boolean>;
}

const FavoritesContext = createContext<FavoritesContextValue | undefined>(undefined);

const getStoredFavorites = (): FavoriteItem[] => {
  if (typeof window === "undefined") {
    return [];
  }

  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);

    if (!raw) {
      return [];
    }

    const parsed = JSON.parse(raw) as FavoriteItem[];

    if (!Array.isArray(parsed)) {
      return [];
    }

    return parsed;
  } catch (error) {
    console.warn("No se pudieron leer los favoritos almacenados", error);
    return [];
  }
};

const persistFavorites = (favorites: FavoriteItem[]) => {
  if (typeof window === "undefined") {
    return;
  }

  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(favorites));
  } catch (error) {
    console.warn("No se pudieron guardar los favoritos", error);
  }
};

export const FavoritesProvider = ({ children }: { children: ReactNode }) => {
  const [favorites, setFavorites] = useState<FavoriteItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const hasInitialized = useRef(false);

  useEffect(() => {
    const fetchFavorites = async () => {
      try {
        const response = await fetch("/api/favorites", { credentials: "include" });

        if (!response.ok) {
          throw new Error("Respuesta inválida del servidor");
        }

        const data = (await response.json()) as FavoriteItem[];

        if (Array.isArray(data)) {
          setFavorites(data);
          persistFavorites(data);
          return;
        }

        throw new Error("Formato de favoritos inválido");
      } catch (error) {
        console.warn("No se pudieron obtener los favoritos desde la API, usando almacenamiento local", error);
        const storedFavorites = getStoredFavorites();
        setFavorites(storedFavorites);
      } finally {
        hasInitialized.current = true;
        setIsLoading(false);
      }
    };

    void fetchFavorites();
  }, []);

  useEffect(() => {
    if (!hasInitialized.current) {
      return;
    }

    persistFavorites(favorites);
  }, [favorites]);

  const syncAddFavorite = useCallback(async (item: FavoriteItem) => {
    try {
      const response = await fetch("/api/favorites", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify({ productId: item.id })
      });

      if (!response.ok) {
        throw new Error("Error al guardar el favorito en el servidor");
      }
    } catch (error) {
      console.warn("Error al sincronizar favorito agregado", error);
    }
  }, []);

  const syncRemoveFavorite = useCallback(async (productId: string) => {
    try {
      const response = await fetch(`/api/favorites/${productId}`, {
        method: "DELETE",
        credentials: "include"
      });

      if (!response.ok) {
        throw new Error("Error al eliminar el favorito en el servidor");
      }
    } catch (error) {
      console.warn("Error al sincronizar favorito eliminado", error);
    }
  }, []);

  const addFavorite = useCallback(
    async (item: FavoriteItem) => {
      setFavorites((previous) => {
        if (previous.some((favorite) => favorite.id === item.id)) {
          return previous;
        }

        return [...previous, item];
      });

      await syncAddFavorite(item);
    },
    [syncAddFavorite]
  );

  const removeFavorite = useCallback(
    async (productId: string) => {
      setFavorites((previous) => previous.filter((favorite) => favorite.id !== productId));
      await syncRemoveFavorite(productId);
    },
    [syncRemoveFavorite]
  );

  const toggleFavorite = useCallback(
    async (item: FavoriteItem) => {
      let added = false;

      setFavorites((previous) => {
        const exists = previous.some((favorite) => favorite.id === item.id);
        added = !exists;

        if (exists) {
          return previous.filter((favorite) => favorite.id !== item.id);
        }

        return [...previous, item];
      });

      if (added) {
        await syncAddFavorite(item);
      } else {
        await syncRemoveFavorite(item.id);
      }

      return added;
    },
    [syncAddFavorite, syncRemoveFavorite]
  );

  const isFavorite = useCallback(
    (productId: string) => favorites.some((favorite) => favorite.id === productId),
    [favorites]
  );

  const value = useMemo(
    () => ({ favorites, isLoading, isFavorite, addFavorite, removeFavorite, toggleFavorite }),
    [favorites, isLoading, isFavorite, addFavorite, removeFavorite, toggleFavorite]
  );

  return <FavoritesContext.Provider value={value}>{children}</FavoritesContext.Provider>;
};

export const useFavorites = () => {
  const context = useContext(FavoritesContext);

  if (context === undefined) {
    throw new Error("useFavorites debe utilizarse dentro de un FavoritesProvider");
  }

  return context;
};
