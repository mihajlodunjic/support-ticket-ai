import { apiRequest } from './apiClient';
import type { PredictRequest, PredictResponse } from '../types/api';

export function predictTicketText(payload: PredictRequest): Promise<PredictResponse> {
  return apiRequest<PredictResponse>('/api/predict', {
    method: 'POST',
    body: payload,
  });
}
