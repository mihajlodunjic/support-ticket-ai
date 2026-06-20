import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { getCurrentUser, loginUser } from '../api/authApi';
import { ApiClientError } from '../api/apiClient';
import { clearAuthToken, getAuthToken, setAuthToken } from './authStorage';
import type { CurrentUser, LoginRequest } from '../types/api';

interface AuthContextValue {
  token: string | null;
  currentUser: CurrentUser | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  refreshCurrentUser: () => Promise<CurrentUser | null>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getAuthToken());
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [isInitializing, setIsInitializing] = useState<boolean>(Boolean(getAuthToken()));

  useEffect(() => {
    let active = true;

    async function bootstrapAuth() {
      const storedToken = getAuthToken();
      if (!storedToken) {
        if (active) {
          setIsInitializing(false);
        }
        return;
      }

      try {
        const user = await getCurrentUser();
        if (active) {
          setCurrentUser(user);
        }
      } catch (error) {
        if (active) {
          if (error instanceof ApiClientError && (error.status === 401 || error.status === 403)) {
            clearAuthToken();
            setToken(null);
          }
          setCurrentUser(null);
        }
      } finally {
        if (active) {
          setIsInitializing(false);
        }
      }
    }

    bootstrapAuth();

    return () => {
      active = false;
    };
  }, []);

  async function login(credentials: LoginRequest): Promise<void> {
    const response = await loginUser(credentials);
    setAuthToken(response.token);
    setToken(response.token);
    setIsInitializing(false);

    try {
      const user = await getCurrentUser();
      setCurrentUser(user);
    } catch (error) {
      if (error instanceof ApiClientError && (error.status === 401 || error.status === 403)) {
        clearAuthToken();
        setToken(null);
      }
      setCurrentUser(null);
    }
  }

  function logout(): void {
    clearAuthToken();
    setToken(null);
    setCurrentUser(null);
    setIsInitializing(false);
  }

  async function refreshCurrentUser(): Promise<CurrentUser | null> {
    if (!getAuthToken()) {
      setCurrentUser(null);
      return null;
    }

    try {
      const user = await getCurrentUser();
      setCurrentUser(user);
      return user;
    } catch (error) {
      if (error instanceof ApiClientError && (error.status === 401 || error.status === 403)) {
        logout();
      }
      throw error;
    }
  }

  return (
    <AuthContext.Provider
      value={{
        token,
        currentUser,
        isAuthenticated: Boolean(token),
        isInitializing,
        login,
        logout,
        refreshCurrentUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider.');
  }

  return context;
}
