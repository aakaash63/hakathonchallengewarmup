export function speak(text) {
  if (!window.speechSynthesis || !text) return;
  window.speechSynthesis.cancel();
  const utt = new SpeechSynthesisUtterance(text);
  utt.rate = 0.9;
  utt.pitch = 1;
  utt.lang = 'en-US';
  window.speechSynthesis.speak(utt);
}

export function stopSpeaking() {
  if (window.speechSynthesis) window.speechSynthesis.cancel();
}

export function startListening(onResult, onError) {
  const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SR) { onError?.('Speech recognition not supported in this browser.'); return null; }
  const rec = new SR();
  rec.continuous = false;
  rec.interimResults = false;
  rec.lang = 'en-US';
  rec.onresult = e => onResult(e.results[0][0].transcript);
  rec.onerror  = e => onError?.(e.error);
  rec.start();
  return rec;
}
