import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { walletService } from '../services/walletService';
import { transactionService } from '../services/transactionService';
import WalletCard from '../components/WalletCard';
import TransactionList from '../components/TransactionList';
import Skeleton from '../components/Skeleton';
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
        const userFriendlyMessage = err.userFriendlyMessage || err.response?.data?.userFriendlyMessage || 'Failed to load dashboard data';
        setError(userFriendlyMessage);
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [navigate]);

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <Skeleton width="200px" height="40px" className="mb-2" />
          <Skeleton width="300px" height="20px" />
        </div>
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-1">
            <div className="bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30">
              <Skeleton variant="circular" width="60px" height="60px" className="mb-4" />
              <Skeleton height="20px" className="mb-2" />
              <Skeleton height="32px" width="150px" className="mb-4" />
              <Skeleton lines={3} />
            </div>
          </div>
          <div className="lg:col-span-2">
            <div className="bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30">
              <Skeleton width="250px" height="28px" className="mb-6" />
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, index) => (
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
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-danger-500/20 backdrop-blur-md border border-danger-500/30 text-danger-100 px-6 py-4 rounded-xl animate-fade-in">
          <div className="flex items-center">
            <svg className="w-6 h-6 mr-3" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
            {error}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8 animate-fade-in">
        <h1 className="text-4xl font-bold text-white mb-2">Dashboard</h1>
        <p className="text-fintech-300 text-lg">Welcome back! Here's your account overview.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1">
          {wallet && <WalletCard wallet={wallet} />}
        </div>
        <div className="lg:col-span-2">
          <div className="bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30 animate-slide-up">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-semibold text-white">Recent Transactions</h2>
              <div className="flex items-center text-fintech-400 text-sm">
                <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
                </svg>
                Last 7 days
              </div>
            </div>
            
            {transactions.length > 0 ? (
              <TransactionList transactions={transactions} />
            ) : (
              <div className="text-center py-12">
                <div className="w-16 h-16 bg-fintech-700/50 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-fintech-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  </svg>
                </div>
                <p className="text-fintech-400 mb-2">No recent transactions</p>
                <p className="text-fintech-500 text-sm">Your transaction history will appear here</p>
              </div>
            )}
            
            <div className="mt-6">
              <button
                onClick={() => navigate('/transactions')}
                className="w-full px-6 py-3 bg-gradient-to-r from-primary-500 to-primary-600 text-white font-medium rounded-xl hover:from-primary-600 hover:to-primary-700 transition-all duration-200 transform hover:scale-[1.02] shadow-lg hover:shadow-glow"
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







