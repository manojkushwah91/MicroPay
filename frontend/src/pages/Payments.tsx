import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { walletService } from '../services/walletService';
import PaymentModal from '../components/PaymentModal';
import WalletCard from '../components/WalletCard';
import Skeleton from '../components/Skeleton';
import type { Wallet } from '../types';

export default function Payments() {
  const navigate = useNavigate();
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [creditAmount, setCreditAmount] = useState<number>(0);
  const [debitAmount, setDebitAmount] = useState<number>(0);
  const [debitError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
  const [isPageLoading, setIsPageLoading] = useState(true);

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/login');
      return;
    }

    const userId = authService.getUserId();
    if (userId) {
      loadWallet(userId);
    }
  }, [navigate]);

  const loadWallet = async (userId: string) => {
    try {
      const walletData = await walletService.getWallet(userId);
      setWallet(walletData);
    } catch (err) {
      console.error('Failed to load wallet:', err);
    } finally {
      setIsPageLoading(false);
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
      const userFriendlyMessage = err.userFriendlyMessage || err.response?.data?.userFriendlyMessage || 'Credit operation failed. Please try again.';
      setMessage({
        type: 'error',
        text: userFriendlyMessage,
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
      const userFriendlyMessage = err.userFriendlyMessage || err.response?.data?.userFriendlyMessage || 'Debit operation failed. Please try again.';
      setMessage({
        type: 'error',
        text: userFriendlyMessage,
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handlePaymentSuccess = () => {
    setMessage({ type: 'success', text: 'Payment initiated successfully!' });
    const userId = authService.getUserId();
    if (userId) {
      loadWallet(userId);
    }
  };

  if (isPageLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <Skeleton width="200px" height="40px" className="mb-2" />
          <Skeleton width="300px" height="20px" />
        </div>
        
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30">
            <Skeleton width="250px" height="28px" className="mb-6" />
            <Skeleton variant="card" height="200px" />
          </div>
          <div className="bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30">
            <Skeleton width="200px" height="28px" className="mb-6" />
            <div className="space-y-4">
              <Skeleton height="50px" />
              <Skeleton height="50px" />
              <Skeleton height="50px" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8 animate-fade-in">
        <h1 className="text-4xl font-bold text-white mb-2">Payments</h1>
        <p className="text-fintech-300 text-lg">Send money and manage your wallet</p>
      </div>

      {message && (
        <div
          className={`mb-6 px-6 py-4 rounded-xl backdrop-blur-sm border animate-slide-down ${
            message.type === 'success'
              ? 'bg-success-500/20 text-success-100 border-success-500/30'
              : 'bg-danger-500/20 text-danger-100 border-danger-500/30'
          }`}
        >
          <div className="flex items-center">
            {message.type === 'success' ? (
              <svg className="w-5 h-5 mr-3" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
            ) : (
              <svg className="w-5 h-5 mr-3" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            )}
            {message.text}
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Wallet Operations */}
        <div className="animate-slide-up">
          <h2 className="text-2xl font-semibold text-white mb-6">Wallet Operations</h2>
          
          {wallet && <WalletCard wallet={wallet} />}

          <div className="mt-6 bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30">
            <h3 className="text-lg font-semibold text-white mb-4">Quick Actions</h3>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-fintech-300 mb-2">
                  Credit Amount
                </label>
                <div className="flex space-x-3">
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={creditAmount || ''}
                    onChange={(e) => setCreditAmount(parseFloat(e.target.value) || 0)}
                    className="flex-1 px-4 py-3 bg-fintech-900/50 border border-fintech-700/30 rounded-lg text-white placeholder-fintech-500 focus:outline-none focus:ring-2 focus:ring-success-500 focus:border-transparent"
                    placeholder="0.00"
                  />
                  <button
                    onClick={handleCredit}
                    disabled={isLoading}
                    className="px-6 py-3 bg-gradient-to-r from-success-500 to-success-600 text-white font-medium rounded-lg hover:from-success-600 hover:to-success-700 transition-all disabled:opacity-50 shadow-lg hover:shadow-glow-green"
                  >
                    {isLoading ? (
                      <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mx-auto"></div>
                    ) : (
                      'Credit'
                    )}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-fintech-300 mb-2">Debit Amount</label>
                <div className="flex space-x-3">
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={debitAmount || ''}
                    onChange={(e) => setDebitAmount(parseFloat(e.target.value) || 0)}
                    className="flex-1 px-4 py-3 bg-fintech-900/50 border border-fintech-700/30 rounded-lg text-white placeholder-fintech-500 focus:outline-none focus:ring-2 focus:ring-danger-500 focus:border-transparent"
                    placeholder="0.00"
                  />
                  <button
                    onClick={handleDebit}
                    disabled={isLoading}
                    className="px-6 py-3 bg-gradient-to-r from-danger-500 to-danger-600 text-white font-medium rounded-lg hover:from-danger-600 hover:to-danger-700 transition-all disabled:opacity-50 shadow-lg"
                  >
                    {isLoading ? (
                      <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mx-auto"></div>
                    ) : (
                      'Debit'
                    )}
                  </button>
                </div>
                {debitError && <p className="text-red-500 text-sm mt-1">{debitError}</p>}
              </div>
            </div>
          </div>
        </div>

        {/* Send Payment */}
        <div className="animate-slide-up" style={{ animationDelay: '0.1s' }}>
          <div className="bg-fintech-800/50 backdrop-blur-md rounded-2xl p-6 shadow-glass border border-fintech-700/30">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-semibold text-white">Send Payment</h2>
              <div className="bg-primary-500/20 rounded-full p-3 border border-primary-500/30">
                <svg className="w-6 h-6 text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
            </div>

            <div className="text-center py-8">
              <div className="w-16 h-16 bg-gradient-to-br from-primary-500/20 to-primary-600/20 rounded-full flex items-center justify-center mx-auto mb-4 border border-primary-500/30">
                <svg className="w-8 h-8 text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">Quick Payment Transfer</h3>
              <p className="text-fintech-400 mb-6">Send money instantly to any user</p>
              
              <button
                onClick={() => setIsPaymentModalOpen(true)}
                className="px-8 py-3 bg-gradient-to-r from-primary-500 to-primary-600 text-white font-medium rounded-xl hover:from-primary-600 hover:to-primary-700 transition-all duration-200 transform hover:scale-[1.02] shadow-lg hover:shadow-glow"
              >
                Send Money Now
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Payment Modal */}
      <PaymentModal
        isOpen={isPaymentModalOpen}
        onClose={() => setIsPaymentModalOpen(false)}
        onSuccess={handlePaymentSuccess}
      />
    </div>
  );
}


