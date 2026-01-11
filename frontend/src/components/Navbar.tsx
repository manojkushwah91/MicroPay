import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';

export default function Navbar() {
  const navigate = useNavigate();
  const isAuthenticated = authService.isAuthenticated();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <nav className="bg-primary-700 text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-2">
              <span className="text-2xl font-bold">MicroPay</span>
            </Link>
          </div>
          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <>
                <Link
                  to="/dashboard"
                  className="px-3 py-2 rounded-md text-sm font-medium hover:bg-primary-600 transition"
                >
                  Dashboard
                </Link>
                <Link
                  to="/payments"
                  className="px-3 py-2 rounded-md text-sm font-medium hover:bg-primary-600 transition"
                >
                  Payments
                </Link>
                <Link
                  to="/transactions"
                  className="px-3 py-2 rounded-md text-sm font-medium hover:bg-primary-600 transition"
                >
                  Transactions
                </Link>
                <Link
                  to="/notifications"
                  className="px-3 py-2 rounded-md text-sm font-medium hover:bg-primary-600 transition"
                >
                  Notifications
                </Link>
                <button
                  onClick={handleLogout}
                  className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-md text-sm font-medium transition"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="px-3 py-2 rounded-md text-sm font-medium hover:bg-primary-600 transition"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 bg-primary-500 hover:bg-primary-600 rounded-md text-sm font-medium transition"
                >
                  Register
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}




