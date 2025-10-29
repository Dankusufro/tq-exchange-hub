import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { NotificationsProvider } from "@/hooks/use-notifications";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate, useLocation } from "react-router-dom";
import type { ReactElement } from "react";
import Index from "./pages/Index";
import NotFound from "./pages/NotFound";
import Auth from "./pages/Auth";
import CreateListing from "./pages/CreateListing";
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
            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </NotificationsProvider>
  </QueryClientProvider>
);

export default App;
