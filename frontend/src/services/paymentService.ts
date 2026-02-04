import { api } from './api';
import type { Payment, PaymentRequest } from '../types';

export const paymentService = {
  async initiatePayment(request: PaymentRequest): Promise<Payment> {
    const response = await api.post<Payment>('/api/payment', request);
    return response.data;
  },

  async getPayment(paymentId: string): Promise<Payment> {
    const response = await api.get<Payment>(`/api/payment/${paymentId}`);
    return response.data;
  },
};







