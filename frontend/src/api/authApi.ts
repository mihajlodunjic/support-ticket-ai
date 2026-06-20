import { apiRequest } from './apiClient';
import type { CurrentUser, LoginRequest, LoginResponse, RegisterRequest } from '../types/api';

export function registerUser(payload: RegisterRequest): Promise<CurrentUser> {
  return apiRequest<CurrentUser>('/api/auth/register', {
    method: 'POST',
    body: payload,
  });
}

export function loginUser(payload: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: payload,
  });
}

export function getCurrentUser(): Promise<CurrentUser> {
  return apiRequest<CurrentUser>('/api/auth/me');
}
