import { useState } from 'react';
import { startListening, stopSpeaking } from '../utils/speech';

export default function VoiceButton({ onTranscript, onError }) {
  const [listening, setListening] = useState(false);
  const [rec, setRec] = useState(null);

  const toggle = () => {
    if (listening) {
      rec?.stop();
      setListening(false);
    } else {
      stopSpeaking();
      setListening(true);
      const r = startListening(
        text => { setListening(false); onTranscript(text); },
        err  => { setListening(false); onError?.(err); }
      );
      setRec(r);
    }
  };

  return (
    <button onClick={toggle} aria-label={listening ? 'Stop listening' : 'Speak your feelings'}
      className={`w-24 h-24 rounded-full flex flex-col items-center justify-center shadow-xl transition-all duration-300 mx-auto
        ${listening
          ? 'bg-red-500 animate-pulse scale-110 text-white'
          : 'bg-hope-600 hover:bg-hope-700 text-white hover:scale-105'}`}>
      <span className="text-4xl">{listening ? '⏹' : '🎤'}</span>
      <span className="text-[11px] font-semibold mt-1">{listening ? 'Listening…' : 'Tap & Speak'}</span>
    </button>
  );
}
