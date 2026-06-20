import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiClientError } from '../api/apiClient';
import { getAdminTicketById, updateAdminTicket } from '../api/ticketsApi';
import { useAuth } from '../auth/AuthContext';
import { ConfidenceBadge } from '../components/ConfidenceBadge';
import { EmptyState } from '../components/EmptyState';
import { ErrorMessage } from '../components/ErrorMessage';
import { LoadingState } from '../components/LoadingState';
import { PriorityBadge } from '../components/PriorityBadge';
import { StatusBadge } from '../components/StatusBadge';
import { TICKET_CATEGORIES, TICKET_PRIORITIES, TICKET_STATUSES, type Ticket, type TicketPriority, type TicketStatus } from '../types/api';
import { formatDateTime } from '../utils/format';

interface TicketUpdateFormState {
  finalCategory: string;
  priority: TicketPriority;
  status: TicketStatus;
}

export function TicketDetailsPage() {
  const params = useParams<{ id: string }>();
  const { logout } = useAuth();
  const ticketId = Number(params.id);
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [form, setForm] = useState<TicketUpdateFormState | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [loadError, setLoadError] = useState<unknown>(null);
  const [updateError, setUpdateError] = useState<unknown>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!Number.isFinite(ticketId)) {
      setLoadError(new Error('Ticket id is invalid.'));
      setLoading(false);
      return;
    }

    let active = true;

    async function loadTicket() {
      try {
        const response = await getAdminTicketById(ticketId);
        if (active) {
          setTicket(response);
          setForm({
            finalCategory: response.finalCategory,
            priority: response.priority,
            status: response.status,
          });
          setLoadError(null);
        }
      } catch (requestError) {
        if (active) {
          setLoadError(requestError);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void loadTicket();

    return () => {
      active = false;
    };
  }, [ticketId]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form || !ticket) {
      return;
    }

    setSaving(true);
    setSuccessMessage(null);
    setUpdateError(null);

    try {
      const updatedTicket = await updateAdminTicket(ticket.id, {
        finalCategory: form.finalCategory,
        priority: form.priority,
        status: form.status,
      });
      setTicket(updatedTicket);
      setForm({
        finalCategory: updatedTicket.finalCategory,
        priority: updatedTicket.priority,
        status: updatedTicket.status,
      });
      setSuccessMessage('Ticket was updated successfully.');
    } catch (updateError) {
      setUpdateError(updateError);
      setSuccessMessage(null);
    } finally {
      setSaving(false);
    }
  }

  function handleChange(event: ChangeEvent<HTMLSelectElement>) {
    const { name, value } = event.target;
    setForm((current) => {
      if (!current) {
        return current;
      }

      return {
        ...current,
        [name]: value,
      } as TicketUpdateFormState;
    });
  }

  if (loading) {
    return <LoadingState label="Loading ticket details..." />;
  }

  if (loadError) {
    return (
      <div className="page-stack">
        <section className="page-header">
          <div>
            <span className="section-kicker">Admin ticket details</span>
            <h1>Ticket details</h1>
          </div>
        </section>
        <ErrorMessage title="Ticket could not be loaded" error={loadError}>
          {isAuthorizationError(loadError) && (
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

  if (!ticket || !form) {
    return <EmptyState title="Ticket not found" description="The requested ticket does not exist." />;
  }

  return (
    <div className="page-stack">
      <section className="page-header">
        <div>
          <span className="section-kicker">Admin ticket details</span>
          <h1>
            Ticket #{ticket.id}: {ticket.title}
          </h1>
          <p>Review the original report, AI result and admin-managed fields returned by the backend.</p>
        </div>
        <Link to="/admin/tickets" className="button button--secondary">
          Back to tickets
        </Link>
      </section>

      {successMessage && (
        <div className="alert alert--success">
          <div className="alert__title">Update completed</div>
          <p className="alert__message">{successMessage}</p>
        </div>
      )}

      {updateError ? <ErrorMessage title="Ticket update failed" error={updateError} /> : null}

      <div className="content-grid content-grid--wide">
        <section className="card">
          <h2>Original report</h2>
          <div className="detail-list">
            <div>
              <span className="data-label">Title</span>
              <p>{ticket.title}</p>
            </div>
            <div>
              <span className="data-label">Description</span>
              <p>{ticket.description}</p>
            </div>
            <div>
              <span className="data-label">User email</span>
              <p>{ticket.userEmail}</p>
            </div>
            <div>
              <span className="data-label">Notes</span>
              <p>{ticket.notes || 'No notes provided.'}</p>
            </div>
            <div>
              <span className="data-label">Created at</span>
              <p>{formatDateTime(ticket.createdAt)}</p>
            </div>
            <div>
              <span className="data-label">Updated at</span>
              <p>{formatDateTime(ticket.updatedAt)}</p>
            </div>
          </div>
        </section>

        <section className="card">
          <h2>AI result</h2>
          <div className="detail-list">
            <div>
              <span className="data-label">Predicted category</span>
              <p>{ticket.predictedCategory}</p>
            </div>
            <div>
              <span className="data-label">Confidence</span>
              <p>
                <ConfidenceBadge confidence={ticket.confidence} />
              </p>
            </div>
            <div>
              <span className="data-label">AI accepted</span>
              <p>
                <span className={ticket.aiAccepted ? 'inline-pill inline-pill--success' : 'inline-pill'}>
                  {ticket.aiAccepted ? 'Yes' : 'No'}
                </span>
              </p>
            </div>
            <div>
              <span className="data-label">AI failed</span>
              <p>
                <span className={ticket.aiFailed ? 'inline-pill inline-pill--warning' : 'inline-pill'}>
                  {ticket.aiFailed ? 'Yes' : 'No'}
                </span>
              </p>
            </div>
            <div>
              <span className="data-label">AI error</span>
              <p>{ticket.aiErrorMessage || 'No AI error message.'}</p>
            </div>
          </div>

          <div className="subsection">
            <h3>Top predictions</h3>
            {ticket.topPredictions.length > 0 ? (
              <ul className="prediction-list">
                {ticket.topPredictions.map((prediction) => (
                  <li key={`${prediction.category}-${prediction.rank}`}>
                    <span>
                      #{prediction.rank} {prediction.category}
                    </span>
                    <strong>{(prediction.probability * 100).toFixed(1)}%</strong>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="muted-text">No ranked predictions are stored for this ticket.</p>
            )}
          </div>
        </section>
      </div>

      <section className="card">
        <h2>Admin update</h2>
        <p className="muted-text">Only `finalCategory`, `priority` and `status` are editable, matching the backend update DTO.</p>

        <form className="filter-grid" onSubmit={handleSubmit}>
          <div className="form-field">
            <label htmlFor="finalCategory">Final category</label>
            <select id="finalCategory" name="finalCategory" value={form.finalCategory} onChange={handleChange}>
              {TICKET_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="priority">Priority</label>
            <select id="priority" name="priority" value={form.priority} onChange={handleChange}>
              {TICKET_PRIORITIES.map((priority) => (
                <option key={priority} value={priority}>
                  {priority}
                </option>
              ))}
            </select>
          </div>

          <div className="form-field">
            <label htmlFor="status">Status</label>
            <select id="status" name="status" value={form.status} onChange={handleChange}>
              {TICKET_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </div>

          <div className="form-actions form-actions--inline">
            <button type="submit" className="button button--primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save changes'}
            </button>
          </div>
        </form>

        <div className="detail-summary">
          <div>
            <span className="data-label">Current final category</span>
            <p>{ticket.finalCategory}</p>
          </div>
          <div>
            <span className="data-label">Current priority</span>
            <PriorityBadge priority={ticket.priority} />
          </div>
          <div>
            <span className="data-label">Current status</span>
            <StatusBadge status={ticket.status} />
          </div>
        </div>
      </section>
    </div>
  );
}

function isAuthorizationError(error: unknown): boolean {
  return error instanceof ApiClientError && (error.status === 401 || error.status === 403);
}
