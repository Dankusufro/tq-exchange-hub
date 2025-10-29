import Header from "@/components/Header";
import Footer from "@/components/Footer";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { apiClient } from "@/lib/api";
import { useAuth } from "@/providers/AuthProvider";
import { useQuery } from "@tanstack/react-query";
import { CalendarDays, MapPin, Phone, Sparkles, Star, TrendingUp } from "lucide-react";

const formatDate = (value: string | null) => {
  if (!value) {
    return null;
  }

  try {
    return new Intl.DateTimeFormat("es-MX", { dateStyle: "long" }).format(new Date(value));
  } catch (error) {
    console.error("Failed to format date", error);
    return null;
  }
};

const formatRating = (value: number | null) => {
  if (value === null || value === undefined) {
    return null;
  }

  return value.toFixed(1).replace(/\.0$/, "");
};

const formatPhone = (value: string | null) => {
  if (!value) {
    return null;
  }

  return value.replace(/(\d{3})(\d{3})(\d{4})/, "$1 $2 $3");
};

type Profile = {
  id: string;
  displayName: string;
  bio: string | null;
  avatarUrl: string | null;
  location: string | null;
  phone: string | null;
  rating: number | null;
  totalTrades: number | null;
  createdAt: string | null;
  updatedAt: string | null;
};

const Profile = () => {
  const { user } = useAuth();

  const {
    data: profile,
    isFetching,
    isError,
    refetch,
  } = useQuery<Profile>({
    queryKey: ["profile", user?.id],
    queryFn: async () => apiClient.get<Profile>("/api/profile/me"),
    enabled: Boolean(user),
    initialData: user ?? undefined,
  });

  if (!user) {
    return null;
  }

  const currentProfile = profile ?? user;
  const joinedDate = formatDate(currentProfile.createdAt);
  const ratingLabel = formatRating(currentProfile.rating);
  const formattedPhone = formatPhone(currentProfile.phone);

  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1 bg-muted/30 py-12">
        <div className="container mx-auto px-4 space-y-10">
          <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
            <div className="space-y-2">
              <h1 className="text-3xl font-bold text-foreground">Mi perfil</h1>
              <p className="text-muted-foreground max-w-2xl">
                Gestiona tu presencia en TruequePlus, comparte tu historia y mantiene al día tu
                información para inspirar confianza en la comunidad.
              </p>
            </div>
            {isFetching && (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <span className="h-3 w-3 animate-ping rounded-full bg-primary" />
                Sincronizando con el servidor...
              </div>
            )}
          </div>

          {isError ? (
            <Card className="border-destructive/30 bg-destructive/5">
              <CardHeader>
                <CardTitle className="text-destructive">No pudimos cargar tu perfil</CardTitle>
                <CardDescription className="text-destructive/90">
                  Ocurrió un problema al obtener tu información más reciente. Intenta nuevamente.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <Button variant="destructive" onClick={() => refetch()}>
                  Reintentar
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
              <Card className="lg:col-span-1 lg:row-span-2">
                <CardHeader className="flex flex-col gap-6 md:flex-row md:items-center">
                  <Avatar className="h-24 w-24 border-2 border-primary/20">
                    <AvatarImage
                      src={currentProfile.avatarUrl ?? undefined}
                      alt={currentProfile.displayName}
                    />
                    <AvatarFallback className="text-xl font-semibold">
                      {currentProfile.displayName
                        .split(" ")
                        .map((part) => part.charAt(0))
                        .join("")
                        .slice(0, 2)
                        .toUpperCase() || "?"}
                    </AvatarFallback>
                  </Avatar>

                  <div className="space-y-3 text-center md:text-left">
                    <div>
                      <CardTitle className="text-2xl">{currentProfile.displayName}</CardTitle>
                      {currentProfile.location && (
                        <p className="flex items-center justify-center gap-2 text-sm text-muted-foreground md:justify-start">
                          <MapPin className="h-4 w-4" />
                          {currentProfile.location}
                        </p>
                      )}
                    </div>

                    <div className="flex flex-wrap items-center justify-center gap-2 md:justify-start">
                      {joinedDate && (
                        <Badge variant="secondary" className="flex items-center gap-1">
                          <CalendarDays className="h-3.5 w-3.5" />
                          Miembro desde {joinedDate}
                        </Badge>
                      )}
                      {(currentProfile.totalTrades ?? 0) > 0 && (
                        <Badge className="flex items-center gap-1 bg-gradient-primary text-white">
                          <TrendingUp className="h-3.5 w-3.5" />
                          {currentProfile.totalTrades} trueques completados
                        </Badge>
                      )}
                      {ratingLabel && (
                        <Badge variant="outline" className="flex items-center gap-1">
                          <Star className="h-3.5 w-3.5 text-amber-500" />
                          {ratingLabel} / 5.0
                        </Badge>
                      )}
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="space-y-6">
                  <section className="space-y-2">
                    <h2 className="text-lg font-semibold text-foreground">Sobre mí</h2>
                    <p className="text-sm leading-relaxed text-muted-foreground">
                      {currentProfile.bio?.trim() ||
                        "Aún no has agregado una biografía. Comparte tus habilidades, intereses y lo que buscas intercambiar para que otros usuarios te conozcan mejor."}
                    </p>
                  </section>

                  <Separator />

                  <section className="space-y-4">
                    <h2 className="text-lg font-semibold text-foreground">Datos de contacto</h2>
                    <ul className="space-y-3 text-sm text-muted-foreground">
                      <li className="flex items-center gap-3">
                        <Phone className="h-4 w-4 text-foreground" />
                        {formattedPhone || "Aún no agregas un número de contacto"}
                      </li>
                      <li className="flex items-center gap-3">
                        <Sparkles className="h-4 w-4 text-foreground" />
                        {currentProfile.updatedAt
                          ? `Última actualización ${formatDate(currentProfile.updatedAt)}`
                          : "Mantén tu información actualizada para generar confianza"}
                      </li>
                    </ul>
                  </section>
                </CardContent>
              </Card>

              <Card className="lg:col-span-1">
                <CardHeader>
                  <CardTitle>Resumen de actividad</CardTitle>
                  <CardDescription>
                    Estadísticas generales de tus intercambios en la plataforma.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <dl className="grid gap-4">
                    <div className="rounded-lg border bg-muted/40 p-4">
                      <dt className="text-xs uppercase text-muted-foreground tracking-wide">
                        Trueques concretados
                      </dt>
                      <dd className="mt-2 text-2xl font-semibold text-foreground">
                        {currentProfile.totalTrades ?? 0}
                      </dd>
                    </div>
                    <div className="rounded-lg border bg-muted/40 p-4">
                      <dt className="text-xs uppercase text-muted-foreground tracking-wide">
                        Calificación promedio
                      </dt>
                      <dd className="mt-2 text-2xl font-semibold text-foreground">
                        {ratingLabel ? `${ratingLabel} / 5` : "Sin valoraciones"}
                      </dd>
                    </div>
                  </dl>
                </CardContent>
              </Card>
            </div>
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default Profile;
