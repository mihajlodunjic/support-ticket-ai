import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getHealth } from '../api/ticketsApi';
import { ErrorMessage } from '../components/ErrorMessage';
import type { HealthResponse } from '../types/api';

export function HomePage() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [healthError, setHealthError] = useState<unknown>(null);
  const [checkingHealth, setCheckingHealth] = useState(true);

  useEffect(() => {
    let active = true;

    async function loadHealth() {
      try {
        const response = await getHealth();
        if (active) {
          setHealth(response);
          setHealthError(null);
        }
      } catch (error) {
        if (active) {
          setHealthError(error);
        }
      } finally {
        if (active) {
          setCheckingHealth(false);
        }
      }
    }

    void loadHealth();

    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="page-stack">
      <section className="hero-card">
        <div className="hero-card__content">
          <span className="section-kicker">Intelligent helpdesk</span>
          <h1>Support Ticket AI</h1>
          <p className="lead">
            Create support tickets, inspect AI category suggestions, and manage the admin workflow from a
            single React SPA wired to the Spring Boot backend.
          </p>

          <div className="hero-actions">
            <Link to="/tickets/new" className="button button--primary">
              Create ticket
            </Link>
            <Link to="/login" className="button button--secondary">
              Admin login
            </Link>
            <Link to="/predict-test" className="button button--ghost">
              Test prediction
            </Link>
          </div>
        </div>

        <aside className="hero-card__side">
          <div className="info-panel">
            <h2>Process</h2>
            <ol className="steps-list">
              <li>User reports an IT issue with title, description and email.</li>
              <li>Backend calls AI prediction and falls back safely when AI is unavailable.</li>
              <li>Support admin reviews, updates and closes the ticket.</li>
            </ol>
          </div>

          <div className="info-panel">
            <h2>Backend health</h2>
            {checkingHealth && <p className="muted-text">Checking `GET /api/health`...</p>}
            {!checkingHealth && health && (
              <div className="health-chip health-chip--ok">
                <span>{health.status}</span>
                <span>{health.service}</span>
              </div>
            )}
            {!checkingHealth && healthError ? (
              <ErrorMessage
                title="Health check unavailable"
                message="The frontend is configured, but the backend did not answer the health request."
                error={healthError}
              />
            ) : null}
          </div>
        </aside>
      </section>

      <section className="card-grid card-grid--three">
        <article className="card">
          <h2>Public ticket intake</h2>
          <p>Use the public form to create new incidents and show predicted category, confidence and fallback state.</p>
        </article>
        <article className="card">
          <h2>JWT-protected admin area</h2>
          <p>Login stores a JWT in local storage and sends it as a Bearer token for `/api/admin/**` endpoints.</p>
        </article>
        <article className="card">
          <h2>Direct AI probe</h2>
          <p>Use the predict test page for quick manual checks against `POST /api/predict` when the AI service is up.</p>
        </article>
      </section>
    </div>
  );
}
