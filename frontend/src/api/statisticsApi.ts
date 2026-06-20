import { apiRequest } from './apiClient';
import type { StatisticsResponse } from '../types/api';

export function getStatistics(): Promise<StatisticsResponse> {
  return apiRequest<StatisticsResponse>('/api/admin/statistics');
}
