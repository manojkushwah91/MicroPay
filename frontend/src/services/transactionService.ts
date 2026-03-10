import { api } from './api';
import type { Transaction } from '../types';

export const transactionService = {
  async getTransactions(userId: string): Promise<Transaction[]> {
    const response = await api.get<Transaction[]>(`/transactions/${userId}`);
    return response.data;
  },

  async getTransaction(transactionId: string): Promise<Transaction> {
    const response = await api.get<Transaction>(`/transaction/${transactionId}`);
    return response.data;
  },
};







