import type { Wallet } from '../types';

interface WalletCardProps {
  wallet: Wallet;
}

export default function WalletCard({ wallet }: WalletCardProps) {
  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(amount);
  };

  return (
    <div className="bg-gradient-to-br from-primary-600 to-primary-800 rounded-lg shadow-xl p-6 text-white">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-lg font-semibold text-primary-100">Wallet Balance</h3>
          <p className="text-sm text-primary-200">Account Status: {wallet.status}</p>
        </div>
        <div className="bg-white/20 rounded-full p-3">
          <svg
            className="w-8 h-8"
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
      <div className="mt-6">
        <p className="text-4xl font-bold">{formatCurrency(wallet.balance, wallet.currency)}</p>
        <p className="text-sm text-primary-200 mt-2">
          Last updated: {new Date(wallet.updatedAt).toLocaleString()}
        </p>
      </div>
    </div>
  );
}




