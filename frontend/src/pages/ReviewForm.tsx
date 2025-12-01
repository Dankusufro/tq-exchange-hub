import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { z } from "zod";

import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/components/ui/use-toast";
import { apiClient } from "@/lib/api";
import { Loader2, ChevronLeft, Star } from "lucide-react";
import { useAuth } from "@/providers/AuthProvider";
import useTradeRequests from "@/hooks/use-trade-requests";
import NotFound from "@/pages/NotFound";

// ----------------------------------------------

const reviewSchema = z.object({
    rating: z.number().min(1, "Debes calificar al menos 1 estrella").max(5, "La calificación máxima es 5 estrellas"),
    comment: z.string().max(500, "El comentario no puede superar los 500 caracteres").optional(),
});

type ReviewFormValues = z.infer<typeof reviewSchema>;

// ----------------------------------------------

const ReviewForm = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const { toast } = useToast();
    const { tradeId, reviewedId } = useParams<{ tradeId: string; reviewedId: string }>();

    const form = useForm<ReviewFormValues>({
        resolver: zodResolver(reviewSchema),
        defaultValues: { rating: 5, comment: "" },
    });

    const { requests, isLoading } = useTradeRequests({ status: ["accepted"] });
    const trade = requests.find((t) => t.id === tradeId);

    // ----------------------------------------------
    // MUTATION
    // ----------------------------------------------

    const createReviewMutation = useMutation({
        mutationFn: async (values: ReviewFormValues) => {

            console.log("🔵 1. Entró al mutationFn");

            if (!tradeId || !reviewedId || !user?.id) {
                console.log("🔴 ERROR: faltan datos obligatorios");
                throw new Error("Faltan datos para crear la valoración");
            }

            console.log("🟣 2. Enviando POST al backend...");

            const response = await apiClient.post("/api/reviews", {
                tradeId,
                reviewedId,
                reviewerId: user.id,
                rating: values.rating,
                comment: values.comment,
            });

            console.log("🟢 3. POST recibido:", response);

            console.log("🟠 4. Consultando reviews del usuario revisado...");

            try {
                const reviewsResponse = await apiClient.get(`/api/reviews/profile/${reviewedId}`);
                console.log("🟡 5. Reviews actuales del usuario:", reviewsResponse);
            } catch (error) {
                console.error("🔴 ERROR obteniendo reviews del usuario:", error);
            }

            return response;
        },

        onSuccess: () => {
            toast({
                title: "Valoración enviada",
                description: "Gracias por calificar tu experiencia de trueque.",
            });
            navigate(-1);
        },

        onError: (error: unknown) => {
            toast({
                title: "Error",
                description: error instanceof Error ? error.message : "No se pudo enviar tu valoración. Intenta nuevamente.",
                variant: "destructive",
            });
        },
    });

    // ----------------------------------------------

    if (isLoading) return <div>Cargando...</div>;
    if (!trade || !user) return <NotFound />;

    const onSubmit = (values: ReviewFormValues) => createReviewMutation.mutate(values);
    const isSubmitting = createReviewMutation.isPending;

    // ----------------------------------------------

    return (
        <div className="min-h-screen flex items-center justify-center bg-muted/40 px-4">
            <div className="w-full max-w-3xl">
                <Card>
                    <CardHeader className="space-y-2">
                        <Button type="button" variant="ghost" size="sm" className="-ml-2" onClick={() => navigate(-1)}>
                            <ChevronLeft className="mr-1 h-4 w-4" /> Volver
                        </Button>
                        <CardTitle>¿Cómo valoras tu experiencia?</CardTitle>
                        <CardDescription>Califica tu experiencia con el trueque y agrega un comentario opcional.</CardDescription>
                    </CardHeader>

                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            <CardContent className="space-y-6">
                                <FormField
                                    control={form.control}
                                    name="rating"
                                    render={({ field }) => (
                                        <FormItem className="flex flex-col items-center">
                                            <FormLabel>Calificación</FormLabel>
                                            <FormControl>
                                                <div className="flex gap-1">
                                                    {[1, 2, 3, 4, 5].map((star) => (
                                                        <Star
                                                            key={star}
                                                            className={`h-8 w-8 cursor-pointer ${field.value >= star ? "text-yellow-400" : "text-gray-300"}`}
                                                            onClick={() => field.onChange(star)}
                                                        />
                                                    ))}
                                                </div>
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                                <FormField
                                    control={form.control}
                                    name="comment"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Comentario (opcional)</FormLabel>
                                            <FormControl>
                                                <Textarea placeholder="Escribe tu comentario..." className="min-h-[120px]" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </CardContent>

                            <CardFooter className="flex items-center justify-center border-t bg-muted/50 px-6 py-4">
                                <Button type="submit" className="bg-green-500 text-white hover:bg-green-600" disabled={isSubmitting}>
                                    {isSubmitting ? (
                                        <>
                                            <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Enviando...
                                        </>
                                    ) : (
                                        "Enviar valoración"
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

export default ReviewForm;
