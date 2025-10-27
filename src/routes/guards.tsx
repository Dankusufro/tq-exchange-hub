import { ReactNode, useEffect, useState } from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import type { Session } from "@supabase/supabase-js";
import { supabase } from "@/integrations/supabase/client";

type GuardProps = {
  children?: ReactNode;
  redirectTo?: string;
};

type SessionState = {
  session: Session | null;
  loading: boolean;
};

const useSupabaseSession = (): SessionState => {
  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    const loadSession = async () => {
      const { data } = await supabase.auth.getSession();
      if (!isMounted) return;

      setSession(data.session ?? null);
      setLoading(false);
    };

    loadSession();

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, newSession) => {
      if (!isMounted) return;

      setSession(newSession);
      setLoading(false);
    });

    return () => {
      isMounted = false;
      subscription.unsubscribe();
    };
  }, []);

  return { session, loading };
};

export const ProtectedRoute = ({ children, redirectTo = "/auth" }: GuardProps) => {
  const location = useLocation();
  const { session, loading } = useSupabaseSession();

  if (loading) {
    return null;
  }

  if (!session) {
    return <Navigate to={redirectTo} replace state={{ from: location }} />;
  }

  return children ?? <Outlet />;
};

export const PublicOnlyRoute = ({ children, redirectTo = "/" }: GuardProps) => {
  const { session, loading } = useSupabaseSession();

  if (loading) {
    return null;
  }

  if (session) {
    return <Navigate to={redirectTo} replace />;
  }

  return children ?? <Outlet />;
};
