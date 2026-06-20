import type { ReactNode } from 'react';

interface EmptyStateProps {
  title: string;
  description: string;
  action?: ReactNode;
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="state-card">
      <h3>{title}</h3>
      <p>{description}</p>
      {action && <div className="state-card__action">{action}</div>}
    </div>
  );
}
