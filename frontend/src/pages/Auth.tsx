import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useToast } from "@/components/ui/use-toast";
import { useAuth } from "@/providers/AuthProvider";

type NotifiableError = Error & { status?: number; alreadyNotified?: boolean };

const Auth = () => {
  const navigate = useNavigate();
  const { signIn, signUp } = useAuth();
  const { toast } = useToast();

  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [loginLoading, setLoginLoading] = useState(false);
  const [registerLoading, setRegisterLoading] = useState(false);

  const showErrorToast = (title: string, description: string, error?: NotifiableError) => {
    if (error?.alreadyNotified) {
      return;
    }

    toast({
      title,
      description,
      variant: "destructive",
    });
  };

  const handleLoginSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoginLoading(true);

    try {
      const session = await signIn({ ...loginForm });
      toast({
        title: "Sesión iniciada",
        description: `Bienvenido de nuevo, ${session.profile.displayName}!`,
      });
      navigate("/");
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "No pudimos iniciar sesión. Inténtalo nuevamente más tarde.";
      showErrorToast("No pudimos iniciar sesión", message, error as NotifiableError);
    } finally {
      setLoginLoading(false);
    }
  };

  const handleRegisterSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (registerForm.password !== registerForm.confirmPassword) {
      toast({
        title: "Las contraseñas no coinciden",
        description: "Revisa que ambas contraseñas sean iguales.",
        variant: "destructive",
      });
      return;
    }

    setRegisterLoading(true);

    try {
      const session = await signUp({
        email: registerForm.email,
        password: registerForm.password,
        displayName: registerForm.name,
      });
      toast({
        title: "Registro exitoso",
        description: `¡Hola ${session.profile.displayName}! Tu cuenta ya está lista para usarla en TruequePlus.`,
      });
      navigate("/");
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "No pudimos crear tu cuenta. Inténtalo nuevamente más tarde.";
      showErrorToast("Registro fallido", message, error as NotifiableError);
    } finally {
      setRegisterLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-muted flex items-center justify-center p-4">
      <Card className="w-full max-w-xl">
        <CardHeader>
          <CardTitle>Bienvenido a TruequePlus</CardTitle>
          <CardDescription>
            Crea una cuenta nueva o inicia sesión para comenzar a intercambiar productos y servicios.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="login" className="space-y-4">
            <TabsList className="grid grid-cols-2">
              <TabsTrigger value="login">Iniciar sesión</TabsTrigger>
              <TabsTrigger value="register">Registrarme</TabsTrigger>
            </TabsList>
            <TabsContent value="login">
              <form className="space-y-4" onSubmit={handleLoginSubmit}>
                <div className="space-y-2">
                  <Label htmlFor="login-email">Correo electrónico</Label>
                  <Input
                    id="login-email"
                    type="email"
                    placeholder="tu@correo.com"
                    value={loginForm.email}
                    onChange={(event) => setLoginForm({ ...loginForm, email: event.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="login-password">Contraseña</Label>
                  <Input
                    id="login-password"
                    type="password"
                    placeholder="••••••••"
                    value={loginForm.password}
                    onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })}
                    required
                  />
                </div>
                <Button type="submit" className="w-full" disabled={loginLoading}>
                  Iniciar sesión
                </Button>
              </form>
            </TabsContent>
            <TabsContent value="register">
              <form className="space-y-4" onSubmit={handleRegisterSubmit}>
                <div className="space-y-2">
                  <Label htmlFor="register-name">Nombre completo</Label>
                  <Input
                    id="register-name"
                    placeholder="Tu nombre"
                    value={registerForm.name}
                    onChange={(event) => setRegisterForm({ ...registerForm, name: event.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="register-email">Correo electrónico</Label>
                  <Input
                    id="register-email"
                    type="email"
                    placeholder="tu@correo.com"
                    value={registerForm.email}
                    onChange={(event) => setRegisterForm({ ...registerForm, email: event.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="register-password">Contraseña</Label>
                  <Input
                    id="register-password"
                    type="password"
                    placeholder="Crea una contraseña"
                    value={registerForm.password}
                    onChange={(event) => setRegisterForm({ ...registerForm, password: event.target.value })}
                    required
                    minLength={6}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="register-confirm-password">Confirmar contraseña</Label>
                  <Input
                    id="register-confirm-password"
                    type="password"
                    placeholder="Repite tu contraseña"
                    value={registerForm.confirmPassword}
                    onChange={(event) =>
                      setRegisterForm({ ...registerForm, confirmPassword: event.target.value })
                    }
                    required
                    minLength={6}
                  />
                </div>
                <Button type="submit" className="w-full" disabled={registerLoading}>
                  Crear cuenta
                </Button>
              </form>
            </TabsContent>
          </Tabs>
        </CardContent>
        <CardFooter className="flex-col gap-2 text-sm text-muted-foreground">
          <span>
            Al continuar aceptas nuestros términos y condiciones. Puedes volver al inicio cuando quieras.
          </span>
          <Button variant="link" asChild>
            <Link to="/">Volver a la página principal</Link>
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
};

export default Auth;
