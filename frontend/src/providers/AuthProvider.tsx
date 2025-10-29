import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";

import { useToast } from "@/components/ui/use-toast";
import { apiClient, type ApiError, type AuthTokens } from "@/lib/api";

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

type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  profile: Profile;
};

type AuthSession = {
  tokens: AuthTokens;
  profile: Profile;
};

type LoginCredentials = {
  email: string;
  password: string;
};

type RegisterCredentials = {
  email: string;
  password: string;
  displayName: string;
};

type AuthContextValue = {
  user: Profile | null;
  session: AuthSession | null;
  signIn: (credentials: LoginCredentials) => Promise<AuthSession>;
  signUp: (credentials: RegisterCredentials) => Promise<AuthSession>;
  signOut: () => Promise<void>;
};

type NotifiableApiError = ApiError & { alreadyNotified?: boolean };

const isUnauthorizedError = (error: unknown): error is NotifiableApiError =>
  typeof error === "object" && error !== null && (error as ApiError).status === 401;

const toFriendlyError = (
  message: string,
  status?: number,
  alreadyNotified = false,
): NotifiableApiError => {
  const error = new Error(message) as NotifiableApiError;
  error.status = status;
  if (alreadyNotified) {
    error.alreadyNotified = true;
  }
  return error;
};

const SESSION_STORAGE_KEY = "exchangehub.session";

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const toAuthSession = (response: AuthResponse): AuthSession => ({
  tokens: {
    accessToken: response.accessToken,
    refreshToken: response.refreshToken,
  },
  profile: response.profile,
});

const readStoredSession = (): AuthSession | null => {
  if (typeof window === "undefined") {
    return null;
  }

  const stored = window.localStorage.getItem(SESSION_STORAGE_KEY);
  if (!stored) {
    return null;
  }

  try {
    return JSON.parse(stored) as AuthSession;
  } catch (error) {
    console.error("Failed to parse stored session", error);
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return null;
  }
};

const persistSession = (session: AuthSession | null) => {
  if (typeof window === "undefined") {
    return;
  }

  if (!session) {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return;
  }

  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [session, setSession] = useState<AuthSession | null>(null);
  const sessionRef = useRef<AuthSession | null>(null);
  const user = session?.profile ?? null;
  const { toast } = useToast();

  const applySession = useCallback((nextSession: AuthSession | null) => {
    sessionRef.current = nextSession;
    setSession(nextSession);
    persistSession(nextSession);
    apiClient.setTokens(nextSession?.tokens ?? null);
  }, []);

  const fetchSession = useCallback(async () => {
    try {
      const currentSession = await apiClient.get<AuthResponse>("/api/auth/session");
      applySession(toAuthSession(currentSession));
    } catch (error) {
      if (isUnauthorizedError(error)) {
        applySession(null);
        toast({
          title: "Tu sesión ha expirado",
          description: "Por seguridad te pedimos iniciar sesión nuevamente.",
          variant: "destructive",
        });
        error.alreadyNotified = true;
        return;
      }

      console.error("Failed to fetch session", error);
      applySession(null);
    }
  }, [applySession, toast]);

  useEffect(() => {
    const storedSession = readStoredSession();

    if (storedSession) {
      sessionRef.current = storedSession;
      setSession(storedSession);
      apiClient.setTokens(storedSession.tokens);
    }

    if (apiClient.getTokens()) {
      void fetchSession();
    }

    const unsubscribe = apiClient.subscribe((updatedTokens) => {
      if (!updatedTokens) {
        applySession(null);
        return;
      }

      const currentSession = sessionRef.current;

      if (!currentSession) {
        void fetchSession();
        return;
      }

      if (
        currentSession.tokens.accessToken === updatedTokens.accessToken &&
        currentSession.tokens.refreshToken === updatedTokens.refreshToken
      ) {
        return;
      }

      const nextSession: AuthSession = {
        ...currentSession,
        tokens: updatedTokens,
      };

      sessionRef.current = nextSession;
      setSession(nextSession);
      persistSession(nextSession);
    });

    return unsubscribe;
  }, [applySession, fetchSession]);

  const signIn = useCallback<AuthContextValue["signIn"]>(
    async (credentials) => {
      try {
        const response = await apiClient.post<AuthResponse>("/api/auth/login", credentials, { auth: false });
        const nextSession = toAuthSession(response);
        applySession(nextSession);
        return nextSession;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          applySession(null);
          const message = "Credenciales inválidas. Verifica tu correo y contraseña.";
          toast({
            title: "No pudimos iniciar sesión",
            description: message,
            variant: "destructive",
          });
          throw toFriendlyError(message, error.status, true);
        }

        const message =
          error instanceof Error
            ? error.message
            : "No pudimos iniciar sesión. Inténtalo nuevamente más tarde.";
        toast({
          title: "No pudimos iniciar sesión",
          description: message,
          variant: "destructive",
        });
        throw toFriendlyError(message, (error as ApiError | undefined)?.status, true);
      }
    },
    [applySession, toast],
  );

  const signUp = useCallback<AuthContextValue["signUp"]>(
    async (credentials) => {
      try {
        const response = await apiClient.post<AuthResponse>("/api/auth/register", credentials, { auth: false });
        const nextSession = toAuthSession(response);
        applySession(nextSession);
        return nextSession;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          applySession(null);
          const message = "No pudimos crear tu cuenta. Verifica los datos proporcionados.";
          toast({
            title: "Registro fallido",
            description: message,
            variant: "destructive",
          });
          throw toFriendlyError(message, error.status, true);
        }

        const message =
          error instanceof Error
            ? error.message
            : "No pudimos crear tu cuenta. Inténtalo nuevamente más tarde.";
        toast({
          title: "Registro fallido",
          description: message,
          variant: "destructive",
        });
        throw toFriendlyError(message, (error as ApiError | undefined)?.status, true);
      }
    },
    [applySession, toast],
  );

  const signOut = useCallback<AuthContextValue["signOut"]>(async () => {
    try {
      await apiClient.post<void>("/api/auth/logout");
    } catch (error) {
      console.error("Failed to sign out", error);
    } finally {
      applySession(null);
    }
  }, [applySession]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      session,
      signIn,
      signUp,
      signOut,
    }),
    [session, signIn, signOut, signUp, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
};
