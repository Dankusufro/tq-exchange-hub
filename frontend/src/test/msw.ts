export interface RestRequest {
  url: URL;
  method: string;
  headers: Headers;
  text: () => Promise<string>;
  json: <T>() => Promise<T>;
}

export class HttpResponse {
  readonly body: BodyInit | null;
  readonly init: ResponseInit;

  constructor(body: BodyInit | null, init: ResponseInit = {}) {
    this.body = body;
    this.init = init;
  }

  static json(body: unknown, init: ResponseInit = {}) {
    const headers = new Headers(init.headers ?? {});
    if (!headers.has("content-type")) {
      headers.set("content-type", "application/json");
    }
    return new HttpResponse(JSON.stringify(body), {
      ...init,
      headers: Object.fromEntries(headers.entries()),
    });
  }
}

export type RestResolver = (request: RestRequest) => HttpResponse | Promise<HttpResponse>;

export interface RestHandler {
  method: string;
  predicate: (url: URL, method: string) => boolean;
  resolver: RestResolver;
}

const createPathPredicate = (path: string) => {
  if (/^https?:\/\//i.test(path)) {
    return (url: URL) => url.toString() === path;
  }
  return (url: URL) => url.pathname === path;
};

const createRestHandler = (method: string, path: string, resolver: RestResolver): RestHandler => {
  const predicate = createPathPredicate(path);
  return {
    method,
    predicate: (url, incomingMethod) => incomingMethod === method && predicate(url),
    resolver,
  };
};

export const http = {
  get: (path: string, resolver: RestResolver) => createRestHandler("GET", path, resolver),
  post: (path: string, resolver: RestResolver) => createRestHandler("POST", path, resolver),
};
