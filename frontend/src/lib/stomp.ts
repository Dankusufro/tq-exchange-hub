export interface StompFrame {
  command: string;
  headers: Record<string, string>;
  body: string;
}

export const STOMP_TERMINATOR = "\u0000";

export const buildFrame = (command: string, headers: Record<string, string>, body = "") => {
  const headerLines = Object.entries(headers)
    .map(([key, value]) => `${key}:${value}`)
    .join("\n");

  return `${command}\n${headerLines}\n\n${body}${STOMP_TERMINATOR}`;
};

export const parseFrames = (payload: string): StompFrame[] => {
  const frames: StompFrame[] = [];
  const segments = payload.split(STOMP_TERMINATOR).filter(Boolean);

  segments.forEach((segment) => {
    const [rawHeaders, ...rawBodyParts] = segment.split(/\n\n/);
    const headerLines = rawHeaders.split(/\n+/).filter(Boolean);
    const command = headerLines.shift()?.trim() ?? "";

    const headers: Record<string, string> = {};
    headerLines.forEach((line) => {
      const [key, ...valueParts] = line.split(":");
      if (!key) {
        return;
      }
      headers[key.trim()] = valueParts.join(":").trim();
    });

    const body = rawBodyParts.join("\n\n");
    frames.push({ command, headers, body });
  });

  return frames;
};

export const createSubscriptionId = (prefix: string, identifier: string) => `${prefix}-${identifier}`;
