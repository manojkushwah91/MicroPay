import type { Transaction } from '../types';

interface TransactionListProps {
  transactions: Transaction[];
  showPaymentId?: boolean;
}

export default function TransactionList({ transactions, showPaymentId = false }: TransactionListProps) {
  if (transactions.length === 0) {
    return (
      <div className="text-center py-12 text-fintech-400">
        <p>No transactions found.</p>
      </div>
    );
  }

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  };

  const getEntryTypeColor = (type: string) => {
    return type === 'DEBIT' ? 'text-danger-400' : 'text-success-400';
  };

  const getEntryTypeBg = (type: string) => {
    return type === 'DEBIT' ? 'bg-danger-500/20' : 'bg-success-500/20';
  };

  const getEntryTypeIcon = (type: string) => {
    return type === 'DEBIT' ? (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
      </svg>
    ) : (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
      </svg>
    );
  };

  const getStatusBadge = (status: string) => {
    const statusConfig = {
      'RECORDED': {
        bg: 'bg-success-500/20',
        text: 'text-success-400',
        border: 'border-success-500/30',
        icon: (
          <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
          </svg>
        )
      },
      'PENDING': {
        bg: 'bg-warning-500/20',
        text: 'text-warning-400',
        border: 'border-warning-500/30',
        icon: (
          <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
          </svg>
        )
      },
      'FAILED': {
        bg: 'bg-danger-500/20',
        text: 'text-danger-400',
        border: 'border-danger-500/30',
        icon: (
          <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
          </svg>
        )
      }
    };

    const config = statusConfig[status as keyof typeof statusConfig] || statusConfig['PENDING'];
    
    return (
      <div className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium border ${config.bg} ${config.text} ${config.border}`}>
        <span className="mr-1">{config.icon}</span>
        {status}
      </div>
    );
  };

  return (
    <div className="space-y-4">
      {transactions.map((transaction) => (
        <div
          key={transaction.id}
          className="bg-fintech-800/30 backdrop-blur-sm rounded-xl p-6 border border-fintech-700/30 hover:bg-fintech-800/40 transition-all duration-200 hover:shadow-glass-sm animate-slide-up"
        >
          <div className="flex justify-between items-start mb-4">
            <div>
              <h3 className="text-lg font-semibold text-white mb-1">
                Transaction #{transaction.transactionId.slice(0, 8)}
              </h3>
              {showPaymentId && transaction.paymentId && (
                <p className="text-sm text-fintech-500">
                  Payment: {transaction.paymentId.slice(0, 8)}
                </p>
              )}
            </div>
            {getStatusBadge(transaction.status)}
          </div>

          <div className="space-y-3">
            {transaction.entries.map((entry, index) => (
              <div
                key={entry.id || index}
                className="flex justify-between items-center p-4 bg-fintech-900/30 rounded-lg border border-fintech-700/20"
              >
                <div className="flex items-center space-x-4">
                  <div className={`p-2 rounded-lg ${getEntryTypeBg(entry.entryType)}`}>
                    <span className={getEntryTypeColor(entry.entryType)}>
                      {getEntryTypeIcon(entry.entryType)}
                    </span>
                  </div>
                  <div>
                    <p className="font-medium text-white">
                      {entry.entryType === 'DEBIT' ? 'Debit' : 'Credit'}
                    </p>
                    <p className="text-sm text-fintech-500">User: {entry.userId.slice(0, 8)}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className={`text-lg font-semibold ${getEntryTypeColor(entry.entryType)}`}>
                    {entry.entryType === 'DEBIT' ? '-' : '+'}
                    {formatCurrency(entry.amount, entry.currency)}
                  </p>
                </div>
              </div>
            ))}
          </div>

          <div className="mt-4 pt-4 border-t border-fintech-700/30">
            <div className="flex items-center justify-between text-sm text-fintech-500">
              <div className="flex items-center">
                <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
                </svg>
                {transaction.recordedAt ? new Date(transaction.recordedAt).toLocaleString() : 'Processing...'}
              </div>
              <div className="flex items-center space-x-2">
                <span className="w-2 h-2 bg-success-400 rounded-full animate-pulse"></span>
                <span>Blockchain Verified</span>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}







