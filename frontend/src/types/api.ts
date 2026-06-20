export type TicketStatus = 'NEW' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type UserRole = 'USER' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BLOCKED';

export const TICKET_STATUSES: TicketStatus[] = ['NEW', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
export const TICKET_PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH'];
export const TICKET_CATEGORIES = [
  'Access',
  'Administrative rights',
  'HR Support',
  'Hardware',
  'Internal Project',
  'Miscellaneous',
  'Purchase',
  'Storage',
] as const;

export const SORT_OPTIONS = [
  { value: 'createdAt,desc', label: 'Newest first' },
  { value: 'createdAt,asc', label: 'Oldest first' },
  { value: 'confidence,desc', label: 'Confidence high to low' },
  { value: 'confidence,asc', label: 'Confidence low to high' },
] as const;

export interface ValidationErrorItem {
  field: string;
  message: string;
}

export interface ApiErrorDetails {
  timestamp?: string;
  status: number;
  error?: string;
  message: string;
  path?: string;
  validationErrors?: ValidationErrorItem[];
}

export interface TopPrediction {
  category: string;
  probability: number;
  rank?: number;
}

export interface Ticket {
  id: number;
  title: string;
  description: string;
  userEmail: string;
  notes: string | null;
  predictedCategory: string;
  confidence: number;
  finalCategory: string;
  priority: TicketPriority;
  status: TicketStatus;
  aiAccepted: boolean;
  aiFailed: boolean;
  aiErrorMessage: string | null;
  topPredictions: TopPrediction[];
  createdAt: string;
  updatedAt: string;
}

export interface TicketListItem {
  id: number;
  title: string;
  userEmail: string;
  predictedCategory: string;
  confidence: number;
  finalCategory: string;
  priority: TicketPriority;
  status: TicketStatus;
  aiAccepted: boolean;
  aiFailed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface StatisticsResponse {
  totalTickets: number;
  openTickets: number;
  closedTickets: number;
  averageConfidence: number;
  aiAcceptanceRate: number;
  aiFailedCount: number;
  ticketsByStatus: Record<string, number>;
  ticketsByPriority: Record<string, number>;
  ticketsByCategory: Record<string, number>;
}

export interface HealthResponse {
  status: string;
  service: string;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  userEmail: string;
  notes?: string;
}

export type CreateTicketResponse = Ticket;

export interface UpdateTicketRequest {
  finalCategory?: string;
  priority?: TicketPriority;
  status?: TicketStatus;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface CurrentUser {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  status: UserStatus;
}

export interface PredictRequest {
  text: string;
}

export interface PredictResponse {
  predictedCategory: string;
  confidence: number | null;
  topPredictions: TopPrediction[];
}

export interface TicketFilters {
  status: string;
  priority: string;
  predictedCategory: string;
  finalCategory: string;
  userEmail: string;
  createdFrom: string;
  createdTo: string;
  minConfidence: string;
  maxConfidence: string;
  sort: string;
}

export interface TicketQueryParams {
  page?: number;
  size?: number;
  status?: TicketStatus;
  priority?: TicketPriority;
  predictedCategory?: string;
  finalCategory?: string;
  userEmail?: string;
  createdFrom?: string;
  createdTo?: string;
  minConfidence?: number;
  maxConfidence?: number;
  sort?: string;
}
