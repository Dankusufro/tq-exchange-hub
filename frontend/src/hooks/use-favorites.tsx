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
import { apiClient, type ApiError } from "@/lib/api";

const STORAGE_KEY = "tq-favorites";

export interface FavoriteItem {
  id: string;
  ownerId: string;
  title: string;
  description: string;
  image: string | null;
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
  error: string | null;
  isFavorite: (productId: string) => boolean;
  addFavorite: (item: FavoriteItem) => Promise<void>;
  removeFavorite: (productId: string) => Promise<void>;
  toggleFavorite: (item: FavoriteItem) => Promise<boolean>;
  refresh: () => Promise<void>;
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

const clearStoredFavorites = () => {
  if (typeof window === "undefined") {
    return;
  }

  try {
    window.localStorage.removeItem(STORAGE_KEY);
  } catch (error) {
    console.warn("No se pudieron limpiar los favoritos almacenados", error);
  }
};

const isApiError = (value: unknown): value is ApiError => value instanceof Error && "status" in value;

const resolveErrorMessage = (error: unknown, fallback: string) => {
  if (isApiError(error) && error.status === 401) {
    return "Debes iniciar sesión para sincronizar tus favoritos.";
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallback;
};

export const FavoritesProvider = ({ children }: { children: ReactNode }) => {
  const [favorites, setFavorites] = useState<FavoriteItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const hasInitialized = useRef(false);

  const fetchFavorites = useCallback(async () => {
    setIsLoading(true);

    try {
      const data = await apiClient.get<FavoriteItem[]>("/api/favorites");

      if (!Array.isArray(data)) {
        throw new Error("Formato de favoritos inválido");
      }

      setFavorites(data);
      setError(null);
    } catch (caughtError) {
      console.warn("No se pudieron obtener los favoritos desde la API", caughtError);

      if (isApiError(caughtError) && caughtError.status === 401) {
        clearStoredFavorites();
        setFavorites([]);
        setError(null);
      } else {
        const message = resolveErrorMessage(caughtError, "No se pudieron cargar tus favoritos.");
        const storedFavorites = getStoredFavorites();
        setFavorites(storedFavorites);
        setError(storedFavorites.length === 0 ? null : message);
      }
    } finally {
      hasInitialized.current = true;
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchFavorites();
  }, [fetchFavorites]);

  useEffect(() => {
    if (!hasInitialized.current) {
      return;
    }

    persistFavorites(favorites);
  }, [favorites]);

  const addFavorite = useCallback(
    async (item: FavoriteItem) => {
      try {
        const response = await apiClient.post<FavoriteItem>("/api/favorites", { itemId: item.id });
        const favorite = response ?? item;

        setFavorites((previous) => {
          if (previous.some((existing) => existing.id === favorite.id)) {
            return previous;
          }

          return [...previous, favorite];
        });
        setError(null);
      } catch (caughtError) {
        const message = resolveErrorMessage(caughtError, "No se pudo guardar el favorito.");
        setError(message);
        throw caughtError instanceof Error ? caughtError : new Error(message);
      }
    },
    []
  );

  const removeFavorite = useCallback(
    async (productId: string) => {
      try {
        await apiClient.delete(`/api/favorites/${productId}`);
        setFavorites((previous) => previous.filter((favorite) => favorite.id !== productId));
        setError(null);
      } catch (caughtError) {
        const message = resolveErrorMessage(caughtError, "No se pudo eliminar el favorito.");
        setError(message);
        throw caughtError instanceof Error ? caughtError : new Error(message);
      }
    },
    []
  );

  const toggleFavorite = useCallback(
    async (item: FavoriteItem) => {
      const exists = favorites.some((favorite) => favorite.id === item.id);

      if (exists) {
        await removeFavorite(item.id);
        return false;
      }

      await addFavorite(item);
      return true;
    },
    [favorites, addFavorite, removeFavorite]
  );

  const isFavorite = useCallback(
    (productId: string) => favorites.some((favorite) => favorite.id === productId),
    [favorites]
  );

  const refresh = useCallback(async () => {
    await fetchFavorites();
  }, [fetchFavorites]);

  const value = useMemo(
    () => ({
      favorites,
      isLoading,
      error,
      isFavorite,
      addFavorite,
      removeFavorite,
      toggleFavorite,
      refresh
    }),
    [favorites, isLoading, error, isFavorite, addFavorite, removeFavorite, toggleFavorite, refresh]
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
