import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ArrowRight, Loader2, MapPin, Search } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";
import { getCategoryIcon } from "@/lib/category-icons";
import type { CategorySummary } from "@/hooks/use-categories";
import type { ItemSummary } from "@/hooks/use-highlighted-items";
import { useSearch } from "@/hooks/use-search";

interface SearchBarProps {
  placeholder?: string;
  className?: string;
  inputClassName?: string;
  iconClassName?: string;
  defaultValue?: string;
  minQueryLength?: number;
  actionSlot?: ReactNode;
  syncWithUrl?: boolean;
}

const formatCount = (value: number) => new Intl.NumberFormat("es-MX").format(value);

const SearchBar = ({
  placeholder = "Buscar productos o servicios...",
  className,
  inputClassName,
  iconClassName,
  defaultValue = "",
  minQueryLength = 2,
  actionSlot,
  syncWithUrl = false,
}: SearchBarProps) => {
  const navigate = useNavigate();
  const location = useLocation();
  const formRef = useRef<HTMLFormElement>(null);
  const [query, setQuery] = useState(defaultValue);
  const [isFocused, setIsFocused] = useState(false);

  const trimmedQuery = query.trim();
  const [debouncedQuery, setDebouncedQuery] = useState(trimmedQuery);

  useEffect(() => {
    const nextValue = (() => {
      if (!syncWithUrl) {
        return defaultValue;
      }

      try {
        const params = new URLSearchParams(location.search);
        return params.get("q") ?? defaultValue;
      } catch (error) {
        console.error("Failed to read search params", error);
        return defaultValue;
      }
    })();

    setQuery(nextValue);
    setDebouncedQuery(nextValue.trim());
  }, [defaultValue, location.search, syncWithUrl]);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedQuery(trimmedQuery);
    }, 250);

    return () => {
      clearTimeout(handler);
    };
  }, [trimmedQuery]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (!formRef.current) {
        return;
      }

      if (!formRef.current.contains(event.target as Node)) {
        setIsFocused(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const shouldFetch = debouncedQuery.length >= minQueryLength;
  const { data, isFetching, isError, error } = useSearch({
    query: debouncedQuery,
    page: 0,
    size: 5,
    enabled: shouldFetch,
  });

  const showPanel = isFocused && trimmedQuery.length > 0;

  const handleNavigate = useCallback(
    (nextQuery: string, extraParams?: Record<string, string | undefined>) => {
      const normalized = nextQuery.trim();
      const params = new URLSearchParams();

      if (normalized) {
        params.set("q", normalized);
      }

      params.set("page", "0");

      if (extraParams) {
        Object.entries(extraParams).forEach(([key, value]) => {
          if (value) {
            params.set(key, value);
          }
        });
      }

      navigate(`/search?${params.toString()}`);
      setIsFocused(false);
    },
    [navigate],
  );

  const handleSubmit: React.FormEventHandler<HTMLFormElement> = (event) => {
    event.preventDefault();
    if (trimmedQuery.length < minQueryLength) {
      return;
    }

    handleNavigate(trimmedQuery);
  };

  const handleCategorySelect = useCallback(
    (category: CategorySummary) => {
      setQuery(category.name);
      handleNavigate(category.name, {
        categoryId: category.id,
        categoryName: category.name,
      });
    },
    [handleNavigate],
  );

  const handleItemSelect = useCallback(
    (item: ItemSummary) => {
      handleNavigate(debouncedQuery, {
        highlight: item.id,
      });
    },
    [debouncedQuery, handleNavigate],
  );

  const panelContent = useMemo(() => {
    if (trimmedQuery.length < minQueryLength) {
      return (
        <div className="px-4 py-6 text-sm text-muted-foreground">
          Escribe al menos {minQueryLength} caracteres para buscar.
        </div>
      );
    }

    if (isFetching) {
      return (
        <div className="flex items-center gap-2 px-4 py-6 text-sm text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" />
          Buscando "{debouncedQuery}"...
        </div>
      );
    }

    if (isError) {
      const message =
        error instanceof Error
          ? error.message
          : "Ocurrió un error al buscar. Inténtalo nuevamente.";
      return <div className="px-4 py-6 text-sm text-destructive">{message}</div>;
    }

    if (!data || (data.categories.length === 0 && data.items.length === 0)) {
      return (
        <div className="px-4 py-6 text-sm text-muted-foreground">
          No encontramos resultados para "{debouncedQuery}".
        </div>
      );
    }

    return (
      <div className="max-h-96 overflow-y-auto">
        {data.categories.length > 0 && (
          <div className="border-b border-border/40">
            <p className="px-4 pt-3 pb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              Categorías
            </p>
            <div className="space-y-1 px-2 pb-3">
              {data.categories.map((category) => {
                const Icon = getCategoryIcon(category.icon);
                return (
                  <button
                    key={category.id}
                    type="button"
                    className="flex w-full items-center gap-3 rounded-md px-3 py-2 text-left transition-colors hover:bg-muted"
                    onMouseDown={(event) => event.preventDefault()}
                    onClick={() => handleCategorySelect(category)}
                  >
                    <span className="flex h-9 w-9 items-center justify-center rounded-md bg-primary/10 text-primary">
                      <Icon className="h-4 w-4" />
                    </span>
                    <span className="flex-1">
                      <span className="block text-sm font-medium text-foreground">{category.name}</span>
                      {category.description && (
                        <span className="block text-xs text-muted-foreground line-clamp-1">
                          {category.description}
                        </span>
                      )}
                    </span>
                    <Badge variant="secondary" className="shrink-0">
                      {formatCount(category.itemsCount)}
                    </Badge>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {data.items.length > 0 && (
          <div className="space-y-1 p-2">
            <p className="px-2 pt-1 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              Productos y servicios
            </p>
            {data.items.map((item) => (
              <button
                key={item.id}
                type="button"
                className="flex w-full items-center gap-3 rounded-md px-3 py-2 text-left transition-colors hover:bg-muted"
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => handleItemSelect(item)}
              >
                <span className="h-12 w-12 overflow-hidden rounded-md bg-muted">
                  <img
                    src={item.mainImageUrl ?? "https://placehold.co/120x120?text=TQ"}
                    alt={item.title}
                    className="h-full w-full object-cover"
                    loading="lazy"
                  />
                </span>
                <span className="flex-1">
                  <span className="block text-sm font-medium text-foreground line-clamp-1">
                    {item.title}
                  </span>
                  <span className="block text-xs text-muted-foreground line-clamp-1">
                    {item.categoryName}
                  </span>
                  {item.location && (
                    <span className="mt-1 flex items-center gap-1 text-xs text-muted-foreground">
                      <MapPin className="h-3 w-3" />
                      {item.location}
                    </span>
                  )}
                </span>
                <ArrowRight className="h-4 w-4 text-muted-foreground" />
              </button>
            ))}
          </div>
        )}

        <div className="border-t border-border/40 p-2">
          <Button
            type="button"
            variant="ghost"
            className="w-full justify-center"
            onMouseDown={(event) => event.preventDefault()}
            onClick={() => handleNavigate(trimmedQuery)}
          >
            Ver todos los resultados
          </Button>
        </div>
      </div>
    );
  }, [
    data,
    debouncedQuery,
    error,
    handleCategorySelect,
    handleItemSelect,
    handleNavigate,
    isError,
    isFetching,
    minQueryLength,
    trimmedQuery,
  ]);

  return (
    <form
      ref={formRef}
      className={cn(
        "relative flex w-full flex-col gap-2 sm:flex-row sm:items-center",
        className,
      )}
      onSubmit={handleSubmit}
    >
      <div className="relative flex-1">
        <Search
          className={cn(
            "pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground",
            iconClassName,
          )}
        />
        <Input
          value={query}
          placeholder={placeholder}
          onChange={(event) => setQuery(event.target.value)}
          onFocus={() => setIsFocused(true)}
          className={cn("h-10 w-full pl-9 pr-9", inputClassName)}
        />
        {isFetching && (
          <Loader2 className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 animate-spin text-muted-foreground" />
        )}

        {showPanel && (
          <div className="absolute left-0 right-0 top-full z-50 mt-2 overflow-hidden rounded-lg border bg-popover text-popover-foreground shadow-lg">
            {panelContent}
          </div>
        )}
      </div>

      {actionSlot}
    </form>
  );
};

export default SearchBar;
