import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { toast } from "@/components/ui/use-toast";
import { supabase } from "@/integrations/supabase/client";

const Auth = () => {
  const navigate = useNavigate();

  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [isLoginLoading, setIsLoginLoading] = useState(false);
  const [isRegisterLoading, setIsRegisterLoading] = useState(false);

  const handleLoginSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!loginForm.email || !loginForm.password) {
      toast({
        title: "Datos incompletos",
        description: "Ingresa tu correo y contraseña para continuar.",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsLoginLoading(true);
      const { error } = await supabase.auth.signInWithPassword({
        email: loginForm.email,
        password: loginForm.password,
      });

      if (error) {
        toast({
          title: "No se pudo iniciar sesión",
          description: error.message,
          variant: "destructive",
        });
        return;
      }

      toast({
        title: "Sesión iniciada",
        description: "Bienvenido de nuevo a TruequePlus.",
      });

      navigate("/");
    } catch (error) {
      toast({
        title: "Error inesperado",
        description: error instanceof Error ? error.message : "Intenta nuevamente más tarde.",
        variant: "destructive",
      });
    } finally {
      setIsLoginLoading(false);
    }
  };

  const handleRegisterSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!registerForm.name || !registerForm.email || !registerForm.password || !registerForm.confirmPassword) {
      toast({
        title: "Datos incompletos",
        description: "Todos los campos son obligatorios para crear tu cuenta.",
        variant: "destructive",
      });
      return;
    }

    if (registerForm.password !== registerForm.confirmPassword) {
      toast({
        title: "Las contraseñas no coinciden",
        description: "Revisa que ambas contraseñas sean iguales.",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsRegisterLoading(true);
      const { data, error } = await supabase.auth.signUp({
        email: registerForm.email,
        password: registerForm.password,
        options: {
          data: {
            full_name: registerForm.name,
          },
        },
      });

      if (error) {
        toast({
          title: "No se pudo crear la cuenta",
          description: error.message,
          variant: "destructive",
        });
        return;
      }

      toast({
        title: "Registro exitoso",
        description:
          data.session
            ? "Tu cuenta fue creada y ya puedes comenzar a intercambiar."
            : "Revisa tu correo para confirmar tu cuenta y luego inicia sesión.",
      });

      if (data.session) {
        navigate("/");
      }
    } catch (error) {
      toast({
        title: "Error inesperado",
        description: error instanceof Error ? error.message : "Intenta nuevamente más tarde.",
        variant: "destructive",
      });
    } finally {
      setIsRegisterLoading(false);
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
                <Button type="submit" className="w-full" disabled={isLoginLoading}>
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
                <Button type="submit" className="w-full" disabled={isRegisterLoading}>
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
