import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import RequireRole from './components/RequireRole.jsx';
import SearchPage from './pages/SearchPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import BookFlightPage from './pages/BookFlightPage.jsx';
import MyBookingsPage from './pages/MyBookingsPage.jsx';
import BookingDetailPage from './pages/BookingDetailPage.jsx';
import AdminAirportsPage from './pages/admin/AdminAirportsPage.jsx';
import AdminFlightsPage from './pages/admin/AdminFlightsPage.jsx';
import FlightManifestPage from './pages/admin/FlightManifestPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<SearchPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        <Route element={<RequireRole role="CUSTOMER" />}>
          <Route path="flights/:id/book" element={<BookFlightPage />} />
          <Route path="bookings" element={<MyBookingsPage />} />
          <Route path="bookings/:id" element={<BookingDetailPage />} />
        </Route>

        <Route element={<RequireRole role="ADMIN" />}>
          <Route path="admin/airports" element={<AdminAirportsPage />} />
          <Route path="admin/flights" element={<AdminFlightsPage />} />
          <Route path="admin/flights/:id/manifest" element={<FlightManifestPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
