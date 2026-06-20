import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { formatEnumLabel } from '../utils/format';

export function Navbar() {
  const { currentUser, isAuthenticated, logout } = useAuth();
  const showAdminLinks = currentUser?.role === 'ADMIN';

  return (
    <header className="topbar">
      <div className="topbar__content">
        <div className="brand">
          <NavLink to="/" className="brand__link">
            <span className="brand__eyebrow">Support</span>
            <span className="brand__title">Ticket AI</span>
          </NavLink>
        </div>

        <nav className="nav-links" aria-label="Primary navigation">
          <NavLink to="/" className={getNavLinkClassName} end>
            Overview
          </NavLink>
          <NavLink to="/tickets/new" className={getNavLinkClassName}>
            New ticket
          </NavLink>
          <NavLink to="/predict-test" className={getNavLinkClassName}>
            Predict test
          </NavLink>
          {showAdminLinks && (
            <>
              <NavLink to="/admin" className={getNavLinkClassName} end>
                Dashboard
              </NavLink>
              <NavLink to="/admin/tickets" className={getNavLinkClassName}>
                Tickets
              </NavLink>
            </>
          )}
          {!isAuthenticated && (
            <>
              <NavLink to="/login" className={getNavLinkClassName}>
                Login
              </NavLink>
              <NavLink to="/register" className={getNavLinkClassName}>
                Register
              </NavLink>
            </>
          )}
        </nav>

        <div className="topbar__session">
          {isAuthenticated ? (
            <>
              <div className="session-chip">
                <span className="session-chip__label">
                  {currentUser ? currentUser.name : 'Authenticated user'}
                </span>
                {currentUser && (
                  <span className="session-chip__meta">
                    {formatEnumLabel(currentUser.role)} · {currentUser.email}
                  </span>
                )}
              </div>
              <button type="button" className="button button--ghost" onClick={logout}>
                Logout
              </button>
            </>
          ) : (
            <span className="topbar__hint">Admin routes require a JWT token.</span>
          )}
        </div>
      </div>
    </header>
  );
}

function getNavLinkClassName({ isActive }: { isActive: boolean }) {
  return isActive ? 'nav-link nav-link--active' : 'nav-link';
}
