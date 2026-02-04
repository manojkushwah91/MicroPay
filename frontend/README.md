# MicroPay Frontend

Production-ready React frontend application for MicroPay digital payment platform.

## Technology Stack

- **React**: 18.2.0
- **TypeScript**: 5.2.2
- **Vite**: 5.0.8 (Build tool)
- **Tailwind CSS**: 3.3.6 (Styling)
- **React Router**: 6.20.0 (Navigation)
- **Axios**: 1.6.2 (HTTP client)

## Features

- 🔐 Authentication (Login/Register)
- 💰 Wallet Management (View balance, Credit/Debit)
- 💳 Payment Processing (Initiate payments)
- 📊 Transaction History (View all transactions)
- 🔔 Notifications (View user notifications)
- 🎨 Modern UI with Tailwind CSS
- 📱 Responsive Design
- ✅ Form Validation
- 🔒 Protected Routes
- 🎯 TypeScript for type safety

## Project Structure

```
frontend/
├── src/
│   ├── components/          # Reusable components
│   │   ├── Navbar.tsx
│   │   ├── Footer.tsx
│   │   ├── WalletCard.tsx
│   │   ├── TransactionList.tsx
│   │   ├── NotificationList.tsx
│   │   └── ProtectedRoute.tsx
│   ├── pages/               # Page components
│   │   ├── Login.tsx
│   │   ├── Register.tsx
│   │   ├── Dashboard.tsx
│   │   ├── Payments.tsx
│   │   ├── Transactions.tsx
│   │   └── Notifications.tsx
│   ├── services/            # API services
│   │   ├── api.ts
│   │   ├── authService.ts
│   │   ├── walletService.ts
│   │   ├── paymentService.ts
│   │   ├── transactionService.ts
│   │   └── notificationService.ts
│   ├── types/               # TypeScript types
│   │   └── index.ts
│   ├── App.tsx              # Main app component with routing
│   ├── main.tsx             # Entry point
│   └── index.css            # Global styles
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
└── README.md
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn
- API Gateway running on `http://localhost:8080`

### Installation

1. Install dependencies:
```bash
npm install
```

2. Create `.env` file (optional, defaults to `http://localhost:8080`):
```env
VITE_API_BASE_URL=http://localhost:8080
```

3. Start development server:
```bash
npm run dev
```

The application will be available at `http://localhost:3000`

### Build for Production

```bash
npm run build
```

The production build will be in the `dist` directory.

### Preview Production Build

```bash
npm run preview
```

## Pages

### Login (`/login`)
- User authentication
- Form validation
- Redirects to dashboard on success

### Register (`/register`)
- New user registration
- Form validation
- Auto-login after registration

### Dashboard (`/dashboard`)
- Wallet balance display
- Recent transactions (last 5)
- Quick navigation to other pages

### Payments (`/payments`)
- Wallet credit/debit operations
- Initiate payments to other users
- Real-time wallet balance updates

### Transactions (`/transactions`)
- Complete transaction history
- Transaction details with entries
- Payment ID references

### Notifications (`/notifications`)
- User notification list
- Notification status and details
- Reference to related payments/transactions

## API Integration

The frontend connects to the API Gateway at `http://localhost:8080` (configurable via environment variable).

### Authentication
- JWT tokens stored in localStorage
- Automatic token injection in API requests
- Auto-redirect to login on 401 errors

### API Endpoints Used

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/wallet/{userId}` - Get wallet
- `POST /api/wallet/{userId}/credit` - Credit wallet
- `POST /api/wallet/{userId}/debit` - Debit wallet
- `POST /api/payment` - Initiate payment
- `GET /api/payment/{paymentId}` - Get payment
- `GET /api/transactions/{userId}` - Get transactions
- `GET /api/transaction/{transactionId}` - Get transaction
- `GET /api/notifications/{userId}` - Get notifications

## Features

### Form Validation
- Email format validation
- Password length validation
- Required field validation
- Real-time error display

### Protected Routes
- Authentication check before accessing protected pages
- Automatic redirect to login if not authenticated
- Prevents access to auth pages when already logged in

### Error Handling
- API error display
- User-friendly error messages
- Loading states for async operations

### Responsive Design
- Mobile-friendly layout
- Tailwind CSS responsive utilities
- Adaptive components

## Configuration

### Environment Variables

- `VITE_API_BASE_URL`: API Gateway base URL (default: `http://localhost:8080`)

### Vite Configuration

- Development server port: `3000`
- Proxy configuration for API calls
- Hot module replacement enabled

## Development

### Code Style

- TypeScript strict mode enabled
- ESLint for code quality
- Consistent component structure
- Service layer for API calls

### Best Practices

- TypeScript types for all data structures
- Reusable components
- Service layer separation
- Error handling in all API calls
- Loading states for better UX

## Troubleshooting

### API Connection Issues

1. Ensure API Gateway is running on `http://localhost:8080`
2. Check CORS configuration in API Gateway
3. Verify JWT token is being sent in requests

### Build Issues

1. Clear `node_modules` and reinstall:
```bash
rm -rf node_modules package-lock.json
npm install
```

2. Check TypeScript errors:
```bash
npm run build
```

## License

Copyright © 2024 MicroPay. All rights reserved.







