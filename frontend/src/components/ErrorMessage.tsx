import type { ReactNode } from 'react';
import { ApiClientError } from '../api/apiClient';

interface ErrorMessageProps {
  title?: string;
  message?: string;
  error?: unknown;
  children?: ReactNode;
}

export function ErrorMessage({ title = 'Request failed', message, error, children }: ErrorMessageProps) {
  const resolvedMessage = resolveMessage(error, message);
  const validationErrors = error instanceof ApiClientError ? error.details?.validationErrors ?? [] : [];

  return (
    <div className="alert alert--error" role="alert">
      <div className="alert__title">{title}</div>
      <p className="alert__message">{resolvedMessage}</p>
      {validationErrors.length > 0 && (
        <ul className="alert__list">
          {validationErrors.map((item) => (
            <li key={`${item.field}-${item.message}`}>
              <strong>{item.field}</strong>: {item.message}
            </li>
          ))}
        </ul>
      )}
      {children && <div className="alert__actions">{children}</div>}
    </div>
  );
}

function resolveMessage(error: unknown, fallback?: string): string {
  if (fallback) {
    return fallback;
  }

  if (error instanceof ApiClientError) {
    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'An unexpected error occurred.';
}
