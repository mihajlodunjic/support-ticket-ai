import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { ApiClientError } from '../api/apiClient';
import { getAdminTickets } from '../api/ticketsApi';
import { useAuth } from '../auth/AuthContext';
import { ConfidenceBadge } from '../components/ConfidenceBadge';
import { EmptyState } from '../components/EmptyState';
import { ErrorMessage } from '../components/ErrorMessage';
import { LoadingState } from '../components/LoadingState';
import { PriorityBadge } from '../components/PriorityBadge';
import { StatusBadge } from '../components/StatusBadge';
import type { PageResponse, TicketFilters, TicketListItem, TicketPriority, TicketQueryParams, TicketStatus } from '../types/api';
import { SORT_OPTIONS, TICKET_CATEGORIES, TICKET_PRIORITIES, TICKET_STATUSES } from '../types/api';
import { formatDateTime } from '../utils/format';
import { toIsoStringOrUndefined } from '../utils/format';

const initialFilters: TicketFilters = {
  status: '',
  priority: '',
  predictedCategory: '',
  finalCategory: '',
  userEmail: '',
  createdFrom: '',
  createdTo: '',
  minConfidence: '',
  maxConfidence: '',
  sort: 'createdAt,desc',
};

export function AdminTicketsPage() {
  const { logout } = useAuth();
  const [filters, setFilters] = useState<TicketFilters>(initialFilters);
  const [ticketPage, setTicketPage] = useState<PageResponse<TicketListItem> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    void fetchTickets(initialFilters, 0);
  }, []);

  async function fetchTickets(activeFilters: TicketFilters, page: number) {
    const validationError = validateFilterRanges(activeFilters);
    if (validationError) {
      setError(new Error(validationError));
      setLoading(false);
      return;
    }

    setLoading(true);

    try {
      const response = await getAdminTickets(buildQueryParams(activeFilters, page));
      setTicketPage(response);
      setError(null);
    } catch (requestError) {
      setError(requestError);
    } finally {
      setLoading(false);
    }
  }

  function handleFilterChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target;
    setFilters((current) => ({
      ...current,
      [name]: value,
    }));
  }

  function handleApplyFilters(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void fetchTickets(filters, 0);
  }

  function handleClearFilters() {
    setFilters(initialFilters);
    void fetchTickets(initialFilters, 0);
  }

  function handlePrevious() {
    if (!ticketPage || ticketPage.first) {
      return;
    }

    void fetchTickets(filters, ticketPage.page - 1);
  }

  function handleNext() {
    if (!ticketPage || ticketPage.last) {
      return;
    }

    void fetchTickets(filters, ticketPage.page + 1);
  }

  const hasTickets = Boolean(ticketPage && ticketPage.content.length > 0);

  return (
    <div className="page-stack">
      <section className="page-header">
        <div>
          <span className="section-kicker">Admin queue</span>
          <h1>Ticket list</h1>
          <p>Filters are mapped to the actual backend `TicketFilterRequest` and Spring pageable query parameters.</p>
        </div>
      </section>

      <section className="card">
        <form className="filter-grid" onSubmit={handleApplyFilters}>
          <div className="form-field">
            <label htmlFor="status">Status</label>
            <select id="status" name="status" value={filters.status} onChange={handleFilterChange}>
              <option value="">All statuses</option>
              {TICKET_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="priority">Priority</label>
            <select id="priority" name="priority" value={filters.priority} onChange={handleFilterChange}>
              <option value="">All priorities</option>
              {TICKET_PRIORITIES.map((priority) => (
                <option key={priority} value={priority}>
                  {priority}
                </option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="predictedCategory">Predicted category</label>
            <select
              id="predictedCategory"
              name="predictedCategory"
              value={filters.predictedCategory}
              onChange={handleFilterChange}
            >
              <option value="">All predicted categories</option>
              {TICKET_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="finalCategory">Final category</label>
            <select id="finalCategory" name="finalCategory" value={filters.finalCategory} onChange={handleFilterChange}>
              <option value="">All final categories</option>
              {TICKET_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="userEmail">User email</label>
            <input
              id="userEmail"
              name="userEmail"
              value={filters.userEmail}
              onChange={handleFilterChange}
              placeholder="Search by email fragment"
            />
          </div>

          <div className="form-field">
            <label htmlFor="createdFrom">Created from</label>
            <input
              id="createdFrom"
              name="createdFrom"
              type="datetime-local"
              value={filters.createdFrom}
              onChange={handleFilterChange}
            />
          </div>

          <div className="form-field">
            <label htmlFor="createdTo">Created to</label>
            <input
              id="createdTo"
              name="createdTo"
              type="datetime-local"
              value={filters.createdTo}
              onChange={handleFilterChange}
            />
          </div>

          <div className="form-field">
            <label htmlFor="minConfidence">Min confidence</label>
            <input
              id="minConfidence"
              name="minConfidence"
              type="number"
              min="0"
              max="1"
              step="0.01"
              value={filters.minConfidence}
              onChange={handleFilterChange}
              placeholder="0.00"
            />
          </div>

          <div className="form-field">
            <label htmlFor="maxConfidence">Max confidence</label>
            <input
              id="maxConfidence"
              name="maxConfidence"
              type="number"
              min="0"
              max="1"
              step="0.01"
              value={filters.maxConfidence}
              onChange={handleFilterChange}
              placeholder="1.00"
            />
          </div>

          <div className="form-field">
            <label htmlFor="sort">Sort</label>
            <select id="sort" name="sort" value={filters.sort} onChange={handleFilterChange}>
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-actions form-actions--inline">
            <button type="submit" className="button button--primary" disabled={loading}>
              Apply filters
            </button>
            <button type="button" className="button button--secondary" onClick={handleClearFilters} disabled={loading}>
              Clear filters
            </button>
          </div>
        </form>
      </section>

      {loading && <LoadingState label="Loading tickets..." />}

      {!loading && error ? (
        <ErrorMessage title="Ticket list could not be loaded" error={error}>
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
      ) : null}

      {!loading && !error && !hasTickets && (
        <EmptyState
          title="No tickets found"
          description="Adjust the filters or wait until the backend has ticket data."
        />
      )}

      {!loading && !error && hasTickets && ticketPage && (
        <section className="card">
          <div className="table-toolbar">
            <p className="muted-text">
              Showing {ticketPage.content.length} of {ticketPage.totalElements} tickets.
            </p>
          </div>

          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Email</th>
                  <th>Predicted</th>
                  <th>Confidence</th>
                  <th>Final</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>AI accepted</th>
                  <th>AI failed</th>
                  <th>Created</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {ticketPage.content.map((ticket) => (
                  <tr key={ticket.id}>
                    <td>#{ticket.id}</td>
                    <td>{ticket.title}</td>
                    <td>{ticket.userEmail}</td>
                    <td>{ticket.predictedCategory}</td>
                    <td>
                      <ConfidenceBadge confidence={ticket.confidence} />
                    </td>
                    <td>{ticket.finalCategory}</td>
                    <td>
                      <PriorityBadge priority={ticket.priority} />
                    </td>
                    <td>
                      <StatusBadge status={ticket.status} />
                    </td>
                    <td>
                      <span className={ticket.aiAccepted ? 'inline-pill inline-pill--success' : 'inline-pill'}>
                        {ticket.aiAccepted ? 'Yes' : 'No'}
                      </span>
                    </td>
                    <td>
                      <span className={ticket.aiFailed ? 'inline-pill inline-pill--warning' : 'inline-pill'}>
                        {ticket.aiFailed ? 'Yes' : 'No'}
                      </span>
                    </td>
                    <td>{formatDateTime(ticket.createdAt)}</td>
                    <td>
                      <Link to={`/admin/tickets/${ticket.id}`} className="button-link">
                        Details
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <button type="button" className="button button--secondary" onClick={handlePrevious} disabled={ticketPage.first}>
              Previous
            </button>
            <span>
              Page {ticketPage.page + 1} of {Math.max(ticketPage.totalPages, 1)}
            </span>
            <button type="button" className="button button--secondary" onClick={handleNext} disabled={ticketPage.last}>
              Next
            </button>
          </div>
        </section>
      )}
    </div>
  );
}

function buildQueryParams(filters: TicketFilters, page: number): TicketQueryParams {
  return {
    page,
    size: 20,
    status: filters.status ? (filters.status as TicketStatus) : undefined,
    priority: filters.priority ? (filters.priority as TicketPriority) : undefined,
    predictedCategory: filters.predictedCategory || undefined,
    finalCategory: filters.finalCategory || undefined,
    userEmail: filters.userEmail.trim() || undefined,
    createdFrom: toIsoStringOrUndefined(filters.createdFrom),
    createdTo: toIsoStringOrUndefined(filters.createdTo),
    minConfidence: filters.minConfidence ? Number(filters.minConfidence) : undefined,
    maxConfidence: filters.maxConfidence ? Number(filters.maxConfidence) : undefined,
    sort: filters.sort || undefined,
  };
}

function validateFilterRanges(filters: TicketFilters): string | null {
  const minConfidence = filters.minConfidence ? Number(filters.minConfidence) : undefined;
  const maxConfidence = filters.maxConfidence ? Number(filters.maxConfidence) : undefined;

  if (minConfidence !== undefined && (Number.isNaN(minConfidence) || minConfidence < 0 || minConfidence > 1)) {
    return 'Minimum confidence must be between 0 and 1.';
  }

  if (maxConfidence !== undefined && (Number.isNaN(maxConfidence) || maxConfidence < 0 || maxConfidence > 1)) {
    return 'Maximum confidence must be between 0 and 1.';
  }

  if (minConfidence !== undefined && maxConfidence !== undefined && minConfidence > maxConfidence) {
    return 'Minimum confidence must be less than or equal to maximum confidence.';
  }

  if (filters.createdFrom && filters.createdTo) {
    const createdFrom = new Date(filters.createdFrom);
    const createdTo = new Date(filters.createdTo);

    if (createdFrom.getTime() > createdTo.getTime()) {
      return 'Created from must be before or equal to created to.';
    }
  }

  return null;
}

function isAuthorizationError(error: unknown): boolean {
  return error instanceof ApiClientError && (error.status === 401 || error.status === 403);
}
