import { FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
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
  // @ts-expect-error React Router does not expose a typed location state helper for this case
  const location = useLocation<{ from?: string }>();
  const { signIn, signUp, requestPasswordReset, resetPassword } = useAuth();
  const { toast } = useToast();

  const redirectTo = location.state?.from ?? "/";

  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [loginLoading, setLoginLoading] = useState(false);
  const [registerLoading, setRegisterLoading] = useState(false);
  const [forgotForm, setForgotForm] = useState({ email: "" });
  const [resetForm, setResetForm] = useState({ token: "", password: "", confirmPassword: "" });
  const [forgotLoading, setForgotLoading] = useState(false);
  const [resetLoading, setResetLoading] = useState(false);

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
      navigate(redirectTo, { replace: true });
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
      navigate(redirectTo, { replace: true });
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

  const handleForgotPasswordSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setForgotLoading(true);

    try {
      await requestPasswordReset({ email: forgotForm.email });
      toast({
        title: "Solicitud enviada",
        description: "Revisa tu correo para continuar con el restablecimiento.",
      });
      setForgotForm({ email: "" });
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "No pudimos procesar la solicitud. Inténtalo nuevamente.";
      showErrorToast("No pudimos enviar el correo", message, error as NotifiableError);
    } finally {
      setForgotLoading(false);
    }
  };

  const handleResetPasswordSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (resetForm.password !== resetForm.confirmPassword) {
      toast({
        title: "Las contraseñas no coinciden",
        description: "Revisa que ambas contraseñas sean iguales.",
        variant: "destructive",
      });
      return;
    }

    setResetLoading(true);

    try {
      await resetPassword({
        token: resetForm.token,
        newPassword: resetForm.password,
        confirmPassword: resetForm.confirmPassword,
      });
      toast({
        title: "Contraseña actualizada",
        description: "Ya puedes iniciar sesión con tu nueva contraseña.",
      });
      setResetForm({ token: "", password: "", confirmPassword: "" });
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : "No pudimos actualizar tu contraseña. Inténtalo nuevamente.";
      showErrorToast("Cambio de contraseña fallido", message, error as NotifiableError);
    } finally {
      setResetLoading(false);
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
            <TabsList className="grid grid-cols-3">
              <TabsTrigger value="login">Iniciar sesión</TabsTrigger>
              <TabsTrigger value="register">Registrarme</TabsTrigger>
              <TabsTrigger value="recover">Recuperar acceso</TabsTrigger>
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
                    minLength={8}
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
                    minLength={8}
                  />
                </div>
                <Button type="submit" className="w-full" disabled={registerLoading}>
                  Crear cuenta
                </Button>
              </form>
            </TabsContent>
            <TabsContent value="recover">
              <div className="grid gap-6 md:grid-cols-2">
                <form className="space-y-4" onSubmit={handleForgotPasswordSubmit}>
                  <div className="space-y-2">
                    <Label htmlFor="forgot-email">Correo electrónico</Label>
                    <Input
                      id="forgot-email"
                      type="email"
                      placeholder="tu@correo.com"
                      value={forgotForm.email}
                      onChange={(event) => setForgotForm({ email: event.target.value })}
                      required
                    />
                  </div>
                  <Button type="submit" className="w-full" disabled={forgotLoading}>
                    Enviarme instrucciones
                  </Button>
                </form>
                <form className="space-y-4" onSubmit={handleResetPasswordSubmit}>
                  <div className="space-y-2">
                    <Label htmlFor="reset-token">Token de restablecimiento</Label>
                    <Input
                      id="reset-token"
                      placeholder="Ingresa el token recibido"
                      value={resetForm.token}
                      onChange={(event) => setResetForm({ ...resetForm, token: event.target.value })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="reset-password">Nueva contraseña</Label>
                    <Input
                      id="reset-password"
                      type="password"
                      placeholder="Crea una nueva contraseña"
                      value={resetForm.password}
                      onChange={(event) => setResetForm({ ...resetForm, password: event.target.value })}
                      required
                      minLength={8}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="reset-confirm-password">Confirmar contraseña</Label>
                    <Input
                      id="reset-confirm-password"
                      type="password"
                      placeholder="Repite tu contraseña"
                      value={resetForm.confirmPassword}
                      onChange={(event) =>
                        setResetForm({ ...resetForm, confirmPassword: event.target.value })
                      }
                      required
                      minLength={8}
                    />
                  </div>
                  <Button type="submit" className="w-full" disabled={resetLoading}>
                    Cambiar contraseña
                  </Button>
                </form>
              </div>
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
