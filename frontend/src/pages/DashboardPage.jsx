import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getMoodHistory } from '../api/moodApi';

const RISK_STYLE = { LOW: 'risk-LOW', MEDIUM: 'risk-MEDIUM', HIGH: 'risk-HIGH' };

const IND_TOOLS = [
  { to: '/moodlog',   icon: '📋', label: 'Mood Log',         desc: 'Track your daily wellbeing',   color: 'bg-blue-50  border-blue-200  hover:bg-blue-100'   },
  { to: '/scripts',   icon: '📝', label: 'Support Scripts',  desc: 'AI-personalized scripts',      color: 'bg-violet-50 border-violet-200 hover:bg-violet-100' },
  { to: '/resources', icon: '📚', label: 'Learn & Explore',  desc: 'Trusted recovery resources',   color: 'bg-emerald-50 border-emerald-200 hover:bg-emerald-100' },
  { to: '/safety-plan',icon:'🛡️', label: 'Safety Plan',      desc: 'Your personal recovery plan',  color: 'bg-amber-50  border-amber-200  hover:bg-amber-100'  },
];

const FAM_TOOLS = [
  { to: '/family',    icon: '🤝', label: 'Family Guidance',  desc: 'AI support for caregivers',    color: 'bg-blue-50  border-blue-200  hover:bg-blue-100'   },
  { to: '/resources', icon: '📚', label: 'Learn & Explore',  desc: 'Recovery education hub',       color: 'bg-emerald-50 border-emerald-200 hover:bg-emerald-100' },
  { to: '/safety-plan',icon:'🛡️', label: 'Safety Plan',      desc: 'Emergency resources & contacts',color:'bg-amber-50  border-amber-200  hover:bg-amber-100'  },
];

export default function DashboardPage() {
  const { user } = useAuth();
  const [latestLog, setLatestLog] = useState(null);
  const isFamily = user?.role === 'FAMILY';
  const tools = isFamily ? FAM_TOOLS : IND_TOOLS;
  const hour = new Date().getHours();
  const greet = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';

  useEffect(() => {
    if (!isFamily) getMoodHistory().then(r => r.data?.length && setLatestLog(r.data[0])).catch(() => {});
  }, [isFamily]);

  return (
    <div className="max-w-2xl mx-auto px-4 py-6">
      {/* Greeting */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">{greet}, {user?.name?.split(' ')[0]} 👋</h1>
        <p className="text-gray-400 text-sm mt-1">
          {isFamily ? "Your support makes a real difference today." : "You chose recovery today — that matters."}
        </p>
      </div>

      {/* Risk banner */}
      {latestLog && (
        <div className={`border-2 rounded-3xl px-5 py-4 mb-5 ${RISK_STYLE[latestLog.riskLevel]}`}>
          <div className="flex justify-between items-center">
            <div>
              <p className="text-xs font-bold uppercase tracking-wide opacity-60">Latest Mood Log</p>
              <p className="text-lg font-bold">{latestLog.riskLevel} Risk</p>
              <p className="text-sm opacity-75">{latestLog.aiSummary?.split('.')[0]}.</p>
            </div>
            <Link to="/moodlog" className="text-xs underline opacity-60 font-medium">Log now →</Link>
          </div>
        </div>
      )}

      {!latestLog && !isFamily && (
        <div className="bg-hope-50 border border-hope-200 rounded-3xl px-5 py-4 mb-5">
          <p className="text-hope-700 font-medium text-sm">🌿 Start with a mood log to personalize your experience.</p>
        </div>
      )}

      {/* Primary CTA */}
      {!isFamily && (
        <Link to="/intervention"
          className="w-full bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white rounded-3xl px-6 py-5 flex items-center gap-4 shadow-lg hover:shadow-xl transition-all duration-200 mb-6 group">
          <span className="text-4xl group-hover:scale-110 transition-transform">🆘</span>
          <div>
            <p className="font-bold text-lg">Need Immediate Help?</p>
            <p className="text-red-100 text-sm">Voice-powered AI intervention — zero typing needed</p>
          </div>
          <span className="ml-auto text-2xl">→</span>
        </Link>
      )}
      {isFamily && (
        <Link to="/family"
          className="w-full bg-gradient-to-r from-calm-500 to-calm-600 hover:from-calm-600 hover:to-calm-700 text-white rounded-3xl px-6 py-5 flex items-center gap-4 shadow-lg hover:shadow-xl transition-all duration-200 mb-6 group">
          <span className="text-4xl group-hover:scale-110 transition-transform">🤝</span>
          <div>
            <p className="font-bold text-lg">Get Guidance Now</p>
            <p className="text-calm-100 text-sm">AI-powered family and caregiver support</p>
          </div>
          <span className="ml-auto text-2xl">→</span>
        </Link>
      )}

      {/* Tools grid */}
      <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Your Tools</p>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {tools.map(t => (
          <Link key={t.to} to={t.to}
            className={`border rounded-3xl p-5 flex items-start gap-4 transition-all duration-200 hover:shadow-md ${t.color}`}>
            <span className="text-3xl">{t.icon}</span>
            <div>
              <p className="font-semibold text-gray-800">{t.label}</p>
              <p className="text-xs text-gray-500 mt-0.5">{t.desc}</p>
            </div>
          </Link>
        ))}
      </div>

      {/* Footer note */}
      <div className="mt-8 bg-gray-100 rounded-3xl p-4 text-center">
        <p className="text-xs text-gray-400">
          🛡️ Supportive guidance only — not medical advice.
          Emergencies: <strong>911</strong> | SAMHSA: <strong>1-800-662-4357</strong>
        </p>
      </div>
    </div>
  );
}
