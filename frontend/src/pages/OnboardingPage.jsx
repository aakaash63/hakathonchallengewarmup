import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { saveOnboarding } from '../api/userApi';
import { useAuth } from '../context/AuthContext';

const STEPS = ['Welcome', 'Triggers & Warnings', 'Calming Strategies', 'Support Contact', 'Personal Mantra'];

export default function OnboardingPage() {
  const [step, setStep] = useState(0);
  const [form, setForm] = useState({ triggers: '', warningSigns: '', calmingStrategies: '', safePlace: '', supportContactName: '', supportContactPhone: '', personalMantra: '' });
  const [loading, setLoading] = useState(false);
  const { completeOnboarding } = useAuth();
  const nav = useNavigate();

  const up = k => e => setForm(p => ({...p, [k]: e.target.value}));

  const finish = async () => {
    setLoading(true);
    try {
      await saveOnboarding(form);
      completeOnboarding();
      nav('/dashboard');
    } catch { nav('/dashboard'); }
    finally { setLoading(false); }
  };

  const pages = [
    <div className="text-center space-y-4">
      <div className="text-7xl">🌱</div>
      <h2 className="text-2xl font-bold text-hope-800">Welcome to HopeGuide AI</h2>
      <p className="text-gray-500 leading-relaxed">Let's take 2 minutes to personalize your experience. This helps HopeGuide give you the most relevant support when you need it most.</p>
      <div className="bg-hope-50 rounded-2xl p-4 text-sm text-hope-700 text-left">
        <p>✅ Your information is private and secure</p>
        <p>✅ Used only to personalize AI support for you</p>
        <p>✅ You can update this anytime in Safety Plan</p>
      </div>
    </div>,

    <div className="space-y-4">
      <h2 className="text-xl font-bold text-gray-800">What triggers you?</h2>
      <p className="text-sm text-gray-500">Understanding your triggers helps us give better support during difficult moments.</p>
      <textarea className="input-field" rows={3} placeholder="e.g. loneliness, conflict with family, financial stress, boredom..."
        value={form.triggers} onChange={up('triggers')} />
      <h3 className="font-semibold text-gray-700 mt-2">Warning signs you notice in yourself</h3>
      <textarea className="input-field" rows={2} placeholder="e.g. withdrawing from friends, poor sleep, irritability..."
        value={form.warningSigns} onChange={up('warningSigns')} />
    </div>,

    <div className="space-y-4">
      <h2 className="text-xl font-bold text-gray-800">What helps you calm down?</h2>
      <p className="text-sm text-gray-500">HopeGuide will suggest these when you're struggling.</p>
      <textarea className="input-field" rows={3} placeholder="e.g. deep breathing, going for a walk, listening to music, calling a friend..."
        value={form.calmingStrategies} onChange={up('calmingStrategies')} />
      <h3 className="font-semibold text-gray-700">Your safe place</h3>
      <input className="input-field" placeholder="e.g. my bedroom, the park, a coffee shop..."
        value={form.safePlace} onChange={up('safePlace')} />
    </div>,

    <div className="space-y-4">
      <h2 className="text-xl font-bold text-gray-800">Who can you call?</h2>
      <p className="text-sm text-gray-500">Your go-to person when things get hard.</p>
      <input className="input-field" placeholder="Name (e.g. Mom, Sponsor, Best friend)"
        value={form.supportContactName} onChange={up('supportContactName')} />
      <input className="input-field" placeholder="Phone number (optional)"
        value={form.supportContactPhone} onChange={up('supportContactPhone')} />
    </div>,

    <div className="space-y-4">
      <h2 className="text-xl font-bold text-gray-800">Your personal mantra</h2>
      <p className="text-sm text-gray-500">A phrase that keeps you grounded. HopeGuide will remind you of this in hard moments.</p>
      <input className="input-field" placeholder="e.g. One day at a time. I am worth recovery. Progress, not perfection."
        value={form.personalMantra} onChange={up('personalMantra')} />
      <div className="bg-hope-50 rounded-2xl p-4 text-sm text-hope-700">
        <p className="font-semibold mb-1">🌟 You're all set!</p>
        <p>HopeGuide AI is personalized and ready to support you. Tap Finish to get started.</p>
      </div>
    </div>
  ];

  return (
    <div className="min-h-screen bg-gradient-to-b from-hope-50 to-white flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        {/* Progress */}
        <div className="flex gap-1.5 mb-8">
          {STEPS.map((_, i) => (
            <div key={i} className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${i <= step ? 'bg-hope-500' : 'bg-gray-200'}`} />
          ))}
        </div>
        <p className="text-xs text-gray-400 mb-4 font-medium">{STEPS[step]}</p>

        <div className="card min-h-[320px] flex flex-col justify-between">
          <div>{pages[step]}</div>
          <div className="flex gap-3 mt-8">
            {step > 0 && (
              <button className="btn-outline flex-1" onClick={() => setStep(s => s - 1)}>← Back</button>
            )}
            {step < STEPS.length - 1 ? (
              <button className="btn-hope flex-1" onClick={() => setStep(s => s + 1)}>Next →</button>
            ) : (
              <button className="btn-hope flex-1" onClick={finish} disabled={loading}>
                {loading ? 'Saving…' : '🌱 Finish Setup'}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
