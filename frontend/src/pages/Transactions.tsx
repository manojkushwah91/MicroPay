import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { transactionService } from '../services/transactionService';
import TransactionList from '../components/TransactionList';
import Skeleton from '../components/Skeleton';
import type { Transaction } from '../types';

export default function Transactions() {
  const navigate = useNavigate();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
      return;
    }

    const loadTransactions = async () => {
      const userId = authService.getUserId();
      if (!userId) {
        navigate('/login');
        return;
      }

      try {
        setIsLoading(true);
        const data = await transactionService.getTransactions(userId);
        setTransactions(data);
      } catch (err: any) {
        const userFriendlyMessage = err.userFriendlyMessage || err.response?.data?.userFriendlyMessage || 'Failed to load transactions';
        setError(userFriendlyMessage);
      } finally {
        setIsLoading(false);
      }
    };

    loadTransactions();
  }, [navigate]);

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <Skeleton width="200px" height="40px" className="mb-2" />
          <Skeleton width="300px" height="20px" />
        </div>
        
        <div className="bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30">
          <div className="space-y-4">
            {Array.from({ length: 10 }).map((_, index) => (
              <div key={index} className="flex justify-between items-center">
                <div className="flex-1">
                  <Skeleton width="120px" height="16px" className="mb-2" />
                  <Skeleton width="80px" height="12px" />
                </div>
                <Skeleton width="80px" height="16px" />
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8 animate-fade-in">
        <h1 className="text-4xl font-bold text-white mb-2">Transactions</h1>
        <p className="text-fintech-300 text-lg">View all your transaction history</p>
      </div>

      {error && (
        <div className="mb-6 bg-danger-500/20 backdrop-blur-md border border-danger-500/30 text-danger-100 px-6 py-4 rounded-xl animate-slide-down">
          <div className="flex items-center">
            <svg className="w-5 h-5 mr-3" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
            {error}
          </div>
        </div>
      )}

      <div className="bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30 animate-slide-up">
        <TransactionList transactions={transactions} showPaymentId={true} />
      </div>
    </div>
  );
}







