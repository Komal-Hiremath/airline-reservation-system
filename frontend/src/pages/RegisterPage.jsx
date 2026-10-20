import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api.js';
import { ErrorNotice } from '../components/Notice.jsx';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.register(form);
      navigate('/login', { replace: true, state: { registered: true } });
    } catch (err) {
      setError(err);
      setBusy(false);
    }
  };

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <form className="panel narrow form" onSubmit={submit}>
      <h1>Create your account</h1>
      <ErrorNotice error={error} />
      <label>
        Full name
        <input autoComplete="name" required value={form.name} onChange={set('name')} />
      </label>
      <label>
        Email
        <input type="email" autoComplete="email" required value={form.email} onChange={set('email')} />
      </label>
      <label>
        Password
        <input type="password" autoComplete="new-password" required minLength={6} value={form.password}
               onChange={set('password')} />
        <small className="muted">At least 6 characters.</small>
      </label>
      <button className="btn primary" disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
      <p className="muted">
        Already registered? <Link to="/login">Sign in</Link>
      </p>
    </form>
  );
}
