import { useState, type ChangeEvent, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { registerUser } from '../api/authApi';
import { ErrorMessage } from '../components/ErrorMessage';
import type { CurrentUser } from '../types/api';

interface RegisterFormState {
  name: string;
  email: string;
  password: string;
}

const initialFormState: RegisterFormState = {
  name: '',
  email: '',
  password: '',
};

export function RegisterPage() {
  const [form, setForm] = useState<RegisterFormState>(initialFormState);
  const [createdUser, setCreatedUser] = useState<CurrentUser | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const validationMessage = validateRegisterForm(form);
    if (validationMessage) {
      setError(new Error(validationMessage));
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const response = await registerUser({
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      setCreatedUser(response);
    } catch (submitError) {
      setError(submitError);
      setCreatedUser(null);
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
        <h1>Create an account</h1>
        <p className="muted-text">`POST /api/auth/register` creates a user account. It does not log you in automatically.</p>

        {error ? <ErrorMessage error={error} /> : null}

        {createdUser && (
          <div className="alert alert--success">
            <div className="alert__title">Registration successful</div>
            <p className="alert__message">
              Account <strong>{createdUser.email}</strong> was created with role <strong>{createdUser.role}</strong>.
            </p>
          </div>
        )}

        <form className="form-grid" onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="name">Name</label>
            <input id="name" name="name" value={form.name} onChange={handleChange} disabled={submitting} />
          </div>

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
              autoComplete="new-password"
            />
            <span className="field-hint">Use at least 8 characters.</span>
          </div>

          <div className="form-actions">
            <button type="submit" className="button button--primary" disabled={submitting}>
              {submitting ? 'Creating account...' : 'Register'}
            </button>
          </div>
        </form>

        <p className="auth-footer">
          Already registered? <Link to="/login">Go to login</Link>.
        </p>
      </section>
    </div>
  );
}

function validateRegisterForm(form: RegisterFormState): string | null {
  if (!form.name.trim()) {
    return 'Name is required.';
  }

  if (!form.email.trim()) {
    return 'Email is required.';
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    return 'Provide a valid email address.';
  }

  if (form.password.length < 8) {
    return 'Password must be at least 8 characters long.';
  }

  return null;
}
