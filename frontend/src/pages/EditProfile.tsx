import { useNavigate } from "react-router-dom";
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
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/components/ui/use-toast";
import { apiClient } from "@/lib/api";
import { useAuth } from "@/providers/AuthProvider";
import { ChevronLeft, Loader2 } from "lucide-react";

// ---------------------------------------------
// VALIDACIÓN
// ---------------------------------------------

const editProfileSchema = z.object({
    displayName: z.string().min(1, "El nombre es obligatorio."),
    phone: z.string().optional(),
    location: z.string().optional(),
    bio: z.string().optional(),
    currentPassword: z.string().min(6, ""),
    repeatPassword: z.string().min(6, "Debes ingresar correctamente tu contraseña para confirmar cambios."),
});

type EditProfileFormValues = z.infer<typeof editProfileSchema>;

const EditProfile = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const { toast } = useToast();

    const form = useForm<EditProfileFormValues>({
        resolver: zodResolver(editProfileSchema),
        defaultValues: {
            displayName: user?.displayName ?? "",
            phone: user?.phone ?? "",
            location: user?.location ?? "",
            bio: user?.bio ?? "",
            currentPassword: "",
        },
    });

    // ---------------------------------------------
    // MUTACIÓN PARA ACTUALIZAR PERFIL
    // ---------------------------------------------

    const updateProfileMutation = useMutation({
        mutationFn: async (values: EditProfileFormValues) => {
            if (!user?.id) throw new Error("Debes iniciar sesión para modificar tu perfil.");

            const payload = {
                displayName: values.displayName,
                phone: values.phone,
                location: values.location,
                bio: values.bio,
                currentPassword: values.currentPassword, // ✅ contraseña para verificación
            };

            return apiClient.put(`/api/profiles/${user.id}`, payload);
        },
        onSuccess: () => {
            toast({
                title: "Cambios guardados",
                description: "Tu perfil ha sido actualizado exitosamente.",
            });
            navigate("/profile");
        },
        onError: (error: unknown) => {
            toast({
                title: "Error",
                description:
                    error instanceof Error
                        ? error.message
                        : "No pudimos actualizar tu perfil. Verifica tu contraseña.",
                variant: "destructive",
            });
        },
    });

    const onSubmit = (values: EditProfileFormValues) => updateProfileMutation.mutate(values);

    const isSubmitting = updateProfileMutation.isPending;

    // ---------------------------------------------
    // UI
    // ---------------------------------------------

    return (
        <div className="min-h-screen bg-muted/40 py-10">
            <div className="container max-w-3xl px-4">
                <Card>
                    <CardHeader className="space-y-4">
                        <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            className="-ml-2"
                            onClick={() => navigate(-1)}
                        >
                            <ChevronLeft className="mr-1 h-4 w-4" /> Volver
                        </Button>

                        <CardTitle>Editar Perfil</CardTitle>
                        <CardDescription>
                            Actualiza tu información personal y confirma tu identidad con tu contraseña.
                        </CardDescription>
                    </CardHeader>

                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
                            <CardContent className="space-y-6">
                                {/* Nombre */}
                                <FormField
                                    control={form.control}
                                    name="displayName"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Nombre</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Tu nombre completo" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                {/* Teléfono */}
                                <FormField
                                    control={form.control}
                                    name="phone"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Teléfono / contacto</FormLabel>
                                            <FormControl>
                                                <Input placeholder="+56 9 1234 5678" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                {/* Ubicación */}
                                <FormField
                                    control={form.control}
                                    name="location"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Ubicación</FormLabel>
                                            <FormControl>
                                                <Input placeholder="Ciudad, país" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                {/* Bio */}
                                <FormField
                                    control={form.control}
                                    name="bio"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Biografía</FormLabel>
                                            <FormControl>
                                                <Textarea
                                                    placeholder="Escribe algo sobre ti..."
                                                    className="min-h-[120px]"
                                                    {...field}
                                                />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                {/* Contraseña actual */}
                                <FormField
                                    control={form.control}
                                    name="currentPassword"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Escribe tu contraseña para confirmar cambios</FormLabel>
                                            <FormControl>
                                                <Input type="password" placeholder="••••••••" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />

                                <FormField
                                    control={form.control}
                                    name="repeatPassword"
                                    render={({ field }) => (
                                        <FormItem>
                                            <FormLabel>Repetir contraseña</FormLabel>
                                            <FormControl>
                                                <Input type="password" placeholder="••••••••" {...field} />
                                            </FormControl>
                                            <FormMessage />
                                        </FormItem>
                                    )}
                                />
                            </CardContent>

                            <CardFooter className="flex items-center justify-end border-t bg-muted/50 px-6 py-4">
                                <Button type="submit" disabled={isSubmitting}>
                                    {isSubmitting ? (
                                        <>
                                            <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Guardando...
                                        </>
                                    ) : (
                                        "Guardar cambios"
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

export default EditProfile;
