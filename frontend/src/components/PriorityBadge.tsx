import type { TicketPriority } from '../types/api';

const PRIORITY_TONE: Record<TicketPriority, string> = {
  LOW: 'badge--success',
  MEDIUM: 'badge--warning',
  HIGH: 'badge--danger',
};

export function PriorityBadge({ priority }: { priority: TicketPriority }) {
  return <span className={`badge ${PRIORITY_TONE[priority]}`}>{priority}</span>;
}
