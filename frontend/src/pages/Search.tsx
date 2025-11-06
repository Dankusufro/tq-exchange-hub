import { useEffect, useMemo } from "react";
import { useSearchParams } from "react-router-dom";

import Header from "@/components/Header";
import Footer from "@/components/Footer";
import SearchBar from "@/components/SearchBar";
import ProductCard from "@/components/ProductCard";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { useSearch } from "@/hooks/use-search";
import type { CategorySummary } from "@/hooks/use-categories";
import heroImage from "@/assets/hero-barter.jpg";

const ITEMS_PER_PAGE = 9;
const MIN_QUERY_LENGTH = 2;

const Search = () => {
  const [params, setParams] = useSearchParams();
  const query = params.get("q") ?? "";
  const categoryId = params.get("categoryId");
  const categoryName = params.get("categoryName") ?? undefined;
  const highlight = params.get("highlight");
  const page = Math.max(Number.parseInt(params.get("page") ?? "0", 10) || 0, 0);

  const shouldFetch = query.trim().length >= MIN_QUERY_LENGTH || Boolean(categoryId);

  const { data, isLoading, isError, error, isFetching } = useSearch({
    query,
    categoryId: categoryId ?? undefined,
    page,
    size: ITEMS_PER_PAGE,
    allowEmptyQuery: true,
    enabled: shouldFetch,
    minQueryLength: MIN_QUERY_LENGTH,
  });

  useEffect(() => {
    if (!highlight || !data?.items.length) {
      return;
    }

    const element = document.getElementById(`search-item-${highlight}`);
    if (!element) {
      return;
    }

    element.classList.add("ring-2", "ring-primary", "ring-offset-2", "ring-offset-background");
    element.scrollIntoView({ behavior: "smooth", block: "center" });

    const timeout = window.setTimeout(() => {
      element.classList.remove("ring-2", "ring-primary", "ring-offset-2", "ring-offset-background");
      const next = new URLSearchParams(params);
      next.delete("highlight");
      setParams(next, { replace: true });
    }, 1600);

    return () => window.clearTimeout(timeout);
  }, [data?.items, highlight, params, setParams]);

  const totalItems = data?.totalItems ?? 0;
  const totalPages = data?.totalPages ?? 0;
  const categories = data?.categories ?? [];

  const handlePageChange = (nextPage: number) => {
    const next = new URLSearchParams(params);
    next.set("page", Math.max(nextPage, 0).toString());
    next.delete("highlight");
    setParams(next);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleCategoryFilter = (category: CategorySummary) => {
    const next = new URLSearchParams();
    next.set("q", category.name);
    next.set("categoryId", category.id);
    next.set("categoryName", category.name);
    next.set("page", "0");
    setParams(next);
  };

  const handleClearCategory = () => {
    const next = new URLSearchParams(params);
    next.delete("categoryId");
    next.delete("categoryName");
    next.set("page", "0");
    setParams(next);
  };

  const paginationPages = useMemo(() => {
    if (totalPages <= 1) {
      return [];
    }

    return Array.from({ length: totalPages }, (_, index) => index);
  }, [totalPages]);

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        <section className="border-b bg-muted/40">
          <div className="container mx-auto px-4 py-8 space-y-6">
            <div className="space-y-2">
              <h1 className="text-3xl font-bold text-foreground">Resultados de búsqueda</h1>
              <p className="text-muted-foreground">
                Explora productos y servicios publicados por la comunidad de TruequePlus.
              </p>
            </div>

            <SearchBar
              className="max-w-2xl"
              placeholder="Buscar productos o servicios..."
              defaultValue={query}
            />

            <div className="flex flex-wrap gap-2">
              {categories.map((category) => (
                <Button
                  key={category.id}
                  variant={categoryId === category.id ? "default" : "outline"}
                  className="h-9 rounded-full"
                  onClick={() => handleCategoryFilter(category)}
                >
                  {category.name}
                </Button>
              ))}

              {categoryId && (
                <Button variant="ghost" className="h-9" onClick={handleClearCategory}>
                  Quitar filtro
                </Button>
              )}
            </div>

            {categoryId && (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <span>Filtrando por categoría:</span>
                <Badge variant="secondary">{categoryName ?? "Categoría seleccionada"}</Badge>
              </div>
            )}
          </div>
        </section>

        <section className="py-12">
          <div className="container mx-auto px-4 space-y-8">
            {!shouldFetch && (
              <div className="rounded-lg border border-dashed bg-card/40 p-12 text-center text-muted-foreground">
                Ingresa al menos {MIN_QUERY_LENGTH} caracteres para realizar una búsqueda.
              </div>
            )}

            {shouldFetch && isLoading && (
              <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
                {Array.from({ length: ITEMS_PER_PAGE }).map((_, index) => (
                  <div
                    key={index}
                    className="h-full rounded-xl border border-dashed border-border/60 bg-card/50 p-6"
                  >
                    <div className="h-full animate-pulse space-y-4">
                      <div className="h-40 rounded-lg bg-muted" />
                      <div className="h-4 rounded bg-muted/80" />
                      <div className="h-4 w-2/3 rounded bg-muted/60" />
                    </div>
                  </div>
                ))}
              </div>
            )}

            {shouldFetch && isError && (
              <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-8 text-center text-destructive">
                {error instanceof Error
                  ? error.message
                  : "No se pudieron cargar los resultados. Inténtalo nuevamente."}
              </div>
            )}

            {shouldFetch && !isLoading && !isError && data && (
              <>
                {data.items.length > 0 ? (
                  <>
                    <div className="flex flex-col gap-2 text-sm text-muted-foreground md:flex-row md:items-center md:justify-between">
                      <span>
                        Mostrando {page * ITEMS_PER_PAGE + 1} -
                        {Math.min((page + 1) * ITEMS_PER_PAGE, totalItems)} de {totalItems} resultados
                      </span>
                      {isFetching && <span className="italic">Actualizando resultados...</span>}
                    </div>

                    <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
                      {data.items.map((item) => (
                        <div key={item.id} id={`search-item-${item.id}`} className="transition-shadow">
                          <ProductCard
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
                        </div>
                      ))}
                    </div>

                    {totalPages > 1 && (
                      <Pagination className="pt-4">
                        <PaginationContent>
                          <PaginationItem>
                            <PaginationPrevious
                              href="#"
                              className={page <= 0 ? "pointer-events-none opacity-50" : undefined}
                              onClick={(event) => {
                                event.preventDefault();
                                if (page > 0) {
                                  handlePageChange(page - 1);
                                }
                              }}
                            />
                          </PaginationItem>

                          {paginationPages.map((pageNumber) => (
                            <PaginationItem key={pageNumber}>
                              <PaginationLink
                                href="#"
                                isActive={pageNumber === page}
                                onClick={(event) => {
                                  event.preventDefault();
                                  handlePageChange(pageNumber);
                                }}
                              >
                                {pageNumber + 1}
                              </PaginationLink>
                            </PaginationItem>
                          ))}

                          <PaginationItem>
                            <PaginationNext
                              href="#"
                              className={page >= totalPages - 1 ? "pointer-events-none opacity-50" : undefined}
                              onClick={(event) => {
                                event.preventDefault();
                                if (page < totalPages - 1) {
                                  handlePageChange(page + 1);
                                }
                              }}
                            />
                          </PaginationItem>
                        </PaginationContent>
                      </Pagination>
                    )}
                  </>
                ) : (
                  <div className="rounded-lg border border-dashed bg-card/40 p-12 text-center text-muted-foreground">
                    No encontramos publicaciones que coincidan con tu búsqueda.
                  </div>
                )}
              </>
            )}
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default Search;
