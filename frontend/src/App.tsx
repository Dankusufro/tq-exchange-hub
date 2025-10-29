import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { NotificationsProvider } from "@/hooks/use-notifications";
import { FavoritesProvider } from "@/hooks/use-favorites";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate, useLocation } from "react-router-dom";
import type { ReactElement } from "react";
import Index from "./pages/Index";
import NotFound from "./pages/NotFound";
import Auth from "./pages/Auth";
import CreateListing from "./pages/CreateListing";
import Favorites from "./pages/Favorites";
import Profile from "./pages/Profile";
import { useAuth } from "./providers/AuthProvider";

const queryClient = new QueryClient();

const ProtectedRoute = ({ children }: { children: ReactElement }) => {
  const { user } = useAuth();
  const location = useLocation();
  const from = `${location.pathname}${location.search}${location.hash}`;

  if (!user) {
    return <Navigate to="/auth" replace state={{ from }} />;
  }

  return children;
};

const App = () => (
  <QueryClientProvider client={queryClient}>
    <FavoritesProvider>
      <NotificationsProvider>
        <TooltipProvider>
          <Toaster />
          <Sonner />
          <BrowserRouter basename={import.meta.env.BASE_URL}>
            <Routes>
              <Route path="/" element={<Index />} />
              <Route path="/auth" element={<Auth />} />
              <Route
                path="/listings/new"
                element={
                  <ProtectedRoute>
                    <CreateListing />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <Profile />
                  </ProtectedRoute>
                }
              />
              <Route path="/favorites" element={<Favorites />} />
              {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
              <Route path="*" element={<NotFound />} />
            </Routes>
          </BrowserRouter>
        </TooltipProvider>
      </NotificationsProvider>
    </FavoritesProvider>
  </QueryClientProvider>
);

export default App;
