import { apiRequest } from './apiClient';
import type {
  CreateTicketRequest,
  CreateTicketResponse,
  HealthResponse,
  PageResponse,
  Ticket,
  TicketListItem,
  TicketQueryParams,
  UpdateTicketRequest,
} from '../types/api';

export function getHealth(): Promise<HealthResponse> {
  return apiRequest<HealthResponse>('/api/health');
}

export function createTicket(payload: CreateTicketRequest): Promise<CreateTicketResponse> {
  return apiRequest<CreateTicketResponse>('/api/tickets', {
    method: 'POST',
    body: payload,
  });
}

export function getAdminTickets(params: TicketQueryParams): Promise<PageResponse<TicketListItem>> {
  return apiRequest<PageResponse<TicketListItem>>('/api/admin/tickets', {
    query: {
      page: params.page,
      size: params.size,
      status: params.status,
      priority: params.priority,
      predictedCategory: params.predictedCategory,
      finalCategory: params.finalCategory,
      userEmail: params.userEmail,
      createdFrom: params.createdFrom,
      createdTo: params.createdTo,
      minConfidence: params.minConfidence,
      maxConfidence: params.maxConfidence,
      sort: params.sort,
    },
  });
}

export function getAdminTicketById(id: number): Promise<Ticket> {
  return apiRequest<Ticket>(`/api/admin/tickets/${id}`);
}

export function updateAdminTicket(id: number, payload: UpdateTicketRequest): Promise<Ticket> {
  return apiRequest<Ticket>(`/api/admin/tickets/${id}`, {
    method: 'PUT',
    body: payload,
  });
}
