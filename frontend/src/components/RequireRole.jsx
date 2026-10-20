import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

// Sends visitors to the sign-in page, and shows a clear message to the wrong role.
export default function RequireRole({ role }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (user.role !== role) {
    return (
      <div className="panel narrow">
        <h1>This area is for {role === 'ADMIN' ? 'administrators' : 'customers'}</h1>
        <p className="muted">
          You are signed in as {user.role === 'ADMIN' ? 'an administrator' : 'a customer'}, which does not
          have access to this page.
        </p>
      </div>
    );
  }
  return <Outlet />;
}
