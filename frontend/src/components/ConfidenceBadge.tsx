import { formatPercent } from '../utils/format';

export function ConfidenceBadge({ confidence }: { confidence: number | null | undefined }) {
  let tone = 'badge--neutral';
  if (confidence !== null && confidence !== undefined) {
    if (confidence >= 0.8) {
      tone = 'badge--success';
    } else if (confidence >= 0.5) {
      tone = 'badge--warning';
    }
  }

  return <span className={`badge ${tone}`}>{formatPercent(confidence, 2)}</span>;
}
