# Investigación del visualizador de audio

La documentación oficial de Android describe `android.media.audiofx.Visualizer` como una API que permite recuperar parte del audio que se está reproduciendo para visualización, incluyendo datos de waveform y FFT. La captura se asocia a una sesión de audio y puede entregar actualizaciones mediante `Visualizer.OnDataCaptureListener`. La disponibilidad y el acceso pueden variar según el dispositivo, por lo que la aplicación necesita detectar errores y mantener un fallback visual.

Fuente: https://developer.android.com/reference/android/media/audiofx/Visualizer

La guía oficial de Media3 confirma que ExoPlayer mantiene la cola y controla reproducción, pausa, seek y navegación. Novera Audio seguirá utilizando el mismo ExoPlayer y su audio session id como fuente de la visualización, sin alterar la cola multimedia.

Fuente: https://developer.android.com/media/media3/exoplayer/hello-world

## Decisión

Se integrará un `AudioVisualizerController` con la sesión del reproductor. Cuando `Visualizer` pueda capturar waveform/FFT, el Canvas mostrará amplitud real suavizada; cuando falle por limitaciones del fabricante o sesión, el componente usará ondas animadas de respaldo sincronizadas con el estado de reproducción y no dejará una barra estática.
