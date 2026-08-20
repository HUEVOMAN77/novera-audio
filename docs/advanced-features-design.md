# Diseño de funciones avanzadas de Novera Audio

## Playlists inteligentes

Se almacenarán reglas locales simples y reproducibles: favoritos, duración mínima/máxima, artista, álbum, género disponible en metadatos y canciones menos escuchadas cuando exista historial. Cada regla producirá una lista virtual que se convierte en cola Media3 al iniciar.

## Novera Studio

Será una categoría independiente dentro de Ajustes. Contendrá marcadores, modo A-B, letras LRC, visualizador y utilidades de copia de seguridad, evitando saturar la pantalla principal.

## Marcadores y modo A-B

Los marcadores se guardarán por `trackId` con posición en milisegundos y nombre. El modo A-B mantendrá dos posiciones en memoria/persistencia y el ViewModel saltará a A cuando la posición alcance B.

## Perfiles por dispositivo

Se guardarán presets de audio por una clave derivada del nombre/tipo de la salida activa. Como Android controla el enrutamiento Bluetooth y algunos efectos dependen del fabricante, el módulo aplicará los ajustes disponibles y mostrará compatibilidad real.

## Letras y visualizador

Los archivos `.lrc` se seleccionarán mediante el selector de documentos y se asociarán al `trackId`. El parser leerá timestamps `[mm:ss.xx]`. El visualizador será un componente Canvas local animado con barras pseudo-reactivas basadas en la posición y el estado de reproducción, sin servidor ni permisos adicionales.

## Copia de seguridad

La exportación generará un JSON local con playlists, reglas, favoritos, temas, marcadores, perfiles y asociaciones LRC. La restauración utilizará `OpenDocument`/`CreateDocument`, validará el formato y fusionará de forma segura sin borrar la biblioteca musical ni copiar archivos de audio.
