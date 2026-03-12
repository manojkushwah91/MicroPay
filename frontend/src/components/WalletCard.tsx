import { useState, useEffect } from 'react';
import type { Wallet } from '../types';

interface WalletCardProps {
  wallet: Wallet;
}

export default function WalletCard({ wallet }: WalletCardProps) {
  const [displayBalance, setDisplayBalance] = useState(wallet.balance);
  const [isAnimating, setIsAnimating] = useState(false);

  useEffect(() => {
    if (wallet.balance !== displayBalance) {
      setIsAnimating(true);
      const timer = setTimeout(() => {
        setDisplayBalance(wallet.balance);
        setIsAnimating(false);
      }, 300);
      return () => clearTimeout(timer);
    }
  }, [wallet.balance, displayBalance]);

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return 'bg-success-500/20 text-success-400 border-success-500/30';
      case 'inactive':
        return 'bg-warning-500/20 text-warning-400 border-warning-500/30';
      case 'suspended':
        return 'bg-danger-500/20 text-danger-400 border-danger-500/30';
      default:
        return 'bg-fintech-500/20 text-fintech-400 border-fintech-500/30';
    }
  };

  return (
    <div className="bg-gradient-to-br from-fintech-800/60 to-fintech-900/60 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30 animate-scale-in">
      <div className="flex justify-between items-start mb-6">
        <div>
          <h3 className="text-lg font-semibold text-fintech-100 mb-1">Wallet Balance</h3>
          <div className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium border ${getStatusColor(wallet.status)}`}>
            <div className={`w-2 h-2 rounded-full mr-2 ${wallet.status === 'active' ? 'bg-success-400' : wallet.status === 'inactive' ? 'bg-warning-400' : 'bg-danger-400'}`}></div>
            {wallet.status}
          </div>
        </div>
        <div className="bg-gradient-to-br from-primary-500/20 to-primary-600/20 rounded-2xl p-4 border border-primary-500/30">
          <svg
            className="w-8 h-8 text-primary-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div>
      </div>
      
      <div className="mt-8">
        <div className="relative">
          <p className={`text-4xl font-bold text-white transition-all duration-300 ${isAnimating ? 'opacity-50 scale-95' : 'opacity-100 scale-100'}`}>
            {formatCurrency(displayBalance, wallet.currency)}
          </p>
          {isAnimating && (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="w-6 h-6 border-2 border-primary-400 border-t-transparent rounded-full animate-spin"></div>
            </div>
          )}
        </div>
        
        <div className="mt-4 flex items-center justify-between">
          <p className="text-sm text-fintech-400">
            {wallet.currency || 'USD'}
          </p>
          <p className="text-xs text-fintech-500">
            Last updated: {new Date(wallet.updatedAt).toLocaleString()}
          </p>
        </div>
      </div>

      <div className="mt-6 pt-6 border-t border-fintech-700/30">
        <div className="flex justify-between items-center">
          <div className="text-center">
            <p className="text-xs text-fintech-500 mb-1">Monthly Income</p>
            <p className="text-sm font-semibold text-success-400">+$2,450.00</p>
          </div>
          <div className="text-center">
            <p className="text-xs text-fintech-500 mb-1">Monthly Expenses</p>
            <p className="text-sm font-semibold text-danger-400">-$1,320.00</p>
          </div>
          <div className="text-center">
            <p className="text-xs text-fintech-500 mb-1">Savings Rate</p>
            <p className="text-sm font-semibold text-primary-400">46%</p>
          </div>
        </div>
      </div>
    </div>
  );
}







