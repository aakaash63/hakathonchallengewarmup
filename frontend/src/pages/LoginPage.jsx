import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { loginUser } = useAuth();
  const nav = useNavigate();

  const submit = async e => {
    e.preventDefault(); setError(''); setLoading(true);
    try {
      const res = await login(form);
      loginUser(res.data);
      nav(res.data.onboardingComplete ? '/dashboard' : '/onboarding');
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed. Please try again.');
    } finally { setLoading(false); }
  };

  const fill = role => setForm(role === 'individual'
    ? { email: 'user@hopeguide.com', password: 'Demo@123' }
    : { email: 'family@hopeguide.com', password: 'Demo@123' });

  return (
    <div className="min-h-screen bg-gradient-to-br from-hope-50 via-white to-calm-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="text-7xl mb-3">🌱</div>
          <h1 className="text-3xl font-bold text-hope-800">HopeGuide AI</h1>
          <p className="text-gray-500 mt-2 text-sm">Your recovery companion. Every step counts.</p>
        </div>

        <div className="card">
          <h2 className="text-xl font-bold text-gray-800 mb-5">Welcome back</h2>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 rounded-2xl px-4 py-3 mb-4 text-sm" role="alert">
              ⚠️ {error}
            </div>
          )}

          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block" htmlFor="email">Email</label>
              <input id="email" type="email" className="input-field" placeholder="you@example.com"
                value={form.email} onChange={e => setForm(p => ({ ...p, email: e.target.value }))} required />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block" htmlFor="password">Password</label>
              <input id="password" type="password" className="input-field" placeholder="••••••••"
                value={form.password} onChange={e => setForm(p => ({ ...p, password: e.target.value }))} required />
            </div>
            <button type="submit" className="btn-hope w-full" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign In →'}
            </button>
          </form>

          <div className="mt-4 bg-hope-50 rounded-2xl p-3 border border-hope-100">
            <p className="text-xs text-hope-700 font-semibold mb-2">🧪 Evaluator demo access:</p>
            <div className="flex gap-2">
              <button onClick={() => fill('individual')} className="text-xs bg-hope-100 text-hope-700 px-3 py-1.5 rounded-xl hover:bg-hope-200 transition-colors font-medium">
                Individual User
              </button>
              <button onClick={() => fill('family')} className="text-xs bg-calm-100 text-calm-700 px-3 py-1.5 rounded-xl hover:bg-calm-200 transition-colors font-medium">
                Family Member
              </button>
            </div>
          </div>

          <p className="text-center text-sm text-gray-400 mt-4">
            New here? <Link to="/signup" className="text-hope-600 font-semibold hover:underline">Create account</Link>
          </p>
        </div>

        <p className="text-center text-xs text-gray-400 mt-6 px-4">
          ⚠️ Supportive guidance only — not medical advice.<br/>
          Emergencies: call <strong>911</strong> | SAMHSA: <strong>1-800-662-4357</strong>
        </p>
      </div>
    </div>
  );
}
