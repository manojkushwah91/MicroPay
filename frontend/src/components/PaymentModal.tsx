import { useState } from 'react';
import { paymentService } from '../services/paymentService';
import { authService } from '../services/authService';

interface PaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export default function PaymentModal({ isOpen, onClose, onSuccess }: PaymentModalProps) {
  const [recipientId, setRecipientId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!recipientId || !amount || parseFloat(amount) <= 0) {
      setError('Please fill in all required fields with valid values');
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      
      const userId = authService.getUserId();
      if (!userId) throw new Error('User not authenticated');

      const payment = await paymentService.initiatePayment({
        payerUserId: userId,
        payeeUserId: recipientId,
        amount: parseFloat(amount),
        description: description || 'Payment transfer',
        currency: 'USD',
        paymentType: 'PAYMENT',
        idempotencyKey: `${Date.now()}-${Math.random()}`
      });

      if (payment.status === 'INITIATED') {
        onSuccess?.();
        onClose();
        // Reset form
        setRecipientId('');
        setAmount('');
        setDescription('');
      } else {
        setError('Payment could not be initiated');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Payment failed');
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    if (!isLoading) {
      setError('');
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex min-h-screen items-center justify-center p-4">
        {/* Backdrop */}
        <div 
          className="fixed inset-0 bg-fintech-900/80 backdrop-blur-sm animate-fade-in"
          onClick={handleClose}
        />
        
        {/* Modal */}
        <div className="relative w-full max-w-md animate-scale-in">
          <div className="bg-fintech-800/90 backdrop-blur-md rounded-2xl shadow-glass border border-fintech-700/30">
            {/* Header */}
            <div className="flex items-center justify-between p-6 border-b border-fintech-700/30">
              <h2 className="text-xl font-semibold text-white">Send Payment</h2>
              <button
                onClick={handleClose}
                disabled={isLoading}
                className="text-fintech-400 hover:text-white transition-colors disabled:opacity-50"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {/* Body */}
            <form onSubmit={handleSubmit} className="p-6">
              {error && (
                <div className="mb-4 p-3 bg-danger-500/20 border border-danger-500/30 rounded-lg text-danger-400 text-sm animate-slide-down">
                  {error}
                </div>
              )}

              <div className="space-y-4">
                {/* Recipient ID */}
                <div>
                  <label className="block text-sm font-medium text-fintech-300 mb-2">
                    Recipient User ID
                  </label>
                  <input
                    type="text"
                    value={recipientId}
                    onChange={(e) => setRecipientId(e.target.value)}
                    placeholder="Enter recipient user ID"
                    disabled={isLoading}
                    className="w-full px-4 py-3 bg-fintech-900/50 border border-fintech-700/30 rounded-lg text-white placeholder-fintech-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:opacity-50"
                    required
                  />
                </div>

                {/* Amount */}
                <div>
                  <label className="block text-sm font-medium text-fintech-300 mb-2">
                    Amount (USD)
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <span className="text-fintech-400">$</span>
                    </div>
                    <input
                      type="number"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                      placeholder="0.00"
                      step="0.01"
                      min="0.01"
                      disabled={isLoading}
                      className="w-full pl-8 pr-4 py-3 bg-fintech-900/50 border border-fintech-700/30 rounded-lg text-white placeholder-fintech-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:opacity-50"
                      required
                    />
                  </div>
                </div>

                {/* Description */}
                <div>
                  <label className="block text-sm font-medium text-fintech-300 mb-2">
                    Description (Optional)
                  </label>
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="What's this payment for?"
                    rows={3}
                    disabled={isLoading}
                    className="w-full px-4 py-3 bg-fintech-900/50 border border-fintech-700/30 rounded-lg text-white placeholder-fintech-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:opacity-50 resize-none"
                  />
                </div>

                {/* Quick Amount Buttons */}
                <div>
                  <label className="block text-sm font-medium text-fintech-300 mb-2">
                    Quick Amount
                  </label>
                  <div className="grid grid-cols-4 gap-2">
                    {['10', '25', '50', '100'].map((value) => (
                      <button
                        key={value}
                        type="button"
                        onClick={() => setAmount(value)}
                        disabled={isLoading}
                        className="px-3 py-2 bg-fintech-700/50 border border-fintech-600/30 rounded-lg text-fintech-300 hover:bg-fintech-700/70 hover:border-fintech-600/50 transition-all disabled:opacity-50"
                      >
                        ${value}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* Footer */}
              <div className="mt-6 flex gap-3">
                <button
                  type="button"
                  onClick={handleClose}
                  disabled={isLoading}
                  className="flex-1 px-4 py-3 bg-fintech-700/50 border border-fintech-600/30 rounded-lg text-fintech-300 hover:bg-fintech-700/70 transition-all disabled:opacity-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="flex-1 px-4 py-3 bg-gradient-to-r from-primary-500 to-primary-600 text-white font-medium rounded-lg hover:from-primary-600 hover:to-primary-700 transition-all disabled:opacity-50 shadow-lg hover:shadow-glow"
                >
                  {isLoading ? (
                    <div className="flex items-center justify-center">
                      <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                      Processing...
                    </div>
                  ) : (
                    'Send Payment'
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
