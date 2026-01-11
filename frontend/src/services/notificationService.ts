import { api } from './api';
import type { Notification } from '../types';

export const notificationService = {
  async getNotifications(userId: string, page: number = 0, size: number = 20): Promise<Notification[]> {
    const response = await api.get<Notification[]>(`/api/notifications/${userId}`, {
      params: { page, size },
    });
    return response.data;
  },
};




