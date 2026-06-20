import type { TicketStatus } from '../types/api';
import { formatEnumLabel } from '../utils/format';

const STATUS_TONE: Record<TicketStatus, string> = {
  NEW: 'badge--neutral',
  IN_PROGRESS: 'badge--teal',
  RESOLVED: 'badge--success',
  CLOSED: 'badge--dark',
};

export function StatusBadge({ status }: { status: TicketStatus }) {
  return <span className={`badge ${STATUS_TONE[status]}`}>{formatEnumLabel(status)}</span>;
}
