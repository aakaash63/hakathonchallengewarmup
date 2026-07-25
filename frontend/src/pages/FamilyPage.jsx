import { useState } from 'react';
import { getFamilyGuidance } from '../api/familyApi';
import { speak } from '../utils/speech';

const SITUATIONS = [
  { value: 'anxious',          label: '😰 They seem anxious or panicking' },
  { value: 'agitated',         label: '😤 They are agitated or angry' },
  { value: 'withdrawn',        label: '😶 They are withdrawn or isolating' },
  { value: 'possible_relapse', label: '⚠️ I suspect possible relapse' },
  { value: 'overdose_concern', label: '🚨 Overdose concern (emergency)' },
];

export default function FamilyPage() {
  const [situation, setSituation] = useState('');
  const [context, setContext]     = useState('');
  const [result, setResult]       = useState(null);
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState('');

  const submit = async () => {
    if (!situation) return setError('Please select a situation first.');
    setLoading(true); setError(''); setResult(null);
    try {
      const r = await getFamilyGuidance({ situation, context });
      setResult(r.data);
      speak(r.data.whatToSay);
    } catch { setError('Failed to get guidance. Please try again.'); }
    finally { setLoading(false); }
  };

  return (
    <div className="max-w-lg mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-1">🤝 Family Guidance</h1>
      <p className="text-gray-400 text-sm mb-5">AI-powered guidance for supporting your loved one right now.</p>

      <div className="card mb-5 space-y-5">
        <div>
          <p className="text-sm font-semibold text-gray-700 mb-2">What's happening right now?</p>
          <div className="space-y-2">
            {SITUATIONS.map(s => (
              <button key={s.value} onClick={() => setSituation(s.value)}
                className={`w-full text-left px-4 py-3 rounded-2xl border text-sm font-medium transition-all
                  ${situation === s.value
                    ? s.value === 'overdose_concern' ? 'bg-red-500 text-white border-red-500' : 'bg-calm-500 text-white border-calm-500'
                    : s.value === 'overdose_concern' ? 'bg-red-50 border-red-200 text-red-700 hover:bg-red-100' : 'bg-gray-50 border-gray-200 hover:border-calm-300 text-gray-700'}`}>
                {s.label}
              </button>
            ))}
          </div>
        </div>

        <div>
          <p className="text-sm font-semibold text-gray-700 mb-1">Additional context <span className="text-gray-400 font-normal">(optional)</span></p>
          <textarea className="input-field" rows={2} placeholder="e.g. They haven't slept in 2 days and won't talk to anyone…"
            value={context} onChange={e => setContext(e.target.value)} />
        </div>

        {error && <p className="text-red-500 text-sm">{error}</p>}
        <button onClick={submit} disabled={loading} className="btn-calm w-full">
          {loading ? 'Getting AI guidance…' : '🤝 Get Guidance'}
        </button>
      </div>

      {result && (
        <div className="space-y-4">
          {result.emergencyEscalate && (
            <div className="bg-red-600 text-white rounded-3xl p-4">
              <p className="font-bold text-lg">🚨 Call 911 immediately</p>
              <p className="text-red-100 text-sm">This is a medical emergency. Do not wait.</p>
            </div>
          )}

          <div className="card border-l-4 border-calm-400">
            <div className="flex justify-between items-start mb-2">
              <p className="text-xs font-bold text-gray-500 uppercase">✅ What to say</p>
              <button onClick={() => speak(result.whatToSay)} className="text-xs text-calm-600 hover:underline">🔊 Listen</button>
            </div>
            <p className="text-gray-800 font-medium leading-relaxed italic">"{result.whatToSay}"</p>
          </div>

          <div className="card border-l-4 border-red-300">
            <p className="text-xs font-bold text-gray-500 uppercase mb-2">❌ Avoid saying</p>
            <ul className="space-y-1.5">
              {result.avoidSaying?.map((s, i) => (
                <li key={i} className="flex gap-2 text-sm text-gray-700">
                  <span className="text-red-400">✗</span> {s}
                </li>
              ))}
            </ul>
          </div>

          <div className="card border-l-4 border-hope-400">
            <p className="text-xs font-bold text-gray-500 uppercase mb-2">📋 Next Steps</p>
            <ol className="space-y-1.5">
              {result.nextSteps?.map((s, i) => (
                <li key={i} className="flex gap-2 text-sm text-gray-700">
                  <span className="font-bold text-hope-600">{i+1}.</span> {s}
                </li>
              ))}
            </ol>
          </div>
        </div>
      )}

      <div className="mt-6 bg-gray-100 rounded-3xl p-4 text-center">
        <p className="text-sm font-semibold text-gray-600">🆘 SAMHSA Family Support</p>
        <a href="tel:18006624357" className="text-xl font-bold text-hope-600 hover:underline">1-800-662-4357</a>
        <p className="text-xs text-gray-400 mt-0.5">Free • Confidential • 24/7</p>
      </div>
    </div>
  );
}
