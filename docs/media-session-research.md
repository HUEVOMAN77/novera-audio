# MediaSession y controles externos

La documentación oficial de Android indica que `MediaSessionService` crea automáticamente una `MediaNotification` de estilo MediaStyle que se actualiza con el estado del Player y muestra controles de reproducción. La notificación se crea cuando el Player tiene elementos multimedia en su playlist y permanece mientras el servicio foreground está activo.

Media3 también permite controlar la reproducción desde controladores externos: controles multimedia del sistema, auriculares, Google Assistant, pantalla bloqueada, Android Auto y otros procesos conectados a la misma MediaSession. La sesión debe estar creada junto al Player que gestiona la reproducción, y Media3 sincroniza automáticamente el estado del Player con la sesión.

Fuentes:
- https://developer.android.com/media/media3/session/background-playback
- https://developer.android.com/media/media3/session/control-playback

## Diagnóstico de Novera Audio

La corrección previa evitaba iniciar `PlaybackService` en Huawei/Honor para reducir crashes. Esa decisión explica por qué desapareció la notificación multimedia y los controles de pantalla bloqueada. La solución siguiente debe separar el modo de efectos inestables del servicio de MediaSession: Huawei puede omitir AudioEffects y Visualizer, pero el servicio MediaSession debe seguir iniciándose para generar la notificación y los controles externos.

El asistente de voz debe mantenerse separado: su permiso RECORD_AUDIO, su servicio foreground de microphone y su reconocimiento on-device no deben inicializarse durante la reproducción ni compartir callbacks que puedan cerrar la Activity.
