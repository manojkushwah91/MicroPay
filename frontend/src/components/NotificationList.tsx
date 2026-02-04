import type { Notification } from '../types';

interface NotificationListProps {
  notifications: Notification[];
}

export default function NotificationList({ notifications }: NotificationListProps) {
  if (notifications.length === 0) {
    return (
      <div className="text-center py-12 text-gray-500">
        <p>No notifications found.</p>
      </div>
    );
  }

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'PAYMENT_COMPLETED':
        return '✓';
      case 'TRANSACTION_RECORDED':
        return '📝';
      case 'PAYMENT_FAILED':
        return '✗';
      default:
        return 'ℹ';
    }
  };

  const getNotificationColor = (type: string) => {
    switch (type) {
      case 'PAYMENT_COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'TRANSACTION_RECORDED':
        return 'bg-blue-100 text-blue-800';
      case 'PAYMENT_FAILED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="space-y-4">
      {notifications.map((notification) => (
        <div
          key={notification.id}
          className="bg-white rounded-lg shadow-md p-6 border border-gray-200 hover:shadow-lg transition"
        >
          <div className="flex items-start space-x-4">
            <div
              className={`flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center text-2xl ${getNotificationColor(
                notification.notificationType
              )}`}
            >
              {getNotificationIcon(notification.notificationType)}
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">{notification.title}</h3>
                  <p className="text-sm text-gray-500 mt-1">{notification.message}</p>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-xs font-semibold ${
                    notification.status === 'SENT'
                      ? 'bg-green-100 text-green-800'
                      : notification.status === 'FAILED'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-yellow-100 text-yellow-800'
                  }`}
                >
                  {notification.status}
                </span>
              </div>
              <div className="mt-4 flex items-center space-x-4 text-sm text-gray-500">
                <span>Channel: {notification.channel}</span>
                <span>•</span>
                <span>{new Date(notification.createdAt).toLocaleString()}</span>
                {notification.referenceId && (
                  <>
                    <span>•</span>
                    <span>Ref: {notification.referenceId.slice(0, 8)}</span>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}







