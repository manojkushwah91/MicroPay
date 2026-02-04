// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
}

// Wallet Types
export interface Wallet {
  id: string;
  userId: string;
  balance: number;
  currency: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreditRequest {
  amount: number;
  transactionId?: string;
  description?: string;
}

export interface DebitRequest {
  amount: number;
  transactionId?: string;
  description?: string;
}

// Payment Types
export interface PaymentRequest {
  payerUserId: string;
  payeeUserId?: string;
  amount: number;
  currency?: string;
  paymentType?: string;
  description?: string;
  reference?: string;
  idempotencyKey: string;
}

export interface Payment {
  id: string;
  paymentId: string;
  payerUserId: string;
  payeeUserId?: string;
  amount: number;
  currency: string;
  paymentType: string;
  status: string;
  description?: string;
  reference?: string;
  transactionId?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  failedAt?: string;
}

// Transaction Types
export interface TransactionEntry {
  id: string;
  userId: string;
  entryType: 'DEBIT' | 'CREDIT';
  amount: number;
  currency: string;
}

export interface Transaction {
  id: string;
  transactionId: string;
  paymentId: string;
  status: string;
  entries: TransactionEntry[];
  createdAt: string;
  updatedAt: string;
  recordedAt?: string;
}

// Notification Types
export interface Notification {
  id: string;
  userId: string;
  notificationType: string;
  channel: string;
  status: string;
  title: string;
  message: string;
  referenceId?: string;
  referenceType?: string;
  createdAt: string;
  sentAt?: string;
  failedAt?: string;
}

// API Error Types
export interface ApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  validationErrors?: Record<string, string>;
}







