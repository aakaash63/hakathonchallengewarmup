import { useState, useEffect } from 'react';
import { getResources, searchResources, explainTopic } from '../api/resourceApi';
import { speak } from '../utils/speech';

const CATS = ['ALL', 'RELAPSE', 'COPING', 'OVERDOSE', 'FAMILY', 'TREATMENT', 'RECOVERY'];
const CAT_COLORS = { RELAPSE: 'bg-red-100 text-red-700', COPING: 'bg-blue-100 text-blue-700', OVERDOSE: 'bg-orange-100 text-orange-700', FAMILY: 'bg-purple-100 text-purple-700', TREATMENT: 'bg-green-100 text-green-700', RECOVERY: 'bg-hope-100 text-hope-700' };

export default function ResourcesPage() {
  const [resources, setResources] = useState([]);
  const [cat, setCat]             = useState('ALL');
  const [search, setSearch]       = useState('');
  const [question, setQuestion]   = useState('');
  const [aiAnswer, setAiAnswer]   = useState(null);
  const [loading, setLoading]     = useState(false);
  const [aiLoading, setAiLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    getResources().then(r => setResources(r.data || [])).finally(() => setLoading(false));
  }, []);

  const handleSearch = async () => {
    if (!search.trim()) return;
    const r = await searchResources(search);
    setResources(r.data || []);
    setCat('ALL');
  };

  const handleAsk = async () => {
    if (!question.trim()) return;
    setAiLoading(true); setAiAnswer(null);
    try {
      const r = await explainTopic(question);
      setAiAnswer(r.data);
      speak(r.data.answer);
    } catch { setAiAnswer({ answer: 'Sorry, could not get an AI answer right now.', source: '' }); }
    finally { setAiLoading(false); }
  };

  const shown = cat === 'ALL' ? resources : resources.filter(r => r.category === cat);

  return (
    <div className="max-w-lg mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-1">📚 Learn & Explore</h1>
      <p className="text-gray-400 text-sm mb-5">Trusted recovery education powered by SAMHSA, NIDA, CDC and WHO.</p>

      {/* AI Ask box */}
      <div className="card mb-5 bg-gradient-to-br from-hope-50 to-calm-50 border-hope-200">
        <p className="text-sm font-bold text-hope-800 mb-2">🤖 Ask HopeGuide AI anything</p>
        <div className="flex gap-2">
          <input className="input-field flex-1" placeholder="e.g. What are relapse warning signs?"
            value={question} onChange={e => setQuestion(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleAsk()} />
          <button onClick={handleAsk} disabled={aiLoading} className="btn-hope px-4">
            {aiLoading ? '…' : 'Ask'}
          </button>
        </div>
        {aiAnswer && (
          <div className="mt-4 bg-white rounded-2xl p-4 border border-hope-100">
            <div className="flex justify-between items-start mb-2">
              <p className="text-xs font-bold text-hope-700 uppercase">AI Answer</p>
              <button onClick={() => speak(aiAnswer.answer)} className="text-xs text-hope-600 hover:underline">🔊</button>
            </div>
            <p className="text-sm text-gray-700 leading-relaxed">{aiAnswer.answer}</p>
            {aiAnswer.source && <p className="text-xs text-gray-400 mt-2">Source: {aiAnswer.source}</p>}
          </div>
        )}
      </div>

      {/* Search */}
      <div className="flex gap-2 mb-4">
        <input className="input-field flex-1" placeholder="Search resources…" value={search}
          onChange={e => setSearch(e.target.value)} onKeyDown={e => e.key === 'Enter' && handleSearch()} />
        <button onClick={handleSearch} className="btn-outline px-4">🔍</button>
      </div>

      {/* Category tabs */}
      <div className="flex gap-2 overflow-x-auto pb-2 mb-4 scrollbar-hide">
        {CATS.map(c => (
          <button key={c} onClick={() => setCat(c)}
            className={`text-xs px-3 py-1.5 rounded-xl font-semibold whitespace-nowrap border transition-all
              ${cat === c ? 'bg-hope-600 text-white border-hope-600' : 'bg-white border-gray-200 text-gray-600 hover:border-hope-300'}`}>
            {c}
          </button>
        ))}
      </div>

      {/* Resource cards */}
      {loading ? <p className="text-center text-gray-400 py-10">Loading resources…</p> : (
        <div className="space-y-3">
          {shown.map((r, i) => (
            <div key={i} className="card hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start mb-2">
                <span className={`text-xs px-2 py-0.5 rounded-lg font-semibold ${CAT_COLORS[r.category] || 'bg-gray-100 text-gray-600'}`}>
                  {r.category}
                </span>
                <button onClick={() => speak(r.summary)} className="text-xs text-hope-600 hover:underline">🔊</button>
              </div>
              <h3 className="font-bold text-gray-800 mb-1">{r.title}</h3>
              <p className="text-sm text-gray-600 leading-relaxed">{r.summary}</p>
              <p className="text-xs text-gray-400 mt-2">Source: {r.sourceName} · <a href={r.sourceUrl} target="_blank" rel="noreferrer" className="text-hope-600 hover:underline">Visit →</a></p>
            </div>
          ))}
          {shown.length === 0 && <p className="text-center text-gray-400 py-10">No resources found.</p>}
        </div>
      )}
    </div>
  );
}
