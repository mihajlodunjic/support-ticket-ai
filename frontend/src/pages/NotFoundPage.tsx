import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div className="auth-layout">
      <section className="auth-card">
        <span className="section-kicker">404</span>
        <h1>Page not found</h1>
        <p className="muted-text">The route you requested does not exist in the frontend SPA.</p>
        <div className="form-actions">
          <Link to="/" className="button button--primary">
            Back to home
          </Link>
        </div>
      </section>
    </div>
  );
}
