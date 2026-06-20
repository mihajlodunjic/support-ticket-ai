import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { Layout } from './components/Layout';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { AdminTicketsPage } from './pages/AdminTicketsPage';
import { CreateTicketPage } from './pages/CreateTicketPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { PredictTestPage } from './pages/PredictTestPage';
import { RegisterPage } from './pages/RegisterPage';
import { TicketDetailsPage } from './pages/TicketDetailsPage';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/tickets/new" element={<CreateTicketPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/predict-test" element={<PredictTestPage />} />
        <Route path="/admin" element={<ProtectedRoute />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="tickets" element={<AdminTicketsPage />} />
          <Route path="tickets/:id" element={<TicketDetailsPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
