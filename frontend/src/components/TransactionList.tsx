import type { Transaction } from '../types';

interface TransactionListProps {
  transactions: Transaction[];
  showPaymentId?: boolean;
}

export default function TransactionList({ transactions, showPaymentId = false }: TransactionListProps) {
  if (transactions.length === 0) {
    return (
      <div className="text-center py-12 text-gray-500">
        <p>No transactions found.</p>
      </div>
    );
  }

  const formatCurrency = (amount: number, currency: string) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(amount);
  };

  const getEntryTypeColor = (type: string) => {
    return type === 'DEBIT' ? 'text-red-600' : 'text-green-600';
  };

  const getEntryTypeIcon = (type: string) => {
    return type === 'DEBIT' ? '↓' : '↑';
  };

  return (
    <div className="space-y-4">
      {transactions.map((transaction) => (
        <div
          key={transaction.id}
          className="bg-white rounded-lg shadow-md p-6 border border-gray-200 hover:shadow-lg transition"
        >
          <div className="flex justify-between items-start mb-4">
            <div>
              <h3 className="text-lg font-semibold text-gray-900">
                Transaction #{transaction.transactionId.slice(0, 8)}
              </h3>
              {showPaymentId && transaction.paymentId && (
                <p className="text-sm text-gray-500 mt-1">
                  Payment: {transaction.paymentId.slice(0, 8)}
                </p>
              )}
            </div>
            <span
              className={`px-3 py-1 rounded-full text-xs font-semibold ${
                transaction.status === 'RECORDED'
                  ? 'bg-green-100 text-green-800'
                  : 'bg-yellow-100 text-yellow-800'
              }`}
            >
              {transaction.status}
            </span>
          </div>

          <div className="space-y-3">
            {transaction.entries.map((entry, index) => (
              <div
                key={entry.id || index}
                className="flex justify-between items-center p-3 bg-gray-50 rounded-lg"
              >
                <div className="flex items-center space-x-3">
                  <span className={`text-2xl font-bold ${getEntryTypeColor(entry.entryType)}`}>
                    {getEntryTypeIcon(entry.entryType)}
                  </span>
                  <div>
                    <p className="font-medium text-gray-900">
                      {entry.entryType === 'DEBIT' ? 'Debit' : 'Credit'}
                    </p>
                    <p className="text-sm text-gray-500">User: {entry.userId.slice(0, 8)}</p>
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

          <div className="mt-4 pt-4 border-t border-gray-200">
            <p className="text-sm text-gray-500">
              Recorded: {transaction.recordedAt ? new Date(transaction.recordedAt).toLocaleString() : 'N/A'}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}







