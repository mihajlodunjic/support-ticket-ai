import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getStatistics } from '../api/statisticsApi';
import { ApiClientError } from '../api/apiClient';
import { useAuth } from '../auth/AuthContext';
import { ErrorMessage } from '../components/ErrorMessage';
import { LoadingState } from '../components/LoadingState';
import type { StatisticsResponse } from '../types/api';
import { formatEnumLabel, formatPercent } from '../utils/format';

export function AdminDashboardPage() {
  const { logout } = useAuth();
  const [statistics, setStatistics] = useState<StatisticsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    let active = true;

    async function loadStatistics() {
      try {
        const response = await getStatistics();
        if (active) {
          setStatistics(response);
          setError(null);
        }
      } catch (requestError) {
        if (active) {
          setError(requestError);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void loadStatistics();

    return () => {
      active = false;
    };
  }, []);

  if (loading) {
    return <LoadingState label="Loading dashboard statistics..." />;
  }

  if (error) {
    return (
      <div className="page-stack">
        <section className="page-header">
          <div>
            <span className="section-kicker">Admin dashboard</span>
            <h1>Statistics overview</h1>
          </div>
        </section>
        <ErrorMessage title="Dashboard could not be loaded" error={error}>
          {isAuthorizationError(error) && (
            <>
              <Link to="/login" className="button button--secondary">
                Go to login
              </Link>
              <button type="button" className="button button--ghost" onClick={logout}>
                Logout
              </button>
            </>
          )}
        </ErrorMessage>
      </div>
    );
  }

  if (!statistics) {
    return null;
  }

  return (
    <div className="page-stack">
      <section className="page-header">
        <div>
          <span className="section-kicker">Admin dashboard</span>
          <h1>Statistics overview</h1>
          <p>Data comes from `GET /api/admin/statistics` with the exact backend response contract.</p>
        </div>
        <Link to="/admin/tickets" className="button button--primary">
          Open ticket queue
        </Link>
      </section>

      <section className="card-grid card-grid--three">
        <article className="metric-card">
          <span className="metric-card__label">Total tickets</span>
          <strong>{statistics.totalTickets}</strong>
        </article>
        <article className="metric-card">
          <span className="metric-card__label">Open tickets</span>
          <strong>{statistics.openTickets}</strong>
        </article>
        <article className="metric-card">
          <span className="metric-card__label">Closed tickets</span>
          <strong>{statistics.closedTickets}</strong>
        </article>
        <article className="metric-card">
          <span className="metric-card__label">Average confidence</span>
          <strong>{formatPercent(statistics.averageConfidence, 2)}</strong>
        </article>
        <article className="metric-card">
          <span className="metric-card__label">AI acceptance rate</span>
          <strong>{formatPercent(statistics.aiAcceptanceRate, 2)}</strong>
        </article>
        <article className="metric-card">
          <span className="metric-card__label">AI failed count</span>
          <strong>{statistics.aiFailedCount}</strong>
        </article>
      </section>

      <section className="card-grid card-grid--three">
        <DistributionCard title="Tickets by status" items={statistics.ticketsByStatus} />
        <DistributionCard title="Tickets by priority" items={statistics.ticketsByPriority} />
        <DistributionCard
          title="Tickets by category"
          items={statistics.ticketsByCategory}
          emptyMessage="No ticket categories have been recorded yet."
        />
      </section>
    </div>
  );
}

function DistributionCard({
  title,
  items,
  emptyMessage = 'No data is available.',
}: {
  title: string;
  items: Record<string, number>;
  emptyMessage?: string;
}) {
  const entries = Object.entries(items).sort((left, right) => right[1] - left[1] || left[0].localeCompare(right[0]));
  const hasValues = entries.length > 0 && entries.some(([, value]) => value > 0);
  const maxValue = Math.max(...entries.map(([, value]) => value), 1);

  return (
    <article className="card">
      <h2>{title}</h2>
      {!hasValues ? (
        <p className="muted-text">{emptyMessage}</p>
      ) : (
        <div className="distribution-list">
          {entries.map(([label, value]) => (
            <div key={label} className="distribution-row">
              <div className="distribution-row__labels">
                <span>{formatDistributionLabel(label)}</span>
                <strong>{value}</strong>
              </div>
              <div className="distribution-bar">
                <span style={{ width: `${(value / maxValue) * 100}%` }} />
              </div>
            </div>
          ))}
        </div>
      )}
    </article>
  );
}

function isAuthorizationError(error: unknown): boolean {
  return error instanceof ApiClientError && (error.status === 401 || error.status === 403);
}

function formatDistributionLabel(label: string): string {
  return /^[A-Z0-9_]+$/.test(label) ? formatEnumLabel(label) : label;
}
