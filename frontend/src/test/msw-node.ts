import { HttpResponse, RestHandler, RestRequest } from "./msw";

type FetchType = typeof fetch;

type ServerHandler = RestHandler;

type ServerApi = {
  listen: () => void;
  use: (...handlers: ServerHandler[]) => void;
  resetHandlers: (...handlers: ServerHandler[]) => void;
  close: () => void;
};

const createRestRequest = (request: Request): RestRequest => {
  const clone = request.clone();
  const url = new URL(request.url);
  return {
    url,
    method: request.method.toUpperCase(),
    headers: request.headers,
    text: () => clone.text(),
    json: <T>() => clone.json() as Promise<T>,
  };
};

const createResponse = (response: HttpResponse) => {
  const headers = new Headers(response.init.headers ?? {});
  return new Response(response.body, {
    status: response.init.status ?? 200,
    statusText: response.init.statusText,
    headers,
  });
};

export const setupServer = (...initialHandlers: ServerHandler[]): ServerApi => {
  let originalFetch: FetchType | null = null;
  let handlers: ServerHandler[] = [...initialHandlers];
  const baseHandlers: ServerHandler[] = [...handlers];

  const findHandler = (request: Request) => {
    const url = new URL(request.url);
    const method = request.method.toUpperCase();
    return handlers.find((handler) => handler.predicate(url, method));
  };

  const interceptFetch: FetchType = async (input, init) => {
    const request = new Request(input, init);
    const matched = findHandler(request);

    if (!matched) {
      if (!originalFetch) {
        throw new Error("No original fetch implementation available");
      }
      return originalFetch(input, init);
    }

    const restRequest = createRestRequest(request);
    const mockedResponse = await matched.resolver(restRequest);

    if (!(mockedResponse instanceof HttpResponse)) {
      throw new Error("Handler resolvers must return an instance of HttpResponse");
    }

    return createResponse(mockedResponse);
  };

  return {
    listen: () => {
      if (!originalFetch) {
        originalFetch = globalThis.fetch.bind(globalThis);
        globalThis.fetch = interceptFetch;
      }
    },
    use: (...nextHandlers: ServerHandler[]) => {
      handlers.push(...nextHandlers);
    },
    resetHandlers: (...nextHandlers: ServerHandler[]) => {
      handlers = [...baseHandlers];
      if (nextHandlers.length > 0) {
        handlers.push(...nextHandlers);
      }
    },
    close: () => {
      if (originalFetch) {
        globalThis.fetch = originalFetch;
        originalFetch = null;
      }
      handlers = [...baseHandlers];
    },
  };
};
