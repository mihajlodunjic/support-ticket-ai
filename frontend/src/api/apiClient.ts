import { getAuthToken } from '../auth/authStorage';
import type { ApiErrorDetails } from '../types/api';

const rawBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim() || 'http://localhost:8080';
export const API_BASE_URL = rawBaseUrl.replace(/\/+$/, '');

type QueryValue = string | number | boolean | undefined | null;

export interface ApiRequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
  query?: Record<string, QueryValue>;
}

export class ApiClientError extends Error {
  status: number;
  details?: ApiErrorDetails;
  rawBody?: unknown;

  constructor(message: string, status: number, details?: ApiErrorDetails, rawBody?: unknown) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.details = details;
    this.rawBody = rawBody;
  }
}

export async function apiRequest<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  const { body, query, headers, ...rest } = options;
  const url = buildUrl(path, query);
  const requestHeaders = new Headers(headers);
  const token = getAuthToken();

  if (body !== undefined && !(body instanceof FormData) && !requestHeaders.has('Content-Type')) {
    requestHeaders.set('Content-Type', 'application/json');
  }

  if (token && !requestHeaders.has('Authorization')) {
    requestHeaders.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(url, {
    ...rest,
    headers: requestHeaders,
    body: serializeBody(body),
  });

  const parsedBody = await parseResponseBody(response);

  if (!response.ok) {
    const details = normalizeApiError(parsedBody, response.status);
    const message =
      details?.message ||
      (typeof parsedBody === 'string' && parsedBody.trim()) ||
      response.statusText ||
      'Request failed.';

    throw new ApiClientError(message, response.status, details, parsedBody);
  }

  return parsedBody as T;
}

function buildUrl(path: string, query?: Record<string, QueryValue>): string {
  const url = new URL(path, `${API_BASE_URL}/`);

  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value === undefined || value === null || value === '') {
        continue;
      }

      url.searchParams.set(key, String(value));
    }
  }

  return url.toString();
}

function serializeBody(body: unknown): BodyInit | undefined {
  if (body === undefined || body === null) {
    return undefined;
  }

  if (body instanceof FormData || typeof body === 'string' || body instanceof URLSearchParams) {
    return body;
  }

  return JSON.stringify(body);
}

async function parseResponseBody(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text) as unknown;
  } catch {
    return text;
  }
}

function normalizeApiError(payload: unknown, status: number): ApiErrorDetails | undefined {
  if (!payload || typeof payload !== 'object') {
    return undefined;
  }

  const candidate = payload as Partial<ApiErrorDetails>;
  if (typeof candidate.message !== 'string') {
    return undefined;
  }

  return {
    timestamp: typeof candidate.timestamp === 'string' ? candidate.timestamp : undefined,
    status: typeof candidate.status === 'number' ? candidate.status : status,
    error: typeof candidate.error === 'string' ? candidate.error : undefined,
    message: candidate.message,
    path: typeof candidate.path === 'string' ? candidate.path : undefined,
    validationErrors: Array.isArray(candidate.validationErrors) ? candidate.validationErrors : undefined,
  };
}
