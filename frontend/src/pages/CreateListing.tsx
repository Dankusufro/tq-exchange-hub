import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { z } from "zod";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/components/ui/use-toast";
import { useCategories } from "@/hooks/use-categories";
import { apiClient } from "@/lib/api";
import { useAuth } from "@/providers/AuthProvider";
import { AlertCircle, ChevronLeft, Loader2 } from "lucide-react";

const conditionOptions = [
  { value: "new", label: "Nuevo" },
  { value: "like_new", label: "Como nuevo" },
  { value: "good", label: "Buen estado" },
  { value: "used", label: "Usado" },
  { value: "needs_repair", label: "Necesita reparación" },
] as const;

const createListingSchema = z.object({
  title: z
    .string()
    .min(1, "Agrega un título para tu publicación."),
  description: z
    .string()
    .min(10, "Describe tu oferta con al menos 10 caracteres."),
  categoryId: z.string().min(1, "Selecciona una categoría."),
  condition: z.string().min(1, "Selecciona la condición de tu artículo o servicio."),
  estimatedValue: z
    .string()
    .optional()
    .transform((value) => value?.trim() ?? "")
    .refine(
      (value) => value === "" || /^\d+(\.\d{1,2})?$/.test(value),
      "Ingresa un valor válido o deja el campo vacío.",
    ),
  available: z.boolean(),
  service: z.boolean(),
  location: z.string().optional(),
  images: z.string().optional(),
  wishlist: z.string().optional(),
});

type CreateListingFormValues = z.infer<typeof createListingSchema>;

type CreateItemRequest = {
  ownerId: string;
  categoryId: string;
  title: string;
  description: string;
  condition: string;
  estimatedValue?: number | null;
  available?: boolean;
  service?: boolean;
  location?: string | null;
  images?: string[];
  wishlist?: string[];
};

type ItemResponse = {
  id: string;
  title: string;
};

const parseMultilineList = (value?: string) =>
  value
    ?.split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean) ?? [];

const CreateListing = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();
  const categoriesQuery = useCategories();

  const form = useForm<CreateListingFormValues>({
    resolver: zodResolver(createListingSchema),
    defaultValues: {
      title: "",
      description: "",
      categoryId: "",
      condition: "",
      estimatedValue: "",
      available: true,
      service: false,
      location: "",
      images: "",
      wishlist: "",
    },
  });

  const categories = useMemo(() => categoriesQuery.data ?? [], [categoriesQuery.data]);

  const createListingMutation = useMutation({
    mutationFn: async (values: CreateListingFormValues) => {
      if (!user?.id) {
        throw new Error("Debes iniciar sesión para publicar.");
      }

      const payload: CreateItemRequest = {
        ownerId: user.id,
        categoryId: values.categoryId,
        title: values.title,
        description: values.description,
        condition: values.condition,
        available: values.available,
        service: values.service,
      };

      if (values.estimatedValue) {
        payload.estimatedValue = Number(values.estimatedValue);
      }

      const location = values.location?.trim();
      if (location) {
        payload.location = location;
      }

      const images = parseMultilineList(values.images);
      if (images.length > 0) {
        payload.images = images;
      }

      const wishlist = parseMultilineList(values.wishlist);
      if (wishlist.length > 0) {
        payload.wishlist = wishlist;
      }

      return apiClient.post<ItemResponse>("/api/items", payload);
    },
    onSuccess: (item) => {
      toast({
        title: "¡Publicación creada!",
        description: "Tu oferta ya está visible para la comunidad.",
      });
      form.reset({
        title: "",
        description: "",
        categoryId: "",
        condition: "",
        estimatedValue: "",
        available: true,
        service: false,
        location: "",
        images: "",
        wishlist: "",
      });
      navigate("/", { replace: true, state: { publishedItemId: item.id } });
    },
    onError: (error: unknown) => {
      const description =
        error instanceof Error
          ? error.message
          : "No pudimos publicar tu oferta. Intenta nuevamente más tarde.";
      toast({
        title: "Error al publicar",
        description,
        variant: "destructive",
      });
    },
  });

  const onSubmit = (values: CreateListingFormValues) => {
    createListingMutation.mutate(values);
  };

  const isSubmitting = createListingMutation.isPending;
  const isLoadingCategories = categoriesQuery.isLoading;
  const categoryPlaceholder = isLoadingCategories
    ? "Cargando categorías..."
    : categories.length > 0
      ? "Selecciona"
      : "Sin categorías disponibles";

  return (
    <div className="min-h-screen bg-muted/40 py-10">
      <div className="container max-w-3xl px-4">
        <Card>
          <CardHeader className="space-y-4">
            <div>
              <Button
                type="button"
                variant="ghost"
                size="sm"
                className="-ml-2"
                onClick={() => navigate(-1)}
              >
                <ChevronLeft className="mr-1 h-4 w-4" /> Volver
              </Button>
            </div>
            <CardTitle>Crea una nueva publicación</CardTitle>
            <CardDescription>
              Comparte un producto o servicio para intercambiar con otros miembros de TruequePlus.
            </CardDescription>
          </CardHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <CardContent className="space-y-6">
                {categoriesQuery.isError && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>No pudimos cargar las categorías</AlertTitle>
                    <AlertDescription>
                      Revisa tu conexión e intenta recargar la página antes de volver a publicar.
                    </AlertDescription>
                  </Alert>
                )}
                <FormField
                  control={form.control}
                  name="title"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Título</FormLabel>
                      <FormControl>
                        <Input placeholder="Escribe un título llamativo" {...field} />
                      </FormControl>
                      <FormDescription>Describe en pocas palabras qué ofreces.</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="description"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Descripción</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="Detalla el estado, características y qué buscas recibir a cambio."
                          className="min-h-[150px]"
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <div className="grid gap-6 md:grid-cols-2">
                  <FormField
                    control={form.control}
                    name="categoryId"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Categoría</FormLabel>
                        <FormControl>
                          <Select
                            onValueChange={field.onChange}
                            value={field.value}
                            disabled={isLoadingCategories || categories.length === 0}
                          >
                            <SelectTrigger>
                              <SelectValue placeholder={categoryPlaceholder} />
                            </SelectTrigger>
                            <SelectContent>
                              {categories.map((category) => (
                                <SelectItem key={category.id} value={category.id}>
                                  {category.name}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </FormControl>
                        <FormDescription>Organiza tu publicación para que otros la encuentren fácilmente.</FormDescription>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="condition"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Condición</FormLabel>
                        <FormControl>
                          <Select onValueChange={field.onChange} value={field.value}>
                            <SelectTrigger>
                              <SelectValue placeholder="Selecciona" />
                            </SelectTrigger>
                            <SelectContent>
                              {conditionOptions.map((option) => (
                                <SelectItem key={option.value} value={option.value}>
                                  {option.label}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                <div className="grid gap-6 md:grid-cols-2">
                  <FormField
                    control={form.control}
                    name="estimatedValue"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Valor estimado</FormLabel>
                        <FormControl>
                          <Input placeholder="Ej. 1500.00" inputMode="decimal" {...field} />
                        </FormControl>
                        <FormDescription>
                          Ingresa un valor aproximado en tu moneda local para orientar a otros
                          usuarios.
                        </FormDescription>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="location"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Ubicación</FormLabel>
                        <FormControl>
                          <Input placeholder="Ciudad o zona de intercambio" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                <FormField
                  control={form.control}
                  name="images"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Imágenes</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="Pega URLs de imágenes, una por línea"
                          className="min-h-[120px]"
                          {...field}
                        />
                      </FormControl>
                      <FormDescription>Comparte fotos hospedadas en línea para destacar tu oferta.</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="wishlist"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Lista de deseos</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="Describe qué artículos o servicios aceptas como intercambio. Uno por línea."
                          className="min-h-[120px]"
                          {...field}
                        />
                      </FormControl>
                      <FormDescription>
                        Esto ayuda a otros usuarios a saber qué puedes aceptar como intercambio.
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <div className="grid gap-4">
                  <FormField
                    control={form.control}
                    name="available"
                    render={({ field }) => (
                      <FormItem className="flex items-center justify-between rounded-lg border p-4">
                        <div>
                          <FormLabel className="text-base">Disponible</FormLabel>
                          <FormDescription>
                            Desactívalo cuando ya no puedas aceptar nuevos intercambios.
                          </FormDescription>
                        </div>
                        <FormControl>
                          <Switch checked={field.value} onCheckedChange={field.onChange} />
                        </FormControl>
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="service"
                    render={({ field }) => (
                      <FormItem className="flex items-center justify-between rounded-lg border p-4">
                        <div>
                          <FormLabel className="text-base">Es un servicio</FormLabel>
                          <FormDescription>
                            Actívalo si ofreces tu tiempo o experiencia en lugar de un producto.
                          </FormDescription>
                        </div>
                        <FormControl>
                          <Switch checked={field.value} onCheckedChange={field.onChange} />
                        </FormControl>
                      </FormItem>
                    )}
                  />
                </div>
              </CardContent>
              <CardFooter className="flex items-center justify-end border-t bg-muted/50 px-6 py-4">
                <Button type="submit" disabled={isSubmitting || isLoadingCategories}>
                  {isSubmitting ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Publicando
                    </>
                  ) : (
                    "Publicar"
                  )}
                </Button>
              </CardFooter>
            </form>
          </Form>
        </Card>
      </div>
    </div>
  );
};

export default CreateListing;
