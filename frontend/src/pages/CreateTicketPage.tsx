import { useState, type ChangeEvent, type FormEvent } from 'react';
import { createTicket } from '../api/ticketsApi';
import { ConfidenceBadge } from '../components/ConfidenceBadge';
import { ErrorMessage } from '../components/ErrorMessage';
import { PriorityBadge } from '../components/PriorityBadge';
import { StatusBadge } from '../components/StatusBadge';
import type { CreateTicketRequest, Ticket } from '../types/api';
import { formatDateTime } from '../utils/format';

interface TicketFormState {
  title: string;
  description: string;
  userEmail: string;
  notes: string;
}

interface TicketFormErrors {
  title?: string;
  description?: string;
  userEmail?: string;
}

const initialFormState: TicketFormState = {
  title: '',
  description: '',
  userEmail: '',
  notes: '',
};

export function CreateTicketPage() {
  const [form, setForm] = useState<TicketFormState>(initialFormState);
  const [validationErrors, setValidationErrors] = useState<TicketFormErrors>({});
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [createdTicket, setCreatedTicket] = useState<Ticket | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextValidationErrors = validateTicketForm(form);
    setValidationErrors(nextValidationErrors);

    if (Object.keys(nextValidationErrors).length > 0) {
      return;
    }

    setSubmitting(true);
    setError(null);

    const payload: CreateTicketRequest = {
      title: form.title.trim(),
      description: form.description.trim(),
      userEmail: form.userEmail.trim(),
      notes: form.notes.trim() || undefined,
    };

    try {
      const response = await createTicket(payload);
      setCreatedTicket(response);
    } catch (submitError) {
      setError(submitError);
      setCreatedTicket(null);
    } finally {
      setSubmitting(false);
    }
  }

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const { name, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: value,
    }));
    setValidationErrors((current) => ({
      ...current,
      [name]: undefined,
    }));
  }

  return (
    <div className="page-stack">
      <section className="page-header">
        <div>
          <span className="section-kicker">Public route</span>
          <h1>Create a support ticket</h1>
          <p>Tickets are submitted to `POST /api/tickets`, which already includes AI prediction and fallback handling.</p>
        </div>
      </section>

      <div className="content-grid content-grid--wide">
        <section className="card">
          <form className="form-grid" onSubmit={handleSubmit} noValidate>
            <div className="form-field">
              <label htmlFor="title">Title</label>
              <input
                id="title"
                name="title"
                value={form.title}
                onChange={handleChange}
                disabled={submitting}
              />
              {validationErrors.title && <span className="field-error">{validationErrors.title}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                value={form.description}
                onChange={handleChange}
                rows={8}
                disabled={submitting}
              />
              {validationErrors.description && <span className="field-error">{validationErrors.description}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="userEmail">User email</label>
              <input
                id="userEmail"
                name="userEmail"
                type="email"
                value={form.userEmail}
                onChange={handleChange}
                disabled={submitting}
              />
              {validationErrors.userEmail && <span className="field-error">{validationErrors.userEmail}</span>}
            </div>

            <div className="form-field">
              <label htmlFor="notes">Notes (optional)</label>
              <textarea
                id="notes"
                name="notes"
                value={form.notes}
                onChange={handleChange}
                rows={4}
                disabled={submitting}
              />
            </div>

            <div className="form-actions">
              <button type="submit" className="button button--primary" disabled={submitting}>
                {submitting ? 'Submitting...' : 'Create ticket'}
              </button>
            </div>
          </form>
        </section>

        <section className="card">
          <h2>Submission result</h2>
          <p className="muted-text">Successful responses expose the AI prediction, final category and support status.</p>

          {error ? <ErrorMessage error={error} /> : null}

          {!error && !createdTicket && (
            <div className="placeholder-box">
              <p>Submit the form to see the created ticket response here.</p>
            </div>
          )}

          {createdTicket && (
            <div className="result-stack">
              <div className="result-grid">
                <div>
                  <span className="data-label">Ticket ID</span>
                  <strong>#{createdTicket.id}</strong>
                </div>
                <div>
                  <span className="data-label">Title</span>
                  <strong>{createdTicket.title}</strong>
                </div>
                <div>
                  <span className="data-label">Predicted category</span>
                  <strong>{createdTicket.predictedCategory}</strong>
                </div>
                <div>
                  <span className="data-label">Confidence</span>
                  <ConfidenceBadge confidence={createdTicket.confidence} />
                </div>
                <div>
                  <span className="data-label">Final category</span>
                  <strong>{createdTicket.finalCategory}</strong>
                </div>
                <div>
                  <span className="data-label">Priority</span>
                  <PriorityBadge priority={createdTicket.priority} />
                </div>
                <div>
                  <span className="data-label">Status</span>
                  <StatusBadge status={createdTicket.status} />
                </div>
                <div>
                  <span className="data-label">AI accepted</span>
                  <span className={createdTicket.aiAccepted ? 'inline-pill inline-pill--success' : 'inline-pill'}>
                    {createdTicket.aiAccepted ? 'Yes' : 'No'}
                  </span>
                </div>
              </div>

              {(createdTicket.aiFailed || createdTicket.aiErrorMessage) && (
                <div className="alert alert--warning">
                  <div className="alert__title">Fallback was used</div>
                  <p className="alert__message">{createdTicket.aiErrorMessage || 'AI prediction could not be completed.'}</p>
                </div>
              )}

              <div>
                <h3>Top predictions</h3>
                {createdTicket.topPredictions.length > 0 ? (
                  <ul className="prediction-list">
                    {createdTicket.topPredictions.map((prediction) => (
                      <li key={`${prediction.category}-${prediction.rank}`}>
                        <span>
                          #{prediction.rank} {prediction.category}
                        </span>
                        <strong>{(prediction.probability * 100).toFixed(1)}%</strong>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="muted-text">No ranked predictions were returned.</p>
                )}
              </div>

              <div className="meta-grid">
                <span>Created at: {formatDateTime(createdTicket.createdAt)}</span>
                <span>Updated at: {formatDateTime(createdTicket.updatedAt)}</span>
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

function validateTicketForm(form: TicketFormState): TicketFormErrors {
  const errors: TicketFormErrors = {};

  if (!form.title.trim()) {
    errors.title = 'Title is required.';
  }

  const description = form.description.trim();
  if (!description) {
    errors.description = 'Description is required.';
  } else if (description.length < 5) {
    errors.description = 'Description must be at least 5 characters long.';
  }

  if (!form.userEmail.trim()) {
    errors.userEmail = 'User email is required.';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.userEmail.trim())) {
    errors.userEmail = 'Provide a valid email address.';
  }

  return errors;
}
