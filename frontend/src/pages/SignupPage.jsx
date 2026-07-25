import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function SignupPage() {
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'INDIVIDUAL' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { loginUser } = useAuth();
  const nav = useNavigate();

  const submit = async e => {
    e.preventDefault(); setError(''); setLoading(true);
    try {
      const res = await signup(form);
      loginUser(res.data);
      nav('/onboarding');
    } catch (err) {
      setError(err.response?.data?.error || 'Signup failed. Try again.');
    } finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-hope-50 via-white to-calm-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="text-6xl mb-3">🌱</div>
          <h1 className="text-3xl font-bold text-hope-800">Join HopeGuide AI</h1>
          <p className="text-gray-500 mt-2 text-sm">Start your recovery journey today</p>
        </div>

        <div className="card">
          {error && <div className="bg-red-50 border border-red-200 text-red-600 rounded-2xl px-4 py-3 mb-4 text-sm">⚠️ {error}</div>}

          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">Full Name</label>
              <input className="input-field" placeholder="Your name" value={form.name}
                onChange={e => setForm(p => ({...p, name: e.target.value}))} required />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">Email</label>
              <input type="email" className="input-field" placeholder="you@example.com" value={form.email}
                onChange={e => setForm(p => ({...p, email: e.target.value}))} required />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">Password</label>
              <input type="password" className="input-field" placeholder="Min. 6 characters" value={form.password}
                onChange={e => setForm(p => ({...p, password: e.target.value}))} required minLength={6} />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 mb-1 block">I am a…</label>
              <select className="input-field" value={form.role} onChange={e => setForm(p => ({...p, role: e.target.value}))}>
                <option value="INDIVIDUAL">Individual seeking recovery support</option>
                <option value="FAMILY">Family member or caregiver</option>
              </select>
            </div>
            <button type="submit" className="btn-hope w-full" disabled={loading}>
              {loading ? 'Creating account…' : 'Create Account →'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-400 mt-4">
            Already have an account? <Link to="/login" className="text-hope-600 font-semibold hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
