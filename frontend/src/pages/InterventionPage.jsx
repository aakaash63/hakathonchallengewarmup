import { useState } from 'react';
import { sendIntervention } from '../api/interventionApi';
import { speak, stopSpeaking } from '../utils/speech';
import VoiceButton from '../components/VoiceButton';

const QUICK = [
  { label: '😰 Strong urge right now', text: 'I am experiencing a very strong urge and need help right now.' },
  { label: '😔 I feel really unsafe', text: 'I feel unsafe and overwhelmed and I need immediate support.' },
  { label: '😤 Completely overwhelmed', text: 'I am completely overwhelmed and struggling to cope right now.' },
  { label: '😢 Thinking about giving up', text: 'I am struggling badly and thinking about giving up on recovery.' },
];

const RISK_BG = { LOW: 'bg-emerald-50 border-emerald-300', MEDIUM: 'bg-amber-50 border-amber-300', HIGH: 'bg-red-50 border-red-300' };

export default function InterventionPage() {
  const [input, setInput]     = useState('');
  const [response, setResponse] = useState(null);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');
  const [playing, setPlaying]   = useState(false);

  const submit = async text => {
    const t = text || input;
    if (!t?.trim()) return;
    setLoading(true); setError(''); setResponse(null); stopSpeaking();
    try {
      const res = await sendIntervention({ inputText: t, mode: 'text' });
      setResponse(res.data);
      setTimeout(() => speak(res.data.ttsText || res.data.message), 300);
    } catch { setError('Could not connect to AI. Please check your connection.'); }
    finally { setLoading(false); }
  };

  const togglePlay = () => {
    if (!response) return;
    if (playing) { stopSpeaking(); setPlaying(false); }
    else {
      setPlaying(true);
      speak(`${response.message}. ${(response.steps||[]).join('. ')}`);
      setTimeout(() => setPlaying(false), 12000);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-hope-50 to-slate-50">
      <div className="max-w-lg mx-auto px-4 py-6">
        <div className="text-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">🆘 Immediate Support</h1>
          <p className="text-gray-400 text-sm mt-1">Speak or type — HopeGuide AI responds instantly</p>
        </div>

        {/* Voice button */}
        <div className="card mb-5 text-center py-8">
          <p className="text-sm text-gray-400 mb-5 font-medium">🎤 Zero-typing: tap the mic and speak</p>
          <VoiceButton onTranscript={t => { setInput(t); submit(t); }} onError={e => setError(e)} />
        </div>

        {/* Quick prompts */}
        <p className="text-xs text-gray-400 font-medium mb-2">Or choose a quick option:</p>
        <div className="grid grid-cols-2 gap-2 mb-4">
          {QUICK.map(q => (
            <button key={q.label} onClick={() => { setInput(q.text); submit(q.text); }}
              className="text-sm bg-white border border-gray-200 hover:border-hope-300 hover:bg-hope-50 rounded-2xl p-3 text-left transition-all font-medium text-gray-700">
              {q.label}
            </button>
          ))}
        </div>

        {/* Text input */}
        <textarea className="input-field mb-2" rows={3} placeholder="Or type how you're feeling right now…"
          value={input} onChange={e => setInput(e.target.value)} />
        <button onClick={() => submit()} disabled={loading || !input.trim()} className="btn-hope w-full mb-4">
          {loading ? <span className="flex items-center justify-center gap-2"><span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"/>Getting AI support…</span> : '🧠 Get AI Support'}
        </button>

        {error && <div className="bg-red-50 border border-red-200 text-red-600 rounded-2xl p-4 mb-4 text-sm" role="alert">{error}</div>}

        {/* AI Response */}
        {response && (
          <div className={`border-2 rounded-3xl p-5 mb-4 ${RISK_BG[response.urgencyLevel] || RISK_BG.MEDIUM}`}>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold px-3 py-1 rounded-full border risk-{response.urgencyLevel}">
                {response.urgencyLevel} RISK
              </span>
              <button onClick={togglePlay} className="text-xs bg-white border border-gray-200 rounded-xl px-3 py-1 hover:bg-gray-50">
                {playing ? '🔇 Stop' : '🔊 Read Aloud'}
              </button>
            </div>

            {response.escalate && (
              <div className="bg-red-600 text-white rounded-2xl p-3 mb-3 flex items-center gap-2">
                <span>🚨</span>
                <div>
                  <p className="font-bold text-sm">Call emergency services immediately</p>
                  <p className="text-red-100 text-xs">Dial 911 now</p>
                </div>
              </div>
            )}

            <p className="text-gray-800 font-semibold text-lg mb-4 leading-relaxed">{response.message}</p>

            {response.steps?.length > 0 && (
              <div className="mb-4">
                <p className="text-xs font-bold text-gray-500 uppercase mb-2">Immediate Steps:</p>
                <div className="space-y-2">
                  {response.steps.map((s, i) => (
                    <div key={i} className="flex gap-2 bg-white bg-opacity-70 rounded-xl p-2">
                      <span className="font-bold text-sm text-hope-600">{i+1}.</span>
                      <p className="text-sm text-gray-700">{s}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {response.script && (
              <div className="bg-white bg-opacity-80 rounded-2xl p-4 mb-4 border border-gray-200">
                <p className="text-xs font-bold text-gray-500 uppercase mb-2">📝 Your Support Script:</p>
                <p className="text-sm text-gray-700 italic">"{response.script}"</p>
                <button onClick={() => navigator.clipboard?.writeText(response.script)}
                  className="text-xs text-hope-600 hover:underline mt-2 block">📋 Copy script</button>
              </div>
            )}

            {response.contactName && (
              <div className="bg-hope-600 text-white rounded-2xl p-4 flex items-center justify-between">
                <div>
                  <p className="text-xs text-hope-100">Support Contact</p>
                  <p className="font-bold">{response.contactName}</p>
                  {response.contactPhone && <p className="text-sm text-hope-100">{response.contactPhone}</p>}
                </div>
                {response.contactPhone && (
                  <a href={`tel:${response.contactPhone}`}
                    className="bg-white text-hope-600 font-bold px-4 py-2 rounded-xl text-sm hover:bg-hope-50">
                    📞 Call
                  </a>
                )}
              </div>
            )}
          </div>
        )}

        <div className="bg-gray-100 rounded-3xl p-4 text-center">
          <p className="text-sm font-semibold text-gray-600">🆘 SAMHSA National Helpline</p>
          <a href="tel:18006624357" className="text-xl font-bold text-hope-600 hover:underline">1-800-662-4357</a>
          <p className="text-xs text-gray-400 mt-0.5">Free • Confidential • 24/7</p>
        </div>
      </div>
    </div>
  );
}
