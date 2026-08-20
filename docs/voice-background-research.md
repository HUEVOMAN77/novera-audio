# Investigación del asistente de voz en segundo plano

Android clasifica el acceso al micrófono como permiso `while-in-use`. Para una aplicación que apunte a Android 14 o superior, el servicio foreground de tipo `microphone` necesita declarar el tipo y `FOREGROUND_SERVICE_MICROPHONE`, y crear/iniciar el servicio mientras la actividad está visible; crear ese servicio desde el fondo puede lanzar una excepción de seguridad. Android 12 o superior también limita el inicio de servicios foreground desde segundo plano.

Fuentes oficiales:

- https://developer.android.com/develop/background-work/services/fgs/service-types
- https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start

## Decisión de producto

Novera Audio puede escuchar con la pantalla apagada después de que el usuario active explícitamente el asistente desde la aplicación. El servicio permanecerá visible mediante una notificación persistente con interruptor para detenerlo. No se prometerá escucha con el teléfono completamente apagado, porque el sistema y el micrófono no están activos.

La modalidad más segura será una palabra de activación local y después una ventana breve de reconocimiento del comando. La aplicación no enviará audio a internet. En dispositivos donde el reconocimiento local no esté disponible, se deberá informar al usuario y no cambiar silenciosamente a reconocimiento en la nube.
