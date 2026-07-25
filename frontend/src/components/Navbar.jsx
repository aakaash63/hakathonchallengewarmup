import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const NAV = [
  { to: '/dashboard',    icon: '🏠', label: 'Home' },
  { to: '/intervention', icon: '🆘', label: 'Help Now' },
  { to: '/moodlog',      icon: '📋', label: 'Mood Log' },
  { to: '/scripts',      icon: '📝', label: 'Scripts' },
  { to: '/resources',    icon: '📚', label: 'Learn' },
];

const NAV_FAMILY = [
  { to: '/dashboard', icon: '🏠', label: 'Home' },
  { to: '/family',    icon: '🤝', label: 'Guidance' },
  { to: '/resources', icon: '📚', label: 'Learn' },
];

export default function Navbar() {
  const { user, logoutUser } = useAuth();
  const loc = useLocation();
  const nav = useNavigate();
  const links = user?.role === 'FAMILY' ? NAV_FAMILY : NAV;

  return (
    <>
      {/* Top bar */}
      <nav className="bg-white border-b border-gray-100 px-4 py-3 flex items-center justify-between sticky top-0 z-40 shadow-sm">
        <Link to="/dashboard" className="flex items-center gap-2 font-bold text-hope-700 text-lg">
          🌱 <span>HopeGuide AI</span>
        </Link>
        <div className="flex items-center gap-3">
          <span className="text-xs text-gray-400 hidden sm:block">{user?.name}</span>
          <button onClick={() => { logoutUser(); nav('/login'); }}
            className="text-xs text-gray-500 hover:text-red-500 transition-colors font-medium">
            Sign out
          </button>
        </div>
      </nav>

      {/* Bottom mobile nav */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 flex justify-around py-2 z-40 md:hidden shadow-lg">
        {links.map(l => (
          <Link key={l.to} to={l.to}
            className={`flex flex-col items-center gap-0.5 px-3 py-1 rounded-xl transition-colors
              ${loc.pathname === l.to ? 'text-hope-600' : 'text-gray-400'}`}>
            <span className="text-xl">{l.icon}</span>
            <span className="text-[10px] font-medium">{l.label}</span>
          </Link>
        ))}
      </nav>
    </>
  );
}
