import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import Auth from "../Auth";

const mockToast = vi.fn();
const mockSignIn = vi.fn();
const mockSignUp = vi.fn();
const mockSignOut = vi.fn();
const navigateMock = vi.fn();

vi.mock("@/providers/AuthProvider", () => ({
  useAuth: () => ({
    user: null,
    session: null,
    isHydrated: true,
    signIn: mockSignIn,
    signUp: mockSignUp,
    signOut: mockSignOut,
  }),
}));

vi.mock("@/components/ui/use-toast", () => ({
  useToast: () => ({ toast: mockToast }),
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

type Deferred<T> = {
  promise: Promise<T>;
  resolve: (value: T) => void;
};

const createDeferred = <T,>(): Deferred<T> => {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((res) => {
    resolve = res;
  });
  return { promise, resolve };
};

const renderAuth = () =>
  render(
    <MemoryRouter>
      <Auth />
    </MemoryRouter>,
  );

const createSession = (displayName: string) => ({
  tokens: { accessToken: "token", refreshToken: "refresh" },
  profile: {
    id: "id-1",
    displayName,
    bio: null,
    avatarUrl: null,
    location: null,
    phone: null,
    rating: null,
    totalTrades: null,
    createdAt: null,
    updatedAt: null,
  },
});

beforeEach(() => {
  vi.clearAllMocks();
});

describe("Auth page", () => {
  it("allows signing in and shows the success toast", async () => {
    const deferred = createDeferred<ReturnType<typeof createSession>>();
    mockSignIn.mockReturnValue(deferred.promise);
    const user = userEvent.setup();
    renderAuth();

    await user.type(screen.getByLabelText(/Correo electrónico/i, { selector: "#login-email" }), "user@example.com");
    await user.type(screen.getByLabelText(/Contraseña/i, { selector: "#login-password" }), "password123");

    const submitButton = screen.getByRole("button", { name: /iniciar sesión/i });
    await user.click(submitButton);

    expect(submitButton).toBeDisabled();

    const session = createSession("Test User");
    deferred.resolve(session);

    await waitFor(() => {
      expect(mockSignIn).toHaveBeenCalledWith({ email: "user@example.com", password: "password123" });
    });

    await waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          title: "Sesión iniciada",
          description: expect.stringContaining("Test User"),
        }),
      );
    });

    expect(navigateMock).toHaveBeenCalledWith("/");
    expect(submitButton).not.toBeDisabled();
  });

  it("maps the register name to displayName and notifies on success", async () => {
    const deferred = createDeferred<ReturnType<typeof createSession>>();
    mockSignUp.mockReturnValue(deferred.promise);
    const user = userEvent.setup();
    renderAuth();

    await user.click(screen.getByRole("tab", { name: /registrarme/i }));

    await user.type(screen.getByLabelText(/nombre completo/i), "New User");
    await user.type(screen.getByLabelText(/Correo electrónico/i, { selector: "#register-email" }), "new@example.com");
    await user.type(screen.getByLabelText(/Contraseña/i, { selector: "#register-password" }), "password123");
    await user.type(
      screen.getByLabelText(/confirmar contraseña/i, { selector: "#register-confirm-password" }),
      "password123",
    );

    const submitButton = screen.getByRole("button", { name: /crear cuenta/i });
    await user.click(submitButton);

    expect(submitButton).toBeDisabled();

    const session = createSession("New User");
    deferred.resolve(session);

    await waitFor(() => {
      expect(mockSignUp).toHaveBeenCalledWith({
        email: "new@example.com",
        password: "password123",
        displayName: "New User",
      });
    });

    await waitFor(() => {
      expect(mockToast).toHaveBeenCalledWith(
        expect.objectContaining({
          title: "Registro exitoso",
          description: expect.stringContaining("New User"),
        }),
      );
    });

    expect(navigateMock).toHaveBeenCalledWith("/");
  });

  it("shows a validation toast when passwords do not match", async () => {
    const user = userEvent.setup();
    renderAuth();

    await user.click(screen.getByRole("tab", { name: /registrarme/i }));

    await user.type(screen.getByLabelText(/nombre completo/i), "Mismatch User");
    await user.type(
      screen.getByLabelText(/Correo electrónico/i, { selector: "#register-email" }),
      "mismatch@example.com",
    );
    await user.type(screen.getByLabelText(/Contraseña/i, { selector: "#register-password" }), "password123");
    await user.type(
      screen.getByLabelText(/confirmar contraseña/i, { selector: "#register-confirm-password" }),
      "different123",
    );

    await user.click(screen.getByRole("button", { name: /crear cuenta/i }));

    expect(mockSignUp).not.toHaveBeenCalled();
    expect(mockToast).toHaveBeenCalledWith(
      expect.objectContaining({
        title: "Las contraseñas no coinciden",
      }),
    );
  });
});
