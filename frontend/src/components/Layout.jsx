import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

export default function Layout() {
  const { user, isAdmin, isCustomer, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = () => {
    logout();
    navigate('/');
  };

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <Link to="/" className="brand">Skyway</Link>
          <nav className="nav" aria-label="Main">
            <NavLink to="/" end>Flights</NavLink>
            {isCustomer && <NavLink to="/bookings">My bookings</NavLink>}
            {isAdmin && <NavLink to="/admin/flights">Manage flights</NavLink>}
            {isAdmin && <NavLink to="/admin/airports">Manage airports</NavLink>}
          </nav>
          <div className="account">
            {user ? (
              <>
                <span className="who">{user.name} <span className="role-label">({user.role === 'ADMIN' ? 'Admin' : 'Customer'})</span></span>
                <button className="btn ghost light" onClick={signOut}>Sign out</button>
              </>
            ) : (
              <>
                <Link className="btn ghost light" to="/login">Sign in</Link>
                <Link className="btn accent" to="/register">Create account</Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </>
  );
}
