import { useState, useEffect } from 'react';
import { logMood, getMoodHistory } from '../api/moodApi';
import { speak } from '../utils/speech';
import VoiceButton from '../components/VoiceButton';

const MOODS = ['😌 Calm', '😊 Good', '😐 Neutral', '😔 Low', '😰 Anxious', '😤 Angry', '😢 Sad'];
const RISK_STYLE = { LOW: 'risk-LOW', MEDIUM: 'risk-MEDIUM', HIGH: 'risk-HIGH' };

export default function MoodLogPage() {
  const [form, setForm] = useState({ mood: '', urgencyLevel: 3, stressLevel: 3, sleepQuality: 3, voiceNote: '' });
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError]   = useState('');

  useEffect(() => { getMoodHistory().then(r => setHistory(r.data || [])).catch(() => {}); }, []);

  const submit = async () => {
    if (!form.mood) return setError('Please select a mood first.');
    setLoading(true); setError('');
    try {
      const r = await logMood(form);
      setResult(r.data);
      setHistory(h => [r.data, ...h.slice(0, 6)]);
      speak(`${r.data.aiSummary}`);
    } catch { setError('Failed to log mood. Please try again.'); }
    finally { setLoading(false); }
  };

  const slider = (key, label, min=1, max=10) => (
    <div>
      <div className="flex justify-between items-center mb-1">
        <label className="text-sm font-medium text-gray-700">{label}</label>
        <span className="text-sm font-bold text-hope-600">{form[key]}/{max}</span>
      </div>
      <input type="range" min={min} max={max} value={form[key]}
        onChange={e => setForm(p => ({...p, [key]: +e.target.value}))}
        className="w-full accent-hope-500" />
      <div className="flex justify-between text-xs text-gray-400 mt-0.5">
        <span>Low</span><span>High</span>
      </div>
    </div>
  );

  return (
    <div className="max-w-lg mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-1">📋 Mood Log</h1>
      <p className="text-gray-400 text-sm mb-5">Daily check-in helps AI understand your patterns and offer better support.</p>

      <div className="card mb-5 space-y-5">
        {/* Mood picker */}
        <div>
          <p className="text-sm font-semibold text-gray-700 mb-2">How are you feeling?</p>
          <div className="flex flex-wrap gap-2">
            {MOODS.map(m => (
              <button key={m} onClick={() => setForm(p => ({...p, mood: m}))}
                className={`text-sm px-3 py-1.5 rounded-xl border transition-all ${form.mood === m ? 'bg-hope-500 text-white border-hope-500' : 'bg-gray-50 border-gray-200 hover:border-hope-300'}`}>
                {m}
              </button>
            ))}
          </div>
        </div>

        {slider('urgencyLevel', '🔥 Urge / Craving Level')}
        {slider('stressLevel',  '⚡ Stress Level')}
        {slider('sleepQuality', '😴 Sleep Quality', 1, 5)}

        {/* Voice note */}
        <div>
          <p className="text-sm font-semibold text-gray-700 mb-2">📝 Voice Note <span className="text-gray-400 font-normal">(optional)</span></p>
          <VoiceButton onTranscript={t => setForm(p => ({...p, voiceNote: t}))} />
          {form.voiceNote && <p className="text-sm text-gray-600 bg-gray-50 rounded-xl p-3 mt-2 italic">"{form.voiceNote}"</p>}
          <textarea className="input-field mt-2" rows={2} placeholder="Or type a short note…"
            value={form.voiceNote} onChange={e => setForm(p => ({...p, voiceNote: e.target.value}))} />
        </div>

        {error && <p className="text-red-500 text-sm">{error}</p>}

        <button onClick={submit} disabled={loading} className="btn-hope w-full">
          {loading ? 'Analyzing with AI…' : '📊 Log & Analyze'}
        </button>
      </div>

      {/* AI Result */}
      {result && (
        <div className={`border-2 rounded-3xl p-5 mb-5 ${RISK_STYLE[result.riskLevel]}`}>
          <div className="flex items-center gap-2 mb-3">
            <span className="font-bold text-lg">{result.riskLevel} Risk</span>
          </div>
          <p className="text-sm font-medium mb-3">{result.aiSummary}</p>
          {result.suggestions && (
            <div>
              <p className="text-xs font-bold uppercase text-gray-500 mb-2">AI Suggestions:</p>
              <div className="space-y-1.5">
                {result.suggestions.split('|').map((s, i) => (
                  <div key={i} className="flex gap-2 text-sm bg-white bg-opacity-70 rounded-xl p-2">
                    <span className="text-hope-500">✦</span><p>{s}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* History */}
      {history.length > 0 && (
        <div>
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Last 7 Logs</p>
          <div className="space-y-2">
            {history.map((h, i) => (
              <div key={i} className={`border rounded-2xl px-4 py-3 flex justify-between items-center ${RISK_STYLE[h.riskLevel]}`}>
                <div>
                  <p className="font-medium text-sm">{h.mood}</p>
                  <p className="text-xs opacity-60">{new Date(h.loggedAt).toLocaleDateString()}</p>
                </div>
                <span className="text-xs font-bold">{h.riskLevel}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
