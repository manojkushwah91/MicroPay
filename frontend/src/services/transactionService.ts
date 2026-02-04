import { api } from './api';
import type { Transaction } from '../types';

export const transactionService = {
  async getTransactions(userId: string): Promise<Transaction[]> {
    const response = await api.get<Transaction[]>(`/api/transactions/${userId}`);
    return response.data;
  },

  async getTransaction(transactionId: string): Promise<Transaction> {
    const response = await api.get<Transaction>(`/api/transaction/${transactionId}`);
    return response.data;
  },
};







