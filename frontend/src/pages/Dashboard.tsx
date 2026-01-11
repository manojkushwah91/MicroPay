import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { walletService } from '../services/walletService';
import { transactionService } from '../services/transactionService';
import WalletCard from '../components/WalletCard';
import TransactionList from '../components/TransactionList';
import type { Wallet, Transaction } from '../types';

export default function Dashboard() {
  const navigate = useNavigate();
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
      return;
    }

    const loadData = async () => {
      const userId = authService.getUserId();
      if (!userId) {
        navigate('/login');
        return;
      }

      try {
        setIsLoading(true);
        const [walletData, transactionsData] = await Promise.all([
          walletService.getWallet(userId),
          transactionService.getTransactions(userId),
        ]);
        setWallet(walletData);
        setTransactions(transactionsData.slice(0, 5)); // Show only recent 5
      } catch (err: any) {
        setError(err.response?.data?.message || 'Failed to load dashboard data');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [navigate]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="mt-2 text-gray-600">Welcome back! Here's your account overview.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1">
          {wallet && <WalletCard wallet={wallet} />}
        </div>
        <div className="lg:col-span-2">
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Recent Transactions</h2>
            {transactions.length > 0 ? (
              <TransactionList transactions={transactions} />
            ) : (
              <p className="text-gray-500 text-center py-8">No recent transactions</p>
            )}
            <div className="mt-6">
              <button
                onClick={() => navigate('/transactions')}
                className="w-full px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 transition"
              >
                View All Transactions
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}




