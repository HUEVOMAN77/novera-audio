# Novera Audio

**Novera Audio** es un reproductor Android nativo para música local. Está diseñado para reproducir MP3 y otros formatos de audio desde el almacenamiento del teléfono, además de permitir importar archivos o carpetas completas desde una memoria USB conectada mediante OTG.

La aplicación utiliza **Kotlin al 100 %**, Jetpack Compose para la interfaz y Java 17 como objetivo de compilación. No utiliza Expo, React Native ni servicios de nube. La biblioteca importada, los favoritos y la configuración de acceso se guardan localmente en el dispositivo.

> **Nota sobre el nombre:** Novera Audio es una propuesta de identidad original para este proyecto. Una búsqueda pública inicial no mostró un reproductor Android con ese nombre exacto, pero esto no constituye una garantía de disponibilidad legal, registral o comercial. Antes de publicar, conviene comprobar marcas, dominios y nombres de paquete en los mercados objetivo.

Esta es la **primera versión pública**, creada para crecer poco a poco con mejoras, correcciones y nuevas funciones. Puedes descargar el APK desde [`releases/Novera-Audio-v1.0.0.apk`](releases/Novera-Audio-v1.0.0.apk).

## Características

| Área | Implementación |
| --- | --- |
| Reproducción | ExoPlayer Media3 con cola local, reproducción, pausa, anterior, siguiente, aleatorio y repetición. |
| Segundo plano | Servicio Media3 con sesión persistente y notificación multimedia para cambiar de canción sin abrir la app. |
| Widget | Widget de pantalla de inicio con pista actual, estado, anterior, reproducir/pausar y siguiente. |
| Biblioteca del teléfono | Escaneo de MediaStore para descubrir música local disponible. |
| Memoria USB | Selector de documentos de Android para escoger archivos o recorrer una carpeta USB completa. |
| Formatos | MP3, M4A, AAC, FLAC, WAV, OGG, OPUS y WMA cuando el dispositivo proporcione un decodificador compatible. |
| Organización | Búsqueda por título, artista o álbum; favoritos y separación entre biblioteca del teléfono e importados. |
| Diseño | Tema oscuro futurista con fondo midnight, paneles grafito, acentos cian/violeta y tarjetas de vidrio tecnológico. |
| Animaciones | Intro animado de 3 segundos con el ícono oficial, halo pulsante, rotación sutil, revelado del nombre y transición hacia la biblioteca; además de entrada/salida del mini-reproductor, expansión del reproductor, cambios de color y transiciones de contenido. |
| Visualizador de reproducción | La barra de tiempo de Ahora suena combina el progreso exacto con ondas animadas por amplitud/FFT de la sesión de audio y un fallback fluido si el dispositivo no permite capturarla. |
| Navegación | Biblioteca implementada como una única `LazyColumn` real, con scroll hasta el final y navegación inferior independiente. |
| Temas | Cinco temas: Aurora, Obsidian, Nebula, Emerald y Copper, persistidos localmente dentro de Ajustes → Temas. |
| Audio avanzado | Ecualizador por bandas, presets del dispositivo, ajuste manual, realce de bajos, volumen percibido, audio espacial y estado de disponibilidad. |
| Bluetooth | Detección de salidas Bluetooth y acceso directo al panel de ajustes de Android para conectar o cambiar auriculares. |
| Configuración | Menú por categorías con pantallas internas independientes: Temas, Ecualización, Eliminar ruido, Mejoras de sonido, Auriculares Bluetooth, Biblioteca/USB y Acerca de Novera. |
| Playlists | Listas locales personalizadas como Viaje, Enamorados, Carretera o cualquier nombre, con canciones seleccionables, reproducción como cola y eliminación individual. |
| Novera Flow | Sesión inteligente offline que prioriza favoritos y completa la cola con el resto de la biblioteca en orden variable. |
| Novera Studio | Playlists inteligentes, marcadores, modo A-B, letras LRC, visualizador reactivo, perfiles por dispositivo, copia de seguridad local y asistente de voz. |
| Asistente de voz | Reconocimiento on-device con palabra de activación “Novera” para reproducir, pausar, cambiar canción, escanear biblioteca y gestionar playlists con la pantalla bloqueada. |
| Privacidad | Sin cuenta, sin servidor y sin sincronización externa. |
| Compatibilidad de código | Android Gradle Plugin 8.6.1, Kotlin 2.0.21, compileSdk 35, minSdk 24, Java/Kotlin JVM target 17. |

## Descargar e instalar

Descarga [`Novera-Audio-v1.0.0.apk`](releases/Novera-Audio-v1.0.0.apk) en un dispositivo Android e instálalo como APK de prueba. Es una versión debug para pruebas locales; antes de instalarla, Android puede pedir permiso para instalar aplicaciones desde el origen utilizado para descargar el archivo.

## Cómo usar una memoria USB

Conecta la memoria USB al teléfono mediante un adaptador OTG compatible. En Novera Audio, pulsa **Añadir** y selecciona **Importar carpeta / USB**. El selector de documentos de Android mostrará las ubicaciones disponibles; concede acceso a la carpeta que contiene la música. La aplicación recorrerá sus subcarpetas y conservará el permiso persistente que Android permita para volver a utilizar esos archivos.

También puedes seleccionar archivos concretos mediante **Seleccionar archivos**. Este método resulta útil cuando la memoria contiene muchas carpetas y solo quieres añadir una selección.

## Asistente de voz local

Activa el asistente desde **Ajustes → Novera Studio → Asistente de voz local**. Después de autorizar el micrófono, Novera Audio mantiene una notificación visible mientras escucha y puede recibir comandos con la pantalla bloqueada.

Ejemplos de comandos:

```text
Novera, pausa
Novera, continúa
Novera, siguiente
Novera, reproduce Despacito
Novera, escanéame la música
Novera, crea una playlist llamada Carretera
Novera, agrega esto a playlist Carretera
```

El reconocimiento está diseñado para funcionar localmente mediante el motor on-device de Android cuando el dispositivo lo ofrece. La aplicación no cambia silenciosamente a un servicio remoto si el reconocimiento local no está disponible. El teléfono completamente apagado no puede escuchar comandos; con pantalla bloqueada sí puede funcionar mientras el servicio foreground esté activo. La escucha continua consume batería y puede detenerse desde la notificación.

## Compilación local

Necesitas Android Studio reciente o las herramientas de línea de comandos de Android, Android SDK Platform 35, Build Tools 35.0.0 y **JDK 17**. El repositorio incluye Gradle Wrapper, por lo que no necesitas instalar Gradle globalmente.

En Linux o macOS, define `JAVA_HOME` apuntando a JDK 17 y ejecuta:

```bash
export JAVA_HOME=/ruta/a/jdk-17
./gradlew assembleDebug
```

En Windows PowerShell, configura `JAVA_HOME` con la ruta del JDK 17 y ejecuta:

```powershell
.\gradlew.bat assembleDebug
```

El APK debug se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para instalarlo con un dispositivo conectado y autorizado por ADB:

```bash
./gradlew installDebug
```

## Estructura principal

| Ruta | Propósito |
| --- | --- |
| `app/src/main/java/com/novera/audio/MainActivity.kt` | UI Compose, modelo de pista, ViewModel, escaneo, importación y controles Media3. |
| `app/src/main/AndroidManifest.xml` | Permisos de audio y actividad de lanzamiento. |
| `app/src/main/res/drawable/novera_audio_icon.png` | Ícono visual original de Novera Audio. |
| `app/build.gradle.kts` | Dependencias y configuración de Java 17/Kotlin. |
| `releases/Novera-Audio-debug.apk` | APK debug compilado localmente para pruebas. |

## Permisos

En Android 13 o superior se solicita `READ_MEDIA_AUDIO` y `POST_NOTIFICATIONS`. En versiones anteriores se utiliza `READ_EXTERNAL_STORAGE`. Para la reproducción persistente se declara un servicio de primer plano de tipo multimedia. Para memorias USB y ubicaciones seleccionadas manualmente se utiliza el Storage Access Framework de Android, que muestra el diálogo del sistema y evita solicitar acceso indiscriminado a todo el almacenamiento.

## Estado del prototipo

La versión entregada está orientada a pruebas locales y compila como APK debug. Es la primera versión pública de Novera Audio y seguirá mejorando progresivamente con nuevas funciones, optimizaciones y correcciones de compatibilidad. El asistente de voz se activa explícitamente desde Ajustes → Novera Studio, requiere permiso de micrófono y muestra una notificación persistente mientras escucha. La interfaz incluye un intro animado, scroll real de toda la biblioteca, mini-reproductor persistente sobre la navegación inferior, reproductor expandible con seek, aleatorio y repetición, barra de progreso con ondas de audio y transiciones Compose. La actualización añade un servicio Media3 para la notificación de controles, un widget de pantalla de inicio, cinco temas persistentes, playlists locales personalizadas, Novera Flow, Novera Studio y un centro de audio avanzado organizado por categorías internas dentro de Configuración. Novera Studio incluye reglas de playlists inteligentes, marcadores por canción, modo A-B persistente, letras LRC locales, visualizador reactivo, perfiles de salida y exportación/restauración JSON. La reducción de ruido se muestra como experimental y dependiente del dispositivo porque la API estándar de Android está enfocada principalmente a captura de voz, no a limpiar música reproducida. La escucha con teléfono apagado no es posible; con pantalla bloqueada funciona mientras el servicio foreground siga activo y Android permita el reconocimiento on-device. El siguiente salto de producto sería añadir edición de metadatos, playlists avanzadas y carátulas.

## Referencias técnicas

[1]: https://developer.android.com/media/media3/exoplayer/hello-world "Android Developers — Media3 ExoPlayer"

[2]: https://developer.android.com/develop/background-work/services/fgs/service-types "Android Developers — Foreground service types"

[3]: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start "Android Developers — Background start restrictions"

[4]: https://developer.android.com/reference/android/media/audiofx/Visualizer "Android Developers — Visualizer"

## Licencia

Este repositorio no añade una licencia de distribución nueva. Define la licencia que prefieras antes de publicar el proyecto o el APK en una tienda.
