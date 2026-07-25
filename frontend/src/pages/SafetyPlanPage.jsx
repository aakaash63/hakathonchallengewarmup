import { useState, useEffect } from 'react';
import { getProfile, updateSafety } from '../api/userApi';

export default function SafetyPlanPage() {
  const [form, setForm] = useState({ triggers: '', warningSigns: '', calmingStrategies: '', safePlace: '', supportContactName: '', supportContactPhone: '', personalMantra: '' });
  const [saved, setSaved] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    getProfile().then(r => {
      const d = r.data;
      setForm({ triggers: d.triggers||'', warningSigns: d.warningSigns||'', calmingStrategies: d.calmingStrategies||'', safePlace: d.safePlace||'', supportContactName: d.supportContactName||'', supportContactPhone: d.supportContactPhone||'', personalMantra: d.personalMantra||'' });
    }).catch(() => {});
  }, []);

  const save = async () => {
    setLoading(true); setSaved(false);
    try { await updateSafety(form); setSaved(true); setTimeout(() => setSaved(false), 3000); }
    catch { alert('Failed to save. Please try again.'); }
    finally { setLoading(false); }
  };

  const field = (key, label, placeholder, rows = 0) => (
    <div>
      <label className="text-sm font-semibold text-gray-700 mb-1 block">{label}</label>
      {rows > 0
        ? <textarea className="input-field" rows={rows} placeholder={placeholder} value={form[key]} onChange={e => setForm(p => ({...p, [key]: e.target.value}))} />
        : <input className="input-field" placeholder={placeholder} value={form[key]} onChange={e => setForm(p => ({...p, [key]: e.target.value}))} />
      }
    </div>
  );

  return (
    <div className="max-w-lg mx-auto px-4 py-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-1">🛡️ Safety Plan</h1>
      <p className="text-gray-400 text-sm mb-5">Your personal recovery toolkit. This information personalizes every AI response.</p>

      <div className="card space-y-5">
        {field('personalMantra',       '🌟 Personal Mantra',           'e.g. One day at a time. I am worth recovery.')}
        {field('supportContactName',   '📞 Support Contact Name',      'e.g. Mom, Sponsor, Best friend')}
        {field('supportContactPhone',  '📱 Support Contact Phone',     'e.g. +1 555 0100 (optional)')}
        {field('triggers',             '⚡ My Triggers',               'e.g. loneliness, stress, conflict…', 2)}
        {field('warningSigns',         '🚨 My Warning Signs',          'e.g. poor sleep, withdrawing from friends…', 2)}
        {field('calmingStrategies',    '🌿 Calming Strategies',        'e.g. deep breathing, walking, calling a friend…', 2)}
        {field('safePlace',            '🏠 My Safe Place',             'e.g. my bedroom, the park…')}

        {saved && <div className="bg-hope-50 border border-hope-200 text-hope-700 rounded-2xl px-4 py-3 text-sm font-medium">✅ Safety plan saved successfully!</div>}

        <button onClick={save} disabled={loading} className="btn-hope w-full">
          {loading ? 'Saving…' : '💾 Save Safety Plan'}
        </button>
      </div>

      <div className="mt-6 bg-red-50 border border-red-200 rounded-3xl p-4">
        <p className="text-sm font-bold text-red-700 mb-1">🆘 Emergency Contacts</p>
        <div className="space-y-1 text-sm">
          <a href="tel:911" className="flex items-center gap-2 text-red-600 font-bold hover:underline">📞 Emergency: 911</a>
          <a href="tel:18006624357" className="flex items-center gap-2 text-red-600 hover:underline">📞 SAMHSA: 1-800-662-4357</a>
          <a href="tel:988" className="flex items-center gap-2 text-red-600 hover:underline">📞 Crisis Lifeline: 988</a>
        </div>
      </div>
    </div>
  );
}
