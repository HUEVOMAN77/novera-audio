# Notas técnicas de audio

## Hallazgos de Android Developers

La API oficial de `android.media.audiofx.Equalizer` permite alterar la respuesta de frecuencia de una fuente o mezcla de audio. La documentación indica que se crea asociando el ecualizador a un `audioSession` concreto de un `AudioTrack` o `MediaPlayer`, y expone bandas, frecuencias centrales, rangos de ganancia, presets y ajustes manuales. También advierte que adjuntar el ecualizador a la mezcla global mediante la sesión 0 está obsoleto.

Fuente: https://developer.android.com/reference/kotlin/android/media/audiofx/Equalizer

La API oficial de `android.media.audiofx.NoiseSuppressor` describe la reducción de ruido como un preprocesador para señal capturada, asociado a una sesión de `AudioRecord`; se usa principalmente en voz, videoconferencia y SIP. No es una solución general para limpiar música reproducida. Por eso, en un reproductor local se debe presentar como una función experimental o de voz/captura, o implementar una cadena DSP específica para reproducción si se desea reducción de ruido real sobre la música.

Fuente: https://developer.android.com/reference/kotlin/android/media/audiofx/NoiseSuppressor

## Decisión de implementación

Se implementará un ecualizador real ligado a la sesión de audio del reproductor cuando el dispositivo lo permita, con perfiles manuales y automáticos. Se añadirá detección de disponibilidad y mensajes de compatibilidad para evitar prometer efectos que algunos fabricantes no soporten. Para Bluetooth se mostrará la salida de audio disponible y se permitirá abrir el panel de selección del sistema; el enrutamiento exacto puede variar por versión de Android, fabricante y dispositivo.

La reducción de ruido no se falsificará como efecto musical universal: se expondrá como control de compatibilidad y se documentará que la API estándar está orientada principalmente a captura de voz. Una reducción de ruido musical avanzada requeriría un procesador DSP de reproducción propio, con mayor complejidad y consumo.
