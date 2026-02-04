import { api } from './api';
import type { Wallet, CreditRequest, DebitRequest } from '../types';

export const walletService = {
  async getWallet(userId: string): Promise<Wallet> {
    const response = await api.get<Wallet>(`/api/wallet/${userId}`);
    return response.data;
  },

  async creditWallet(userId: string, request: CreditRequest): Promise<Wallet> {
    const response = await api.post<Wallet>(`/api/wallet/${userId}/credit`, request);
    return response.data;
  },

  async debitWallet(userId: string, request: DebitRequest): Promise<Wallet> {
    const response = await api.post<Wallet>(`/api/wallet/${userId}/debit`, request);
    return response.data;
  },
};







