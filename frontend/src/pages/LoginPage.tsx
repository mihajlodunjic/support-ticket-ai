import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ErrorMessage } from '../components/ErrorMessage';

interface LoginFormState {
  email: string;
  password: string;
}

const initialFormState: LoginFormState = {
  email: '',
  password: '',
};

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, login } = useAuth();
  const [form, setForm] = useState<LoginFormState>(initialFormState);
  const [error, setError] = useState<unknown>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/admin', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.email.trim() || !form.password) {
      setError(new Error('Email and password are required.'));
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await login({
        email: form.email.trim(),
        password: form.password,
      });
      navigate('/admin', { replace: true, state: { from: location.pathname } });
    } catch (loginError) {
      setError(loginError);
    } finally {
      setSubmitting(false);
    }
  }

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  }

  return (
    <div className="auth-layout">
      <section className="auth-card">
        <span className="section-kicker">Authentication</span>
        <h1>Admin login</h1>
        <p className="muted-text">Sign in to access your administrative dashboard and manage tickets.</p>

        {error ? <ErrorMessage error={error} /> : null}

        <form className="form-grid" onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              disabled={submitting}
              autoComplete="email"
            />
          </div>

          <div className="form-field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              disabled={submitting}
              autoComplete="current-password"
            />
          </div>

          <div className="form-actions">
            <button type="submit" className="button button--primary" disabled={submitting}>
              {submitting ? 'Signing in...' : 'Login'}
            </button>
          </div>
        </form>

        <p className="auth-footer">
          Need an account? <Link to="/register">Register here</Link>.
        </p>
      </section>
    </div>
  );
}
