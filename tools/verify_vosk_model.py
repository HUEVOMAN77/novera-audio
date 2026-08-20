from pathlib import Path
import json
import wave

from vosk import Model, KaldiRecognizer, SetLogLevel

SetLogLevel(-1)
model_path = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "assets" / "model-es"
model = Model(str(model_path))
recognizer = KaldiRecognizer(model, 16000)

# Audio PCM mono de 16 kHz en silencio: valida carga, JNI y ciclo de inferencia.
silence = b"\x00\x00" * 16000
accepted = recognizer.AcceptWaveform(silence)
result = json.loads(recognizer.FinalResult())
print(json.dumps({"model_loaded": True, "accepted_waveform": accepted, "result": result}, ensure_ascii=False))
