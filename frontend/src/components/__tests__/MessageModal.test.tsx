import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { http, HttpResponse } from "msw";
import { setupServer } from "msw/node";
import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from "vitest";

import MessageModal from "../MessageModal";
import { Toaster } from "@/components/ui/toaster";

const server = setupServer();

const defaultProps = {
  isOpen: true,
  onClose: vi.fn(),
  productId: "item-123",
  ownerId: "owner-456",
  productTitle: "Guitarra Acústica",
  userName: "Carla",
};

const renderModal = (props: Partial<typeof defaultProps> = {}) => {
  const mergedProps = { ...defaultProps, ...props };
  return render(
    <>
      <MessageModal {...mergedProps} />
      <Toaster />
    </>,
  );
};

beforeAll(() => {
  server.listen();
});

afterEach(() => {
  server.resetHandlers();
  defaultProps.onClose.mockReset();
});

afterAll(() => {
  server.close();
});

describe("MessageModal", () => {
  it("envía la propuesta de intercambio y muestra un toast de éxito", async () => {
    const user = userEvent.setup();
    let receivedBody: { ownerItemId: string; message: string } | null = null;

    server.use(
      http.post("http://localhost:8080/api/trades", async ({ json }) => {
        receivedBody = await json<{ ownerItemId: string; message: string }>();
        return HttpResponse.json({
          id: "trade-1",
          ownerId: "owner-456",
          requesterId: "requester-789",
          ownerItemId: "item-123",
          requesterItemId: null,
          message: receivedBody?.message ?? "",
          status: "PENDING",
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
        });
      }),
    );

    renderModal();

    const offerInput = screen.getByLabelText("¿Qué ofreces a cambio?");
    const messageInput = screen.getByLabelText("Mensaje (opcional)");

    await user.type(offerInput, "Mi bicicleta en excelente estado");
    await user.type(messageInput, "La entrego con accesorios adicionales.");

    await user.click(screen.getByRole("button", { name: "Enviar Propuesta" }));

    await waitFor(() => {
      expect(screen.getByText("Propuesta enviada")).toBeInTheDocument();
    });

    expect(receivedBody).toEqual({
      ownerItemId: "item-123",
      message: "Mi bicicleta en excelente estado\n\nLa entrego con accesorios adicionales.",
    });
    expect(defaultProps.onClose).toHaveBeenCalled();
    expect((offerInput as HTMLInputElement).value).toBe("");
    expect((messageInput as HTMLInputElement).value).toBe("");
  });

  it("muestra un toast de error si la petición falla", async () => {
    const user = userEvent.setup();

    server.use(
      http.post(
        "http://localhost:8080/api/trades",
        () =>
          HttpResponse.json(
            { message: "Algo salió mal" },
            { status: 500 },
          ),
      ),
    );

    renderModal();

    const offerInput = screen.getByLabelText("¿Qué ofreces a cambio?");
    await user.type(offerInput, "Colección de cómics");

    await user.click(screen.getByRole("button", { name: "Enviar Propuesta" }));

    await waitFor(() => {
      expect(screen.getByText("No se pudo enviar tu propuesta")).toBeInTheDocument();
    });

    expect(defaultProps.onClose).not.toHaveBeenCalled();
  });
});
