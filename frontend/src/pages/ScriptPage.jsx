import { useState, useEffect } from 'react';
import { generateScript, getScriptHistory } from '../api/scriptApi';
import { speak } from '../utils/speech';

const SCENARIOS = [
  { value: 'urge',               label: '🔥 Managing an Urge' },
  { value: 'relapse_prevention', label: '🛡️ Relapse Prevention' },
  { value: 'refusal',            label: '🚫 Refusing Substances' },
  { value: 'grounding',          label: '🌿 Grounding Myself' },
  { value: 'family_support',     label: '🤝 Asking Family for Help' },
];

const AUDIENCE = [
  { value: 'self',    label: '🪞 For Myself' },
  { value: 'family',  label: '👨‍👩‍👧 For Family' },
  { value: 'peer',    label: '🤝 For a Peer' },
  { value: 'sponsor', label: '🌟 For Sponsor' },
];

export default function ScriptPage() {
  const [scenario, setScenario] = useState('urge');
  const [audience, setAudience] = useState('self');
  const [result, setResult]     = useState(null);
  const [history, setHistory]   = useState([]);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');

  useEffect(() => { getScriptHistory().then(r => setHistory(r.data || [])).catch(() => {}); }, []);

  const generate = async () => {
    setLoading(true); setError(''); setResult(null);
    try {
      const r = await generateScript({ scenario, audience });
      setResult(r.data);
      setHistory(h => [r.data, ...h.slice(0, 9)]);
    } catch { setError('Failed to generate script. Please try again.'); }
    finally { setLoading(false); }
  };

  return (
    <div className="max-w-lg mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-1">📝 Support Scripts</h1>
      <p className="text-gray-400 text-sm mb-5">AI generates a personalized script based on your profile and chosen scenario.</p>

      <div className="card mb-5 space-y-5">
        <div>
          <p className="text-sm font-semibold text-gray-700 mb-2">Scenario</p>
          <div className="space-y-2">
            {SCENARIOS.map(s => (
              <button key={s.value} onClick={() => setScenario(s.value)}
                className={`w-full text-left px-4 py-3 rounded-2xl border text-sm font-medium transition-all
                  ${scenario === s.value ? 'bg-hope-500 text-white border-hope-500' : 'bg-gray-50 border-gray-200 hover:border-hope-300 text-gray-700'}`}>
                {s.label}
              </button>
            ))}
          </div>
        </div>

        <div>
          <p className="text-sm font-semibold text-gray-700 mb-2">Audience</p>
          <div className="grid grid-cols-2 gap-2">
            {AUDIENCE.map(a => (
              <button key={a.value} onClick={() => setAudience(a.value)}
                className={`px-3 py-2.5 rounded-2xl border text-sm font-medium transition-all
                  ${audience === a.value ? 'bg-calm-500 text-white border-calm-500' : 'bg-gray-50 border-gray-200 hover:border-calm-300 text-gray-700'}`}>
                {a.label}
              </button>
            ))}
          </div>
        </div>

        {error && <p className="text-red-500 text-sm">{error}</p>}
        <button onClick={generate} disabled={loading} className="btn-hope w-full">
          {loading ? 'Generating with AI…' : '✨ Generate Script'}
        </button>
      </div>

      {result && (
        <div className="card mb-5 border-l-4 border-hope-400">
          <div className="flex justify-between items-start mb-3">
            <div>
              <h3 className="font-bold text-gray-800">{result.title}</h3>
              <p className="text-xs text-gray-400">{new Date(result.createdAt).toLocaleString()}</p>
            </div>
            <div className="flex gap-2">
              <button onClick={() => speak(result.scriptText)} className="text-xs bg-hope-50 text-hope-600 px-3 py-1.5 rounded-xl hover:bg-hope-100">🔊 Listen</button>
              <button onClick={() => navigator.clipboard?.writeText(result.scriptText)} className="text-xs bg-gray-50 text-gray-600 px-3 py-1.5 rounded-xl hover:bg-gray-100">📋 Copy</button>
            </div>
          </div>
          <p className="text-gray-700 leading-relaxed italic text-sm">"{result.scriptText}"</p>
        </div>
      )}

      {history.length > 0 && (
        <div>
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Recent Scripts</p>
          <div className="space-y-3">
            {history.map((h, i) => (
              <div key={i} className="card">
                <div className="flex justify-between items-start mb-2">
                  <p className="font-semibold text-sm text-gray-800">{h.title}</p>
                  <button onClick={() => speak(h.scriptText)} className="text-xs text-hope-600 hover:underline">🔊</button>
                </div>
                <p className="text-xs text-gray-500 italic">"{h.scriptText?.substring(0, 100)}…"</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
