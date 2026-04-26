import axios, { AxiosInstance, AxiosError } from 'axios';
import type { ApiError } from '../types';

// API Base URL - supports both Docker and local development
// In Docker, frontend is served by nginx and calls API Gateway via host network
// VITE_API_BASE_URL is set at build time via Docker build arg
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor to add JWT token
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor to handle errors
    this.api.interceptors.response.use(
      (response) => response,
      (error: AxiosError<ApiError>) => {
        if (error.response?.status === 401) {
          // Unauthorized - clear token and redirect to login
          localStorage.removeItem('token');
          localStorage.removeItem('userId');
          window.location.href = '/login';
        } else if (error.response) {
          // Handle different HTTP status codes with user-friendly messages
const status = error.response?.status;
const originalMessage = (error.response?.data as any)?.message || 'Unknown error';
let userFriendlyMessage = '';

// 1. Use the originalMessage for debugging so it's not "unused"
console.error(`[API Error] Status ${status}:`, originalMessage);

// 2. (Optional) You can also use it as a fallback if you don't have a specific friendly message
if (status >= 500) {
  userFriendlyMessage = 'Something went wrong on our end. Please try again later.';
} else {
  userFriendlyMessage = originalMessage; 
}
          
          switch (status) {
            case 400:
              userFriendlyMessage = 'Invalid request. Please check your input and try again.';
              break;
            case 403:
              userFriendlyMessage = 'Access denied. You don\'t have permission to perform this action.';
              break;
            case 404:
              userFriendlyMessage = 'The requested resource was not found.';
              break;
            case 429:
              userFriendlyMessage = 'Too many requests. Please wait a moment and try again.';
              break;
            case 500:
              userFriendlyMessage = 'Service temporarily unavailable. Please try again later.';
              break;
            case 502:
              userFriendlyMessage = 'Service is currently down for maintenance. Please try again later.';
              break;
            case 503:
              userFriendlyMessage = 'Service temporarily unavailable. Please try again later.';
              break;
            default:
              userFriendlyMessage = 'An unexpected error occurred. Please try again.';
          }
          
          // Replace the error message with user-friendly one
          if (error.response.data) {
            (error.response.data as any).userFriendlyMessage = userFriendlyMessage;
          }
        } else if (error.request) {
          // Network error
          (error as any).userFriendlyMessage = 'Network error. Please check your connection and try again.';
        } else {
          // Other errors
          (error as any).userFriendlyMessage = 'An unexpected error occurred. Please try again.';
        }
        
        return Promise.reject(error);
      }
    );
  }

  get instance(): AxiosInstance {
    return this.api;
  }
}

export const apiService = new ApiService();
export const api = apiService.instance;






