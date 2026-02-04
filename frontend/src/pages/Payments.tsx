import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { paymentService } from '../services/paymentService';
import { walletService } from '../services/walletService';
import type { PaymentRequest, Wallet, ApiError } from '../types';

export default function Payments() {
  const navigate = useNavigate();
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [formData, setFormData] = useState<PaymentRequest>({
    payerUserId: '',
    payeeUserId: '',
    amount: 0,
    currency: 'USD',
    paymentType: 'PAYMENT',
    description: '',
    reference: '',
    idempotencyKey: '',
  });
  const [creditAmount, setCreditAmount] = useState<number>(0);
  const [debitAmount, setDebitAmount] = useState<number>(0);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
      return;
    }

    const userId = authService.getUserId();
    if (userId) {
      formData.payerUserId = userId;
      loadWallet(userId);
    }
  }, [navigate]);

  const loadWallet = async (userId: string) => {
    try {
      const walletData = await walletService.getWallet(userId);
      setWallet(walletData);
    } catch (err) {
      console.error('Failed to load wallet:', err);
    }
  };

  const validatePayment = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.payeeUserId) {
      newErrors.payeeUserId = 'Payee user ID is required';
    }

    if (!formData.amount || formData.amount <= 0) {
      newErrors.amount = 'Amount must be greater than 0';
    }

    // REMOVED: Idempotency check (because we generate it automatically below)

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handlePayment = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);

    if (!validatePayment()) {
      return;
    }

    setIsLoading(true);
    try {
      // ✅ FIX: Generate the key HERE
      const paymentData = {
        ...formData,
        idempotencyKey: `${Date.now()}-${Math.random()}`
      };

      await paymentService.initiatePayment(paymentData);
      
      setMessage({ type: 'success', text: 'Payment initiated successfully!' });
      
      // Reset form (except payer ID)
      setFormData({
        ...formData,
        payeeUserId: '',
        amount: 0,
        description: '',
        reference: '',
        idempotencyKey: '', 
      });

      // Reload wallet to show new balance
      const userId = authService.getUserId();
      if (userId) {
        await loadWallet(userId);
      }
    } catch (err: any) {
      const apiError = err.response?.data as ApiError;
      setMessage({
        type: 'error',
        text: apiError?.message || 'Payment initiation failed. Please try again.',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCredit = async () => {
    if (creditAmount <= 0) {
      setMessage({ type: 'error', text: 'Amount must be greater than 0' });
      return;
    }

    setIsLoading(true);
    try {
      const userId = authService.getUserId();
      if (!userId) return;

      await walletService.creditWallet(userId, {
        amount: creditAmount,
        description: 'Wallet credit',
      });
      setMessage({ type: 'success', text: 'Wallet credited successfully!' });
      setCreditAmount(0);
      await loadWallet(userId);
    } catch (err: any) {
      const apiError = err.response?.data as ApiError;
      setMessage({
        type: 'error',
        text: apiError?.message || 'Credit operation failed. Please try again.',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleDebit = async () => {
    if (debitAmount <= 0) {
      setMessage({ type: 'error', text: 'Amount must be greater than 0' });
      return;
    }

    setIsLoading(true);
    try {
      const userId = authService.getUserId();
      if (!userId) return;

      await walletService.debitWallet(userId, {
        amount: debitAmount,
        description: 'Wallet debit',
      });
      setMessage({ type: 'success', text: 'Wallet debited successfully!' });
      setDebitAmount(0);
      await loadWallet(userId);
    } catch (err: any) {
      const apiError = err.response?.data as ApiError;
      setMessage({
        type: 'error',
        text: apiError?.message || 'Debit operation failed. Please try again.',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Payments</h1>
        <p className="mt-2 text-gray-600">Initiate payments and manage your wallet.</p>
      </div>

      {message && (
        <div
          className={`mb-6 px-4 py-3 rounded ${
            message.type === 'success'
              ? 'bg-green-50 border border-green-200 text-green-700'
              : 'bg-red-50 border border-red-200 text-red-700'
          }`}
        >
          {message.text}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Wallet Operations */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-2xl font-semibold text-gray-900 mb-6">Wallet Operations</h2>
          {wallet && (
            <div className="mb-6 p-4 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-600">Current Balance</p>
              <p className="text-2xl font-bold text-gray-900">
                {new Intl.NumberFormat('en-US', {
                  style: 'currency',
                  currency: wallet.currency,
                }).format(wallet.balance)}
              </p>
            </div>
          )}

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Credit Amount
              </label>
              <div className="flex space-x-2">
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={creditAmount || ''}
                  onChange={(e) => setCreditAmount(parseFloat(e.target.value) || 0)}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  placeholder="0.00"
                />
                <button
                  onClick={handleCredit}
                  disabled={isLoading}
                  className="px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                >
                  Credit
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Debit Amount</label>
              <div className="flex space-x-2">
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={debitAmount || ''}
                  onChange={(e) => setDebitAmount(parseFloat(e.target.value) || 0)}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  placeholder="0.00"
                />
                <button
                  onClick={handleDebit}
                  disabled={isLoading}
                  className="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
                >
                  Debit
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Payment Form */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-2xl font-semibold text-gray-900 mb-6">Initiate Payment</h2>
          <form onSubmit={handlePayment} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Payee User ID
              </label>
              <input
                type="text"
                value={formData.payeeUserId}
                onChange={(e) => setFormData({ ...formData, payeeUserId: e.target.value })}
                className={`w-full px-3 py-2 border ${
                  errors.payeeUserId ? 'border-red-300' : 'border-gray-300'
                } rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500`}
                placeholder="Enter payee user ID"
              />
              {errors.payeeUserId && (
                <p className="mt-1 text-sm text-red-600">{errors.payeeUserId}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Amount</label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formData.amount || ''}
                onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) || 0 })}
                className={`w-full px-3 py-2 border ${
                  errors.amount ? 'border-red-300' : 'border-gray-300'
                } rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500`}
                placeholder="0.00"
              />
              {errors.amount && <p className="mt-1 text-sm text-red-600">{errors.amount}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Description</label>
              <input
                type="text"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                placeholder="Payment description"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Reference</label>
              <input
                type="text"
                value={formData.reference}
                onChange={(e) => setFormData({ ...formData, reference: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                placeholder="Payment reference"
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Processing...' : 'Initiate Payment'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}


