package com.novera.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import java.text.Normalizer

object VoiceAssistant {
    fun execute(context: Context, spoken: String): String? {
        val normalized = normalize(spoken)
        val wakeWord = listOf("novera", "oye novera", "asistente")
        if (wakeWord.none { normalized.contains(it) }) return null
        val command = wakeWord.fold(normalized) { text, word -> text.replace(word, " ") }.trim()
        val player = PlaybackEngine.player(context)
        return when {
            command.contains("escane") || command.contains("buscar musica") || command.contains("buscar canciones") -> {
                val count = VoiceLibrary.load(context).size
                "Encontré $count canciones en tu biblioteca local"
            }
            command.contains("siguiente") || command.contains("otra cancion") || command.contains("otra pista") -> {
                player.seekToNextMediaItem(); "Pasando a la siguiente canción"
            }
            command.contains("anterior") || command.contains("cancion anterior") -> {
                player.seekToPreviousMediaItem(); "Regresando a la canción anterior"
            }
            command == "pausa" || command.contains("pausa la musica") || command.contains("deten la musica") -> {
                player.pause(); "Música pausada"
            }
            command.contains("continua") || command.contains("reanuda") || command.contains("reproduce") && command.length < 18 -> {
                player.play(); "Reanudando la música"
            }
            command.contains("repite esta") || command.contains("repetir esta") -> {
                player.repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE; "Repitiendo esta canción"
            }
            command.contains("agrega") && command.contains("playlist") -> addCurrentToPlaylist(context, command, player)
            command.contains("crea") && command.contains("playlist") -> createPlaylist(context, command)
            command.startsWith("reproduce ") || command.startsWith("reproducime ") || command.startsWith("pon ") -> {
                val query = command.substringAfter(' ').trim()
                playMatching(context, player, query)
            }
            else -> "No reconocí ese comando. Prueba diciendo: Novera, reproduce una canción"
        }
    }

    private fun playMatching(context: Context, player: ExoPlayer, query: String): String {
        val library = VoiceLibrary.load(context)
        val matches = library.filter { "${it.title} ${it.artist} ${it.album}".contains(query, ignoreCase = true) }
        val selected = matches.firstOrNull() ?: return "No encontré una canción llamada $query"
        val index = library.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
        PlaybackEngine.startService(context)
        player.setMediaItems(library.map(::mediaItem), index, 0L)
        player.prepare()
        player.play()
        return "Reproduciendo ${selected.title}"
    }

    private fun addCurrentToPlaylist(context: Context, command: String, player: ExoPlayer): String {
        val currentId = player.currentMediaItem?.mediaId ?: return "No hay una canción reproduciéndose"
        val track = VoiceLibrary.load(context).firstOrNull { it.id == currentId } ?: return "No pude identificar la canción actual"
        val name = extractPlaylistName(command) ?: return "Dime el nombre de la playlist"
        val store = PlaylistStore(context)
        val playlists = store.load().toMutableList()
        val index = playlists.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        if (index >= 0) {
            val target = playlists[index]
            playlists[index] = target.copy(trackIds = (target.trackIds + track.id).distinct())
        } else {
            playlists += Playlist(java.util.UUID.randomUUID().toString(), name, listOf(track.id))
        }
        store.save(playlists)
        return "Añadí ${track.title} a $name"
    }

    private fun createPlaylist(context: Context, command: String): String {
        val name = extractPlaylistName(command) ?: return "Dime el nombre de la playlist"
        val store = PlaylistStore(context)
        val existing = store.load()
        if (existing.any { it.name.equals(name, ignoreCase = true) }) return "La playlist $name ya existe"
        store.save(existing + Playlist(java.util.UUID.randomUUID().toString(), name))
        return "Creé la playlist $name"
    }

    private fun extractPlaylistName(command: String): String? {
        val match = Regex("playlist(?:a)?(?: llamada| llamada como| con el nombre)?\\s+(.+)").find(command)
        return match?.groupValues?.getOrNull(1)?.trim()?.trim('.', '"', '\'')?.ifBlank { null }
    }

    private fun mediaItem(track: Track): MediaItem = MediaItem.Builder()
        .setMediaId(track.id)
        .setUri(track.uri)
        .setMediaMetadata(MediaMetadata.Builder().setTitle(track.title).setArtist(track.artist).setAlbumTitle(track.album).build())
        .build()

    private fun normalize(value: String): String = Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD).replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").replace(Regex("[^a-z0-9ñ ]"), " ").replace(Regex("\\s+"), " ").trim()
}

object VoiceLibrary {
    fun load(context: Context): List<Track> {
        val phone = queryPhoneTracks(context)
        val prefs = context.getSharedPreferences("novera_library", Context.MODE_PRIVATE)
        val imported = prefs.getStringSet("uris", emptySet()).orEmpty().mapNotNull { readTrack(context, Uri.parse(it)) }
        return (phone + imported).distinctBy { it.id }
    }

    private fun queryPhoneTracks(context: Context): List<Track> {
        val result = mutableListOf<Track>()
        val projection = arrayOf(android.provider.MediaStore.Audio.Media._ID, android.provider.MediaStore.Audio.Media.TITLE, android.provider.MediaStore.Audio.Media.ARTIST, android.provider.MediaStore.Audio.Media.ALBUM, android.provider.MediaStore.Audio.Media.DURATION)
        val collection = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        context.contentResolver.query(collection, projection, "${android.provider.MediaStore.Audio.Media.IS_MUSIC} != 0", null, "${android.provider.MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC")?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DURATION)
            while (cursor.moveToNext()) {
                val uri = Uri.withAppendedPath(collection, cursor.getLong(idColumn).toString())
                result += Track(uri.toString(), cursor.getString(titleColumn) ?: "Pista sin título", cursor.getString(artistColumn) ?: "Artista desconocido", cursor.getString(albumColumn) ?: "Biblioteca local", uri.toString(), cursor.getLong(durationColumn), TrackSource.PHONE)
            }
        }
        return result
    }

    private fun readTrack(context: Context, uri: Uri): Track? {
        val retriever = MediaMetadataRetriever()
        return runCatching {
            retriever.setDataSource(context, uri)
            val fallback = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "Pista sin título"
            Track(uri.toString(), retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: fallback, retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Artista desconocido", retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Biblioteca local", uri.toString(), retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L, TrackSource.IMPORTED)
        }.getOrNull().also { retriever.release() }
    }
}
