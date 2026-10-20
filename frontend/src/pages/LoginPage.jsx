import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';
import { ErrorNotice, SuccessNotice } from '../components/Notice.jsx';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const user = await login(form.email, form.password);
      navigate(location.state?.from ?? (user.role === 'ADMIN' ? '/admin/flights' : '/'), { replace: true });
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  return (
    <form className="panel narrow form" onSubmit={submit}>
      <h1>Sign in</h1>
      <SuccessNotice>{location.state?.registered && 'Account created. Sign in to continue.'}</SuccessNotice>
      <ErrorNotice error={error} />
      <label>
        Email
        <input type="email" autoComplete="email" required value={form.email}
               onChange={(e) => setForm({ ...form, email: e.target.value })} />
      </label>
      <label>
        Password
        <input type="password" autoComplete="current-password" required value={form.password}
               onChange={(e) => setForm({ ...form, password: e.target.value })} />
      </label>
      <button className="btn primary" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
      <p className="muted">
        New here? <Link to="/register">Create an account</Link>
      </p>
    </form>
  );
}
