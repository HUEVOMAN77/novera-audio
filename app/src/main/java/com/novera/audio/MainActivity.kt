package com.novera.audio

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import java.util.UUID
import kotlin.math.max

private val Midnight: Color @Composable get() = LocalNoveraPalette.current.midnight
private val DeepPanel: Color @Composable get() = LocalNoveraPalette.current.deepPanel
private val RaisedPanel: Color @Composable get() = LocalNoveraPalette.current.raisedPanel
private val Cyan: Color @Composable get() = LocalNoveraPalette.current.cyan
private val Violet: Color @Composable get() = LocalNoveraPalette.current.violet
private val SoftText: Color @Composable get() = LocalNoveraPalette.current.softText
private val MutedText: Color @Composable get() = LocalNoveraPalette.current.mutedText

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NoveraApp() }
    }
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val uri: String,
    val durationMs: Long,
    val source: TrackSource
)

enum class TrackSource { PHONE, IMPORTED }

data class PlayerState(
    val tracks: List<Track> = emptyList(),
    val importedTracks: List<Track> = emptyList(),
    val query: String = "",
    val currentId: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isScanning: Boolean = false,
    val playlists: List<Playlist> = emptyList(),
    val notice: String? = null
)

class PlayerViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("novera_library", Context.MODE_PRIVATE)
    private val player: ExoPlayer = PlaybackEngine.player(context)
    private val audioEffects = AudioEffectsController(context, player)
    private val audioVisualizer = AudioVisualizerController()
    private val playlistStore = PlaylistStore(context)
    private val advancedStore = AdvancedStore(context)
    private val _advancedState = MutableStateFlow(advancedStore.load())
    val advancedState: StateFlow<AdvancedState> = _advancedState.asStateFlow()
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()
    val audioState: StateFlow<AudioFxState> = audioEffects.state
    val visualizerBars: StateFlow<List<Float>> = audioVisualizer.bars

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                syncWidget()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val id = mediaItem?.mediaId
                _state.update { it.copy(currentId = id, durationMs = player.duration.takeIf { value -> value > 0 } ?: 0L) }
                _advancedState.update { it.copy(currentLyrics = it.lrcByTrack[id].orEmpty()) }
                syncWidget()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update { it.copy(durationMs = player.duration.takeIf { value -> value > 0 } ?: 0L) }
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.update { it.copy(isPlaying = false, notice = "No se pudo reproducir esta pista en el dispositivo") }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                audioEffects.onAudioSessionIdChanged(audioSessionId)
                audioVisualizer.attach(audioSessionId)
            }
        })
        _state.update { it.copy(playlists = playlistStore.load()) }
        refreshLibrary()
        viewModelScope.launch {
            while (true) {
                val position = player.currentPosition.coerceAtLeast(0L)
                val advanced = _advancedState.value
                if (advanced.loopEnabled && advanced.loopA != null && advanced.loopB != null && position >= advanced.loopB) player.seekTo(advanced.loopA)
                _state.update {
                    it.copy(
                        positionMs = position,
                        durationMs = player.duration.takeIf { duration -> duration > 0 } ?: it.durationMs
                    )
                }
                delay(500)
            }
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, notice = null) }
            val phone = withContext(Dispatchers.IO) { queryPhoneTracks(context.contentResolver) }
            val imported = withContext(Dispatchers.IO) { loadImportedTracks() }
            _state.update { it.copy(tracks = phone, importedTracks = imported, isScanning = false, notice = "Biblioteca actualizada") }
        }
    }

    fun setQuery(query: String) = _state.update { it.copy(query = query) }

    fun importUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val additions = withContext(Dispatchers.IO) { uris.mapNotNull { readTrack(it, TrackSource.IMPORTED) } }
            saveImportedTracks((_state.value.importedTracks + additions).distinctBy { it.uri })
            _state.update { it.copy(importedTracks = loadImportedTracks(), notice = "${additions.size} archivo(s) añadido(s)") }
        }
    }

    fun importFolder(treeUri: Uri) {
        viewModelScope.launch {
            val files = withContext(Dispatchers.IO) { collectAudioFiles(DocumentFile.fromTreeUri(context, treeUri)) }
            importUris(files)
        }
    }

    fun play(track: Track) {
        PlaybackEngine.startService(context)
        val queue = allTracks()
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.setMediaItems(queue.map(::toMediaItem), index, 0L)
        player.prepare()
        player.play()
        _state.update { it.copy(currentId = track.id, isPlaying = true, durationMs = track.durationMs, positionMs = 0L) }
    }

    fun togglePlay() {
        if (player.mediaItemCount == 0) {
            allTracks().firstOrNull()?.let(::play)
        } else if (player.isPlaying) {
            player.pause()
        } else {
            PlaybackEngine.startService(context)
            player.play()
        }
    }

    fun seekTo(positionMs: Long) = player.seekTo(positionMs.coerceAtLeast(0L))
    fun seekToBookmark(positionMs: Long) = player.seekTo(positionMs.coerceAtLeast(0L))

    fun next() = player.seekToNextMediaItem()

    fun previous() {
        if (player.currentPosition > 3_000L) player.seekTo(0L) else player.seekToPreviousMediaItem()
    }

    fun toggleShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
        _state.update { it.copy(isShuffle = player.shuffleModeEnabled) }
    }

    fun cycleRepeat() {
        val next = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = next
        _state.update { it.copy(repeatMode = next) }
    }

    fun createPlaylist(name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        val playlist = Playlist(UUID.randomUUID().toString(), cleanName)
        val updated = _state.value.playlists + playlist
        playlistStore.save(updated)
        _state.update { it.copy(playlists = updated, notice = "Playlist creada") }
    }

    fun deletePlaylist(playlistId: String) {
        val updated = _state.value.playlists.filterNot { it.id == playlistId }
        playlistStore.save(updated)
        _state.update { it.copy(playlists = updated, notice = "Playlist eliminada") }
    }

    fun addToPlaylist(playlistId: String, track: Track) {
        val updated = _state.value.playlists.map { playlist ->
            if (playlist.id == playlistId && track.id !in playlist.trackIds) playlist.copy(trackIds = playlist.trackIds + track.id) else playlist
        }
        playlistStore.save(updated)
        _state.update { it.copy(playlists = updated, notice = "Añadida a playlist") }
    }

    fun removeFromPlaylist(playlistId: String, trackId: String) {
        val updated = _state.value.playlists.map { playlist ->
            if (playlist.id == playlistId) playlist.copy(trackIds = playlist.trackIds - trackId) else playlist
        }
        playlistStore.save(updated)
        _state.update { it.copy(playlists = updated, notice = "Quitada de playlist") }
    }

    fun playPlaylist(playlist: Playlist) {
        val queue = playlist.trackIds.mapNotNull { id -> allTracks().firstOrNull { it.id == id } }
        if (queue.isEmpty()) {
            _state.update { it.copy(notice = "Esta playlist todavía no tiene canciones disponibles") }
            return
        }
        playQueue(queue)
    }

    fun playFlow() {
        val library = allTracks()
        if (library.isEmpty()) {
            _state.update { it.copy(notice = "Añade canciones para iniciar Novera Flow") }
            return
        }
        val favoriteIds = prefs.getStringSet("favorites", emptySet()).orEmpty()
        val favorites = library.filter { it.id in favoriteIds }.shuffled()
        val rest = library.filterNot { it.id in favoriteIds }.shuffled()
        playQueue(favorites + rest)
        _state.update { it.copy(notice = "Novera Flow creó una sesión local") }
    }

    private fun playQueue(queue: List<Track>) {
        PlaybackEngine.startService(context)
        player.setMediaItems(queue.map(::toMediaItem), 0, 0L)
        player.prepare()
        player.play()
        _state.update { it.copy(currentId = queue.first().id, isPlaying = true, durationMs = queue.first().durationMs, positionMs = 0L) }
    }

    fun toggleFavorite(track: Track) {
        val favorites = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (!favorites.add(track.id)) favorites.remove(track.id)
        prefs.edit().putStringSet("favorites", favorites).apply()
        _state.update { it.copy(notice = if (track.id in favorites) "Añadido a favoritos" else "Quitado de favoritos") }
    }

    fun isFavorite(track: Track): Boolean = prefs.getStringSet("favorites", emptySet())?.contains(track.id) == true

    fun dismissNotice() = _state.update { it.copy(notice = null) }

    fun setEqualizerBand(index: Short, levelMb: Short) = audioEffects.setBandLevel(index, levelMb)
    fun applyEqualizerPreset(index: Short) = audioEffects.applyPreset(index)
    fun toggleNoiseReduction(enabled: Boolean) = audioEffects.toggleNoiseReduction(enabled)
    fun toggleBassBoost(enabled: Boolean) = audioEffects.toggleBassBoost(enabled)
    fun toggleLoudness(enabled: Boolean) = audioEffects.toggleLoudness(enabled)
    fun toggleSpatial(enabled: Boolean) = audioEffects.toggleSpatial(enabled)
    fun dismissAudioMessage() = audioEffects.clearMessage()

    fun startVoiceAssistant() = VoiceAssistantService.start(context)
    fun stopVoiceAssistant() = VoiceAssistantService.stop(context)

    fun createSmartPlaylist(name: String, mode: SmartRuleMode, artist: String = "") {
        val item = SmartPlaylist(UUID.randomUUID().toString(), name.trim().ifBlank { "Smart Playlist" }, mode, artist.trim())
        val updated = _advancedState.value.smartPlaylists + item
        saveAdvanced(_advancedState.value.copy(smartPlaylists = updated))
        _state.update { it.copy(notice = "Playlist inteligente creada") }
    }

    fun deleteSmartPlaylist(id: String) = saveAdvanced(_advancedState.value.copy(smartPlaylists = _advancedState.value.smartPlaylists.filterNot { it.id == id }))

    fun playSmartPlaylist(item: SmartPlaylist) {
        val library = allTracks()
        val favorites = prefs.getStringSet("favorites", emptySet()).orEmpty()
        val queue = when (item.mode) {
            SmartRuleMode.FAVORITES -> library.filter { it.id in favorites }
            SmartRuleMode.LONGEST -> library.sortedByDescending { it.durationMs }
            SmartRuleMode.ARTIST -> library.filter { it.artist.contains(item.artistQuery, ignoreCase = true) }
        }
        if (queue.isEmpty()) _state.update { it.copy(notice = "No hay canciones que coincidan con esta regla") } else playQueue(queue)
    }

    fun addBookmark(title: String, positionMs: Long) {
        val trackId = _state.value.currentId ?: return
        val entry = Bookmark(UUID.randomUUID().toString(), trackId, title.trim().ifBlank { "Marcador" }, positionMs)
        val updated = _advancedState.value.bookmarks.toMutableMap()
        updated[trackId] = updated[trackId].orEmpty() + entry
        saveAdvanced(_advancedState.value.copy(bookmarks = updated))
    }

    fun removeBookmark(trackId: String, bookmarkId: String) {
        val updated = _advancedState.value.bookmarks.toMutableMap()
        updated[trackId] = updated[trackId].orEmpty().filterNot { it.id == bookmarkId }
        saveAdvanced(_advancedState.value.copy(bookmarks = updated))
    }

    fun setLoopA(positionMs: Long) = saveAdvanced(_advancedState.value.copy(loopA = positionMs))
    fun setLoopB(positionMs: Long) = saveAdvanced(_advancedState.value.copy(loopB = positionMs))
    fun toggleLoop(enabled: Boolean) = saveAdvanced(_advancedState.value.copy(loopEnabled = enabled))
    fun clearLoop() = saveAdvanced(_advancedState.value.copy(loopA = null, loopB = null, loopEnabled = false))

    fun importLyrics(uri: Uri, trackId: String) {
        viewModelScope.launch {
            val lines = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { LrcParser.parse(it.readText()) }.orEmpty() }
            val updated = _advancedState.value.lrcByTrack.toMutableMap()
            updated[trackId] = lines
            saveAdvanced(_advancedState.value.copy(lrcByTrack = updated, currentLyrics = lines))
            _state.update { it.copy(notice = if (lines.isEmpty()) "No se encontraron líneas LRC" else "Letras importadas") }
        }
    }

    fun saveDeviceProfile(profile: DeviceProfile) {
        val updated = (_advancedState.value.deviceProfiles.filterNot { it.key == profile.key } + profile)
        saveAdvanced(_advancedState.value.copy(deviceProfiles = updated))
    }

    fun saveCurrentDeviceProfile(name: String) {
        val fx = audioEffects.state.value
        saveDeviceProfile(DeviceProfile("local-output", name.trim().ifBlank { "Mi salida de audio" }, fx.bands.associate { it.index.toInt() to it.levelMb.toInt() }, fx.bassBoostEnabled, fx.loudnessEnabled, fx.spatialEnabled))
    }

    fun applyDeviceProfile(profile: DeviceProfile) {
        profile.bandLevels.forEach { (index, level) -> audioEffects.setBandLevel(index.toShort(), level.toShort()) }
        audioEffects.toggleBassBoost(profile.bassBoost)
        audioEffects.toggleLoudness(profile.loudness)
        audioEffects.toggleSpatial(profile.spatial)
        _state.update { it.copy(notice = "Perfil ${profile.name} aplicado") }
    }

    fun exportBackup(uri: Uri, themeOrdinal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = advancedStore.backup(_state.value.playlists, _advancedState.value, prefs.getStringSet("favorites", emptySet()).orEmpty(), themeOrdinal)
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json.toString(2)) }
            _state.update { it.copy(notice = "Copia de seguridad exportada") }
        }
    }

    fun restoreBackup(uri: Uri, onTheme: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { JSONObject(it.readText()) } ?: return@launch
            val backup = advancedStore.restore(json)
            playlistStore.save(backup.playlists)
            prefs.edit().putStringSet("favorites", backup.favorites).apply()
            saveAdvanced(backup.advanced)
            _state.update { it.copy(playlists = backup.playlists, notice = "Copia restaurada") }
            withContext(Dispatchers.Main) { onTheme(backup.themeOrdinal) }
        }
    }

    private fun saveAdvanced(state: AdvancedState) {
        advancedStore.save(state)
        _advancedState.value = state
    }

    private fun syncWidget() {
        val track = allTracks().firstOrNull { it.id == player.currentMediaItem?.mediaId }
        if (track != null) WidgetStateStore.save(context, track.title, track.artist, player.isPlaying)
    }

    fun currentTrack(): Track? = allTracks().firstOrNull { it.id == _state.value.currentId }

    private fun allTracks(): List<Track> = (_state.value.tracks + _state.value.importedTracks).distinctBy { it.id }

    private fun toMediaItem(track: Track): MediaItem = MediaItem.Builder()
        .setMediaId(track.id)
        .setUri(track.uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .build()
        )
        .build()

    private fun loadImportedTracks(): List<Track> = prefs.getStringSet("uris", emptySet())?.mapNotNull { readTrack(Uri.parse(it), TrackSource.IMPORTED) } ?: emptyList()

    private fun saveImportedTracks(tracks: List<Track>) {
        prefs.edit().putStringSet("uris", tracks.map { it.uri }.toSet()).apply()
    }

    private fun collectAudioFiles(root: DocumentFile?): List<Uri> {
        if (root == null) return emptyList()
        return root.listFiles().flatMap { file ->
            when {
                file.isDirectory -> collectAudioFiles(file)
                file.isFile && isAudioFile(file.name, file.type) -> listOf(file.uri)
                else -> emptyList()
            }
        }
    }

    private fun readTrack(uri: Uri, source: TrackSource): Track? {
        val resolver = context.contentResolver
        val fallback = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')?.ifBlank { "Pista sin título" } ?: "Pista sin título"
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.ifBlank { fallback } ?: fallback
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.ifBlank { "Artista desconocido" } ?: "Artista desconocido"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.ifBlank { "Biblioteca local" } ?: "Biblioteca local"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            Track(uri.toString(), title, artist, album, uri.toString(), duration, source)
        } catch (_: Exception) {
            if (isAudioFile(fallback, resolver.getType(uri))) Track(uri.toString(), fallback, "Artista desconocido", "Biblioteca local", uri.toString(), 0L, source) else null
        } finally {
            retriever.release()
        }
    }

    override fun onCleared() {
        audioEffects.release()
        audioVisualizer.release()
        super.onCleared()
    }
}

private fun queryPhoneTracks(resolver: ContentResolver): List<Track> {
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.MIME_TYPE,
        MediaStore.Audio.Media.DISPLAY_NAME
    )
    val result = mutableListOf<Track>()
    resolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        "${MediaStore.Audio.Media.IS_MUSIC} != 0",
        null,
        "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        while (cursor.moveToNext()) {
            val mime = cursor.getString(mimeColumn)
            val name = cursor.getString(nameColumn) ?: "audio"
            if (isAudioFile(name, mime)) {
                val id = cursor.getLong(idColumn)
                val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
                result += Track(
                    id = uri.toString(),
                    title = cursor.getString(titleColumn)?.ifBlank { name.substringBeforeLast('.') } ?: name.substringBeforeLast('.'),
                    artist = cursor.getString(artistColumn)?.ifBlank { "Artista desconocido" } ?: "Artista desconocido",
                    album = cursor.getString(albumColumn)?.ifBlank { "Biblioteca del teléfono" } ?: "Biblioteca del teléfono",
                    uri = uri.toString(),
                    durationMs = cursor.getLong(durationColumn),
                    source = TrackSource.PHONE
                )
            }
        }
    }
    return result
}

private fun isAudioFile(name: String?, mime: String?): Boolean {
    val extension = name?.substringAfterLast('.', "")?.lowercase(Locale.US)
    return mime?.startsWith("audio/") == true || extension in setOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "opus", "wma")
}


@Composable
private fun IntroScreen(onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(if (started) 1f else 0.68f, animationSpec = tween(900), label = "introLogoScale")
    val logoAlpha by animateFloatAsState(if (started) 1f else 0f, animationSpec = tween(700), label = "introLogoAlpha")
    val transition = rememberInfiniteTransition(label = "introMotion")
    val pulse by transition.animateFloat(initialValue = 0.92f, targetValue = 1.08f, animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "introPulse")
    val haloAlpha by transition.animateFloat(initialValue = 0.22f, targetValue = 0.62f, animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "introHalo")
    val orbit by transition.animateFloat(initialValue = -7f, targetValue = 7f, animationSpec = infiniteRepeatable(tween(1900), RepeatMode.Reverse), label = "introOrbit")
    LaunchedEffect(Unit) {
        started = true
        delay(3000)
        onFinished()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF1C2F4B), Midnight, Color(0xFF050812)))),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Cyan.copy(alpha = 0.05f), Violet.copy(alpha = 0.12f), Color.Transparent))))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer { alpha = logoAlpha; scaleX = logoScale; scaleY = logoScale }) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(230.dp)) {
                Box(modifier = Modifier.size(190.dp).graphicsLayer { alpha = haloAlpha; scaleX = pulse; scaleY = pulse }.clip(CircleShape).background(Brush.radialGradient(listOf(Cyan.copy(alpha = 0.32f), Violet.copy(alpha = 0.18f), Color.Transparent))))
                Box(modifier = Modifier.size(164.dp).graphicsLayer { rotationZ = orbit }.clip(RoundedCornerShape(48.dp)).background(Brush.linearGradient(listOf(Cyan.copy(alpha = 0.85f), Violet.copy(alpha = 0.9f)))).shadow(26.dp, RoundedCornerShape(48.dp)), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(R.drawable.novera_audio_icon), contentDescription = "Novera Audio", modifier = Modifier.size(142.dp).clip(RoundedCornerShape(42.dp)))
                }
                Box(modifier = Modifier.size(206.dp).graphicsLayer { rotationZ = -orbit * 0.7f }.border(1.dp, Cyan.copy(alpha = haloAlpha), CircleShape))
            }
            Spacer(Modifier.height(18.dp))
            Text("NOVERA", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("AUDIO", style = MaterialTheme.typography.titleMedium, color = Cyan, letterSpacing = 6.sp)
            Spacer(Modifier.height(22.dp))
            AnimatedVisibility(visible = started, enter = fadeIn(tween(850)) + slideInVertically(tween(850)) { it / 2 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LOCAL / UNBOUND", style = MaterialTheme.typography.labelMedium, color = SoftText, letterSpacing = 2.4.sp)
                    Spacer(Modifier.height(18.dp))
                    LinearProgressIndicator(progress = { 1f }, modifier = Modifier.width(110.dp).height(2.dp), color = Cyan, trackColor = Color.White.copy(alpha = 0.12f))
                }
            }
        }
    }
}

@Composable
private fun NoveraApp(vm: PlayerViewModel = viewModel()) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()
    val audioState by vm.audioState.collectAsState()
    val visualizerBars by vm.visualizerBars.collectAsState()
    val advancedState by vm.advancedState.collectAsState()
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    val requestedPermissions = remember(audioPermission) {
        buildList {
            add(audioPermission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray()
    }
    var showIntro by remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { vm.refreshLibrary() }
    LaunchedEffect(showIntro) {
        val missingAudio = ContextCompat.checkSelfPermission(context, audioPermission) != PackageManager.PERMISSION_GRANTED
        val missingNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (!showIntro && (missingAudio || missingNotifications)) permissionLauncher.launch(requestedPermissions)
    }
    val themeStore = remember { ThemeStore(context) }
    var activeTheme by remember { mutableStateOf(themeStore.load()) }
    var voiceEnabled by remember { mutableStateOf(VoiceAssistantService.isEnabled(context)) }
    val voicePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        voiceEnabled = granted
        if (granted) vm.startVoiceAssistant() else vm.stopVoiceAssistant()
    }
    var selectedTab by remember { mutableStateOf(0) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var trackForPlaylist by remember { mutableStateOf<Track?>(null) }
    var showImport by remember { mutableStateOf(false) }
    val multiplePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try {
                vm.getApplication<android.app.Application>().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { }
        }
        vm.importUris(uris)
    }
    val lrcPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val trackId = state.currentId
        if (uri != null && trackId != null) vm.importLyrics(uri, trackId)
    }
    val backupExportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportBackup(uri, activeTheme.ordinal)
    }
    val backupImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.restoreBackup(uri) { ordinal ->
            val theme = NoveraTheme.values().getOrElse(ordinal) { NoveraTheme.AURORA }
            activeTheme = theme
            themeStore.save(theme)
        }
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                vm.getApplication<android.app.Application>().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } catch (_: Exception) { }
            vm.importFolder(uri)
        }
    }
    val currentTrack = remember(state.tracks, state.importedTracks, state.currentId) {
        (state.tracks + state.importedTracks).firstOrNull { it.id == state.currentId }
    }
    val filtered = remember(state.tracks, state.importedTracks, state.query, selectedTab) {
        val source = when (selectedTab) {
            2 -> state.importedTracks
            else -> state.tracks + state.importedTracks
        }.distinctBy { it.id }
        if (state.query.isBlank()) source else source.filter { track ->
            "${track.title} ${track.artist} ${track.album}".contains(state.query, ignoreCase = true)
        }
    }

    CompositionLocalProvider(LocalNoveraPalette provides activeTheme.palette) {
        MaterialTheme(colorScheme = noveraColors()) {
        if (showIntro) {
            IntroScreen(onFinished = { showIntro = false })
        } else {
            Surface(modifier = Modifier.fillMaxSize(), color = Midnight) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = Midnight,
                    topBar = {
                        NoveraTopBar(
                            onRefresh = vm::refreshLibrary,
                            onImport = { showImport = true },
                            onSettings = { selectedTab = 3 }
                        )
                    },
                    bottomBar = {
                        Column(modifier = Modifier.background(DeepPanel)) {
                            AnimatedVisibility(
                                visible = currentTrack != null,
                                enter = fadeIn(tween(260)) + scaleIn(tween(260)),
                                exit = fadeOut(tween(180)) + scaleOut(tween(180))
                            ) {
                                MiniPlayerBar(
                                    track = currentTrack,
                                    state = state,
                                    onPlayPause = vm::togglePlay,
                                    onNext = vm::next,
                                    onOpen = { selectedTab = 0 }
                                )
                            }
                            NavigationBar(
                                modifier = Modifier.navigationBarsPadding(),
                                containerColor = DeepPanel,
                                tonalElevation = 0.dp
                            ) {
                            NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.LibraryMusic, null) }, label = { Text("Biblioteca") })
                            NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.QueueMusic, null) }, label = { Text("Playlists") })
                            NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Usb, null) }, label = { Text("Importados") })
                            NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Ajustes") })
                            }
                        }
                    }
                ) { padding ->
                    when (selectedTab) {
                        1 -> PlaylistsScreen(
                            playlists = state.playlists,
                            tracks = (state.tracks + state.importedTracks).distinctBy { it.id },
                            onCreate = { showCreatePlaylist = true },
                            onPlayFlow = vm::playFlow,
                            onDelete = vm::deletePlaylist,
                            onPlay = vm::playPlaylist,
                            onAddTrack = { trackForPlaylist = it },
                            onRemoveTrack = vm::removeFromPlaylist
                        )
                        3 -> SettingsScreen(
                            selected = activeTheme,
                            onThemeChange = {
                                activeTheme = it
                                themeStore.save(it)
                            },
                            audioState = audioState,
                            onBandChange = vm::setEqualizerBand,
                            onPreset = vm::applyEqualizerPreset,
                            onNoiseReduction = vm::toggleNoiseReduction,
                            onBassBoost = vm::toggleBassBoost,
                            onLoudness = vm::toggleLoudness,
                            onSpatial = vm::toggleSpatial,
                            advancedState = advancedState,
                            currentTrack = currentTrack,
                            positionMs = state.positionMs,
                            onCreateSmartPlaylist = vm::createSmartPlaylist,
                            onPlaySmartPlaylist = vm::playSmartPlaylist,
                            onDeleteSmartPlaylist = vm::deleteSmartPlaylist,
                            onAddBookmark = vm::addBookmark,
                            onRemoveBookmark = vm::removeBookmark,
                            onJumpBookmark = vm::seekToBookmark,
                            onSetLoopA = vm::setLoopA,
                            onSetLoopB = vm::setLoopB,
                            onToggleLoop = vm::toggleLoop,
                            onClearLoop = vm::clearLoop,
                            onImportLyrics = { lrcPicker.launch(arrayOf("text/*", "application/octet-stream")) },
                            onExportBackup = { backupExportPicker.launch("novera-backup.json") },
                            onImportBackup = { backupImportPicker.launch(arrayOf("application/json", "text/*")) },
                            onSaveDeviceProfile = vm::saveCurrentDeviceProfile,
                            onApplyDeviceProfile = vm::applyDeviceProfile,
                            voiceEnabled = voiceEnabled,
                            onVoiceToggle = { enabled ->
                                if (enabled) voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                else { voiceEnabled = false; vm.stopVoiceAssistant() }
                            },
                            onBack = { selectedTab = 0 }
                        )
                        else -> LibraryScreen(
                            padding = padding,
                            tracks = filtered,
                            state = state,
                            visualizerBars = visualizerBars,
                            isFavorite = vm::isFavorite,
                            onQueryChange = vm::setQuery,
                            onPlay = vm::play,
                            onFavorite = vm::toggleFavorite,
                            onAddToPlaylist = { trackForPlaylist = it },
                            onOpenImport = { showImport = true },
                            onRefresh = vm::refreshLibrary,
                            onPlayPause = vm::togglePlay,
                            onPrevious = vm::previous,
                            onNext = vm::next,
                            onSeek = vm::seekTo,
                            onShuffle = vm::toggleShuffle,
                            onRepeat = vm::cycleRepeat
                        )
                    }
                }
            }
                }
        }
    }
    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylist = false },
            onCreate = { name -> showCreatePlaylist = false; vm.createPlaylist(name) }
        )
    }
    trackForPlaylist?.let { track ->
        PlaylistPickerDialog(
            track = track,
            playlists = state.playlists,
            onDismiss = { trackForPlaylist = null },
            onChoose = { playlistId -> trackForPlaylist = null; vm.addToPlaylist(playlistId, track) },
            onCreate = { showCreatePlaylist = true; trackForPlaylist = null }
        )
    }
    if (showImport) {
        ImportDialog(
            onDismiss = { showImport = false },
            onFiles = { showImport = false; multiplePicker.launch(arrayOf("audio/*")) },
            onFolder = { showImport = false; folderPicker.launch(null) }
        )
    }
    state.notice?.let { notice ->
        LaunchedEffect(notice) {
            delay(2300)
            vm.dismissNotice()
        }
        NoticeBar(notice)
    }
    audioState.message?.let { message ->
        LaunchedEffect(message) {
            delay(2800)
            vm.dismissAudioMessage()
        }
        NoticeBar(message)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoveraTopBar(onRefresh: () -> Unit, onImport: () -> Unit, onSettings: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = Midnight, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Novera Audio", fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                    Text("LOCAL / UNBOUND", style = MaterialTheme.typography.labelSmall, color = Cyan, letterSpacing = 1.4.sp)
                }
            }
        },
        actions = {
            IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "Actualizar biblioteca", tint = SoftText) }
            IconButton(onClick = onImport) { Icon(Icons.Default.FolderOpen, "Importar música", tint = SoftText) }
            IconButton(onClick = onSettings) { Icon(Icons.Default.MoreHoriz, "Más opciones", tint = SoftText) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Midnight, titleContentColor = Color.White)
    )
}

@Composable
private fun MiniPlayerBar(
    track: Track?,
    state: PlayerState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit
) {
    val activeTrack = track ?: return
    val progress = if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(250)).clickable(onClick = onOpen),
        color = Color(0xFF121E32),
        tonalElevation = 0.dp
    ) {
        Column {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(2.dp), color = Cyan, trackColor = Color.Transparent)
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AudioFile, null, tint = Midnight, modifier = Modifier.size(23.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(activeTrack.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(activeTrack.artist, color = SoftText, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onPlayPause) { Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Reproducir o pausar", tint = Cyan) }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Siguiente", tint = SoftText) }
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    padding: PaddingValues,
    tracks: List<Track>,
    state: PlayerState,
    visualizerBars: List<Float>,
    isFavorite: (Track) -> Boolean,
    onQueryChange: (String) -> Unit,
    onPlay: (Track) -> Unit,
    onFavorite: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onOpenImport: () -> Unit,
    onRefresh: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp),
        state = listState,
        contentPadding = PaddingValues(top = 8.dp, bottom = 170.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "now-playing") {
            NowPlayingCard(
                state = state,
                visualizerBars = visualizerBars,
                onPlayPause = onPlayPause,
                onPrevious = onPrevious,
                onNext = onNext,
                onSeek = onSeek,
                onShuffle = onShuffle,
                onRepeat = onRepeat
            )
        }
        item(key = "search") {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Cyan) },
                trailingIcon = { if (state.query.isNotBlank()) IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, "Limpiar búsqueda") } },
                placeholder = { Text("Buscar por título, artista o álbum", color = MutedText) },
                shape = RoundedCornerShape(18.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Color(0xFF26364D),
                    focusedContainerColor = DeepPanel,
                    unfocusedContainerColor = DeepPanel,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
        item(key = "library-header") {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (state.isScanning) "Escaneando biblioteca…" else "Tu biblioteca", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${tracks.size} ${if (tracks.size == 1) "pista disponible" else "pistas disponibles"}", style = MaterialTheme.typography.bodySmall, color = SoftText)
                }
                OutlinedButton(onClick = onOpenImport, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan)) {
                    Icon(Icons.Default.Usb, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Añadir")
                }
            }
        }
        if (state.isScanning) {
            item(key = "scan-progress") {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Cyan, trackColor = DeepPanel)
            }
        }
        if (tracks.isEmpty() && !state.isScanning) {
            item(key = "empty-library") {
                EmptyLibrary(onImport = onOpenImport, onRefresh = onRefresh)
            }
        } else {
            items(tracks, key = { it.id }) { track ->
                TrackRow(track = track, active = state.currentId == track.id, favorite = isFavorite(track), onPlay = onPlay, onFavorite = onFavorite, onAddToPlaylist = onAddToPlaylist)
            }
        }
    }
}

@Composable
private fun WaveProgressBar(progress: Float, bars: List<Float>, isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "waveFallback")
    val phase by transition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "wavePhase")
    val accent = Cyan
    val values = if (bars.size >= 4) bars else List(42) { index -> (0.18f + ((kotlin.math.sin((index * 0.83f + phase * 4.5f).toDouble()).toFloat() + 1f) * 0.5f) * 0.68f).coerceIn(0.12f, 0.95f) }
    Canvas(modifier = Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = 0.055f))) {
        val gap = size.width / values.size
        values.forEachIndexed { index, value ->
            val breathing = if (isPlaying) 1f else 0.72f
            val height = (size.height * value * breathing).coerceAtLeast(3f)
            val x = index * gap + gap * 0.18f
            val color = if (index.toFloat() / values.size <= progress) accent.copy(alpha = 0.7f + value * 0.3f) else Color.White.copy(alpha = 0.14f)
            drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(x, (size.height - height) / 2), size = androidx.compose.ui.geometry.Size(gap * 0.62f, height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
        }
        drawRoundRect(accent.copy(alpha = 0.8f), topLeft = androidx.compose.ui.geometry.Offset((size.width * progress).coerceIn(0f, size.width - 2f), 0f), size = androidx.compose.ui.geometry.Size(2f, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
    }
}

@Composable
private fun NowPlayingCard(
    state: PlayerState,
    visualizerBars: List<Float>,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = state.tracks.plus(state.importedTracks).firstOrNull { it.id == state.currentId }
    val repeatLabel = when (state.repeatMode) {
        Player.REPEAT_MODE_ONE -> "Repetir pista"
        Player.REPEAT_MODE_ALL -> "Repetir cola"
        else -> "Repetición desactivada"
    }
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(380)).shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.border(1.dp, Brush.linearGradient(listOf(Cyan.copy(alpha = 0.42f), Violet.copy(alpha = 0.32f))), RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Color(0xFF1A2D49), Color(0xFF221B42)))).padding(18.dp)) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                ) {
                    Box(modifier = Modifier.size(if (expanded) 86.dp else 66.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(Cyan.copy(alpha = 0.9f), Violet.copy(alpha = 0.9f)))), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AudioFile, null, tint = Midnight, modifier = Modifier.size(if (expanded) 38.dp else 30.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AHORA SUENA", style = MaterialTheme.typography.labelSmall, color = Cyan, letterSpacing = 1.7.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(current?.title ?: "Selecciona una pista", style = if (expanded) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(current?.artist ?: "Tu música, sin límites", color = SoftText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(if (expanded) "⌃" else "⌄", color = SoftText, fontSize = 22.sp)
                }
                Spacer(Modifier.height(14.dp))
                val progress = if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f
                WaveProgressBar(progress = progress, bars = visualizerBars, isPlaying = state.isPlaying)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(state.positionMs), style = MaterialTheme.typography.labelSmall, color = SoftText)
                    Text(formatTime(state.durationMs), style = MaterialTheme.typography.labelSmall, color = SoftText)
                }
                AnimatedVisibility(visible = expanded, enter = fadeIn(tween(250)) + scaleIn(tween(250)), exit = fadeOut(tween(180)) + scaleOut(tween(180))) {
                    Column {
                        Slider(
                            value = progress,
                            onValueChange = { onSeek((it * state.durationMs).toLong()) },
                            valueRange = 0f..1f,
                            colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Cyan, activeTrackColor = Cyan, inactiveTrackColor = Color.White.copy(alpha = 0.15f))
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TextButton(onClick = onShuffle) { Icon(Icons.Default.Shuffle, null, tint = if (state.isShuffle) Cyan else SoftText); Spacer(Modifier.width(4.dp)); Text(if (state.isShuffle) "Aleatorio" else "Orden", color = if (state.isShuffle) Cyan else SoftText) }
                            TextButton(onClick = onRepeat) { Icon(Icons.Default.Repeat, repeatLabel, tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) Violet else SoftText); Spacer(Modifier.width(4.dp)); Text(repeatLabel, color = if (state.repeatMode != Player.REPEAT_MODE_OFF) Violet else SoftText) }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, "Anterior", tint = Color.White, modifier = Modifier.size(30.dp)) }
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Cyan, Violet))).clickable(onClick = onPlayPause), contentAlignment = Alignment.Center) {
                        Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Reproducir", tint = Midnight, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Siguiente", tint = Color.White, modifier = Modifier.size(30.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, active: Boolean, favorite: Boolean, onPlay: (Track) -> Unit, onFavorite: (Track) -> Unit, onAddToPlaylist: (Track) -> Unit) {
    val cardColor by animateColorAsState(if (active) Color(0xFF182B42) else DeepPanel, animationSpec = tween(260), label = "trackCardColor")
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(260)).clickable { onPlay(track) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(if (active) Brush.linearGradient(listOf(Cyan, Violet)) else Brush.linearGradient(listOf(RaisedPanel, Color(0xFF24334C)))), contentAlignment = Alignment.Center) {
                Icon(if (active) Icons.Default.PlayArrow else Icons.Default.AudioFile, null, tint = if (active) Midnight else Cyan, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, color = if (active) Cyan else Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${track.artist} · ${track.album}", color = SoftText, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(formatTime(track.durationMs), color = MutedText, style = MaterialTheme.typography.labelSmall)
            IconButton(onClick = { onAddToPlaylist(track) }) { Icon(Icons.Default.PlaylistAdd, "Añadir a playlist", tint = Cyan) }
            IconButton(onClick = { onFavorite(track) }) { Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorito", tint = if (favorite) Violet else SoftText) }
        }
    }
}

@Composable
private fun EmptyLibrary(onImport: () -> Unit, onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 58.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(92.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF172B47), Color(0xFF251E49)))), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.LibraryMusic, null, tint = Cyan, modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("Tu biblioteca está lista", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Escanea el teléfono o importa una carpeta desde una memoria USB.", color = SoftText, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 28.dp))
        Spacer(Modifier.height(20.dp))
        Button(onClick = onImport, colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.Usb, null); Spacer(Modifier.width(8.dp)); Text("Importar música", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onRefresh) { Text("Volver a escanear", color = SoftText) }
    }
}

@Composable
private fun ImportDialog(onDismiss: () -> Unit, onFiles: () -> Unit, onFolder: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepPanel,
        titleContentColor = Color.White,
        textContentColor = SoftText,
        title = { Text("Añadir música", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Elige archivos del teléfono o una carpeta completa. Android también mostrará las memorias USB conectadas.")
                HorizontalDivider(color = Color(0xFF26364D))
                OutlinedButton(onClick = onFiles, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.AudioFile, null); Spacer(Modifier.width(10.dp)); Text("Seleccionar archivos")
                }
                OutlinedButton(onClick = onFolder, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Violet), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.FolderOpen, null); Spacer(Modifier.width(10.dp)); Text("Importar carpeta / USB")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar", color = SoftText) } }
    )
}

@Composable
private fun PlaylistsScreen(
    playlists: List<Playlist>,
    tracks: List<Track>,
    onCreate: () -> Unit,
    onPlayFlow: () -> Unit,
    onDelete: (String) -> Unit,
    onPlay: (Playlist) -> Unit,
    onAddTrack: (Track) -> Unit,
    onRemoveTrack: (String, String) -> Unit
) {
    var openedId by remember { mutableStateOf<String?>(null) }
    val opened = playlists.firstOrNull { it.id == openedId }
    if (opened != null) {
        PlaylistDetailScreen(
            playlist = opened,
            tracks = tracks,
            onBack = { openedId = null },
            onPlay = { onPlay(opened) },
            onDelete = { onDelete(opened.id); openedId = null },
            onRemoveTrack = { trackId -> onRemoveTrack(opened.id, trackId) }
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF172B47)), shape = RoundedCornerShape(22.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Cyan, Violet))), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, "Novera Flow", tint = Midnight) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Novera Flow", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Tu sesión local: favoritos primero, sin internet", color = SoftText, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = onPlayFlow) { Icon(Icons.Default.PlayArrow, "Iniciar Novera Flow", tint = Cyan) }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tus playlists", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Crea la banda sonora de cada momento", color = SoftText)
                }
                OutlinedButton(onClick = onCreate, shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan)) {
                    Icon(Icons.Default.Add, "Crear playlist")
                    Spacer(Modifier.width(6.dp))
                    Text("Nueva")
                }
            }
        }
        if (playlists.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QueueMusic, null, tint = Cyan, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(14.dp))
                        Text("Crea tu primera playlist", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Viaje, carretera, enamorados o el nombre que tú quieras.", color = SoftText, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight), shape = RoundedCornerShape(14.dp)) { Text("Crear playlist") }
                    }
                }
            }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                PlaylistCard(playlist = playlist, tracks = tracks, onOpen = { openedId = playlist.id }, onPlay = { onPlay(playlist) }, onDelete = { onDelete(playlist.id) })
            }
        }
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, tracks: List<Track>, onOpen: () -> Unit, onPlay: () -> Unit, onDelete: () -> Unit) {
    val available = playlist.trackIds.count { id -> tracks.any { it.id == id } }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen), colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(22.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(58.dp).clip(RoundedCornerShape(17.dp)).background(Brush.linearGradient(listOf(Cyan, Violet))), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.QueueMusic, null, tint = Midnight, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$available ${if (available == 1) "canción" else "canciones"}", color = SoftText, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onPlay) { Icon(Icons.Default.PlaylistPlay, "Reproducir playlist", tint = Cyan) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar playlist", tint = Violet) }
        }
    }
}

@Composable
private fun PlaylistDetailScreen(playlist: Playlist, tracks: List<Track>, onBack: () -> Unit, onPlay: () -> Unit, onDelete: () -> Unit, onRemoveTrack: (String) -> Unit) {
    val playlistTracks = playlist.trackIds.mapNotNull { id -> tracks.firstOrNull { it.id == id } }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = SoftText) }
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.name, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${playlistTracks.size} canciones", color = SoftText)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar playlist", tint = Violet) }
        }
        Button(onClick = onPlay, enabled = playlistTracks.isNotEmpty(), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight), shape = RoundedCornerShape(15.dp)) {
            Icon(Icons.Default.PlayArrow, "Reproducir")
            Spacer(Modifier.width(8.dp))
            Text("Reproducir playlist", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        if (playlistTracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Añade canciones desde Biblioteca con el icono de playlist.", color = SoftText, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 150.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(playlistTracks, key = { it.id }) { track ->
                    Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(17.dp)) {
                        Row(modifier = Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(track.artist, color = SoftText, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { onRemoveTrack(track.id) }) { Icon(Icons.Default.RemoveCircleOutline, "Quitar de playlist", tint = MutedText) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepPanel,
        titleContentColor = Color.White,
        textContentColor = SoftText,
        title = { Text("Nueva playlist", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ponle un nombre: Viaje, Enamorados, Carretera, Noche…")
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") })
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = SoftText) } },
        confirmButton = { Button(onClick = { onCreate(name) }, enabled = name.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight)) { Text("Crear") } }
    )
}

@Composable
private fun PlaylistPickerDialog(track: Track, playlists: List<Playlist>, onDismiss: () -> Unit, onChoose: (String) -> Unit, onCreate: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepPanel,
        titleContentColor = Color.White,
        textContentColor = SoftText,
        title = { Text("Añadir a playlist", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(track.title, color = SoftText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (playlists.isEmpty()) Text("Todavía no tienes playlists.", color = MutedText)
                playlists.forEach { playlist ->
                    OutlinedButton(onClick = { onChoose(playlist.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan)) {
                        Icon(Icons.Default.QueueMusic, null)
                        Spacer(Modifier.width(8.dp))
                        Text(playlist.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                TextButton(onClick = onCreate) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Crear nueva playlist", color = Violet) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar", color = SoftText) } }
    )
}

private enum class SettingsPage {
    ROOT, THEMES, EQUALIZER, NOISE_REDUCTION, BLUETOOTH, ENHANCEMENTS, STUDIO, STORAGE, ABOUT
}

@Composable
private fun SettingsScreen(
    selected: NoveraTheme,
    onThemeChange: (NoveraTheme) -> Unit,
    audioState: AudioFxState,
    onBandChange: (Short, Short) -> Unit,
    onPreset: (Short) -> Unit,
    onNoiseReduction: (Boolean) -> Unit,
    onBassBoost: (Boolean) -> Unit,
    onLoudness: (Boolean) -> Unit,
    onSpatial: (Boolean) -> Unit,
    advancedState: AdvancedState,
    currentTrack: Track?,
    positionMs: Long,
    onCreateSmartPlaylist: (String, SmartRuleMode, String) -> Unit,
    onPlaySmartPlaylist: (SmartPlaylist) -> Unit,
    onDeleteSmartPlaylist: (String) -> Unit,
    onAddBookmark: (String, Long) -> Unit,
    onRemoveBookmark: (String, String) -> Unit,
    onJumpBookmark: (Long) -> Unit,
    onSetLoopA: (Long) -> Unit,
    onSetLoopB: (Long) -> Unit,
    onToggleLoop: (Boolean) -> Unit,
    onClearLoop: () -> Unit,
    onImportLyrics: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onSaveDeviceProfile: (String) -> Unit,
    onApplyDeviceProfile: (DeviceProfile) -> Unit,
    voiceEnabled: Boolean,
    onVoiceToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var page by remember { mutableStateOf(SettingsPage.ROOT) }
    val goBack = { if (page == SettingsPage.ROOT) onBack() else page = SettingsPage.ROOT }
    BackHandler {
        goBack()
    }
    when (page) {
        SettingsPage.ROOT -> SettingsHome(
            selectedTheme = selected,
            audioState = audioState,
            onOpen = { page = it },
            onBack = onBack
        )
        SettingsPage.THEMES -> SettingsDetail(title = "Temas", subtitle = "Personaliza el ambiente de Novera Audio", onBack = goBack) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NoveraTheme.values().forEach { option ->
                    ThemeOptionCard(option = option, selected = option == selected, onClick = { onThemeChange(option) })
                }
            }
        }
        SettingsPage.EQUALIZER -> SettingsDetail(title = "Ecualización", subtitle = "Perfiles automáticos y control manual por banda", onBack = goBack) {
            EqualizerOnlyPanel(state = audioState, onBandChange = onBandChange, onPreset = onPreset)
        }
        SettingsPage.NOISE_REDUCTION -> SettingsDetail(title = "Eliminar ruido", subtitle = "Control experimental según la sesión y el dispositivo", onBack = goBack) {
            NoiseReductionPanel(state = audioState, onToggle = onNoiseReduction)
        }
        SettingsPage.BLUETOOTH -> SettingsDetail(title = "Auriculares Bluetooth", subtitle = "Conecta y revisa las salidas disponibles", onBack = goBack) {
            BluetoothAudioPanel(LocalContext.current)
        }
        SettingsPage.ENHANCEMENTS -> SettingsDetail(title = "Mejoras de sonido", subtitle = "Bajos, volumen percibido y espacialidad", onBack = goBack) {
            EnhancementsPanel(state = audioState, onBassBoost = onBassBoost, onLoudness = onLoudness, onSpatial = onSpatial)
        }
        SettingsPage.STUDIO -> SettingsDetail(title = "Novera Studio", subtitle = "Herramientas para escuchar a tu manera", onBack = goBack) {
            NoveraStudioPanel(
                advancedState = advancedState,
                currentTrack = currentTrack,
                positionMs = positionMs,
                onCreateSmartPlaylist = onCreateSmartPlaylist,
                onPlaySmartPlaylist = onPlaySmartPlaylist,
                onDeleteSmartPlaylist = onDeleteSmartPlaylist,
                onAddBookmark = onAddBookmark,
                onRemoveBookmark = onRemoveBookmark,
                onJumpBookmark = onJumpBookmark,
                onSetLoopA = onSetLoopA,
                onSetLoopB = onSetLoopB,
                onToggleLoop = onToggleLoop,
                onClearLoop = onClearLoop,
                onImportLyrics = onImportLyrics,
                onExportBackup = onExportBackup,
                onImportBackup = onImportBackup,
                onSaveDeviceProfile = onSaveDeviceProfile,
                onApplyDeviceProfile = onApplyDeviceProfile,
                voiceEnabled = voiceEnabled,
                onVoiceToggle = onVoiceToggle
            )
        }
        SettingsPage.STORAGE -> SettingsDetail(title = "Biblioteca y USB", subtitle = "Fuentes locales y almacenamiento externo", onBack = goBack) {
            StorageSettingsPanel()
        }
        SettingsPage.ABOUT -> SettingsDetail(title = "Acerca de Novera", subtitle = "Información de la aplicación", onBack = goBack) {
            AboutSettingsPanel()
        }
    }
}

@Composable
private fun SettingsHome(
    selectedTheme: NoveraTheme,
    audioState: AudioFxState,
    onOpen: (SettingsPage) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 26.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Volver", tint = SoftText) }
            Column {
                Text("Ajustes", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Organiza tu experiencia por categorías", color = SoftText, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(24.dp))
        SettingsSectionTitle("Personalización", "Elige cómo quieres ver y escuchar Novera")
        Spacer(Modifier.height(10.dp))
        SettingsOptionRow("Temas", selectedTheme.label, Icons.Default.Palette, selectedTheme.palette.cyan) { onOpen(SettingsPage.THEMES) }
        SettingsOptionRow("Ecualización", if (audioState.equalizerAvailable) "Perfiles y bandas manuales disponibles" else "Esperando una sesión de reproducción", Icons.Default.Tune, Cyan) { onOpen(SettingsPage.EQUALIZER) }
        SettingsOptionRow("Eliminar ruido", if (audioState.noiseReductionAvailable) "Control experimental disponible" else "No disponible en esta sesión", Icons.Default.GraphicEq, Violet) { onOpen(SettingsPage.NOISE_REDUCTION) }
        SettingsOptionRow("Mejoras de sonido", "Bajos, volumen percibido y audio espacial", Icons.Default.AutoAwesome, Color(0xFFFFB27A)) { onOpen(SettingsPage.ENHANCEMENTS) }
        SettingsOptionRow("Novera Studio", "Marcadores, A-B, letras, visualizador y copias", Icons.Default.AutoAwesome, Violet) { onOpen(SettingsPage.STUDIO) }
        Spacer(Modifier.height(22.dp))
        SettingsSectionTitle("Conectividad y biblioteca", "Fuentes de audio y dispositivos")
        Spacer(Modifier.height(10.dp))
        SettingsOptionRow("Auriculares Bluetooth", "Conectar o cambiar la salida de Android", Icons.Default.Bluetooth, Cyan) { onOpen(SettingsPage.BLUETOOTH) }
        SettingsOptionRow("Biblioteca y USB", "Teléfono, OTG y carpetas importadas", Icons.Default.Usb, Violet) { onOpen(SettingsPage.STORAGE) }
        Spacer(Modifier.height(22.dp))
        SettingsSectionTitle("Información", "Privacidad y detalles del proyecto")
        Spacer(Modifier.height(10.dp))
        SettingsOptionRow("Acerca de Novera", "Kotlin 100 % · Java 17 · reproducción local", Icons.Default.Info, SoftText) { onOpen(SettingsPage.ABOUT) }
        Spacer(Modifier.height(24.dp))
        Text("Las funciones de audio muestran su disponibilidad real según el dispositivo y la sesión activa.", color = MutedText, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SettingsDetail(title: String, subtitle: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 26.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = SoftText) }
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, color = SoftText, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(24.dp))
        content()
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun SettingsOptionRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DeepPanel),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent)
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = SoftText, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.ChevronRight, "Abrir", tint = MutedText)
        }
    }
}

@Composable
private fun EqualizerOnlyPanel(state: AudioFxState, onBandChange: (Short, Short) -> Unit, onPreset: (Short) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (state.equalizerAvailable) "Ecualizador activo" else "Ecualizador no disponible todavía", color = if (state.equalizerAvailable) Cyan else SoftText, fontWeight = FontWeight.SemiBold)
            Text("Sesión ${state.sessionId}. Los cambios se aplican a la reproducción actual.", color = MutedText, style = MaterialTheme.typography.bodySmall)
            if (state.presets.isNotEmpty()) {
                Text("Perfiles automáticos", color = Color.White, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.presets.take(8).forEachIndexed { index, name ->
                        FilterChip(selected = state.selectedPreset == index.toShort(), onClick = { onPreset(index.toShort()) }, label = { Text(name) }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            if (state.bands.isNotEmpty()) {
                Text("Ajuste manual", color = Color.White, fontWeight = FontWeight.SemiBold)
                state.bands.forEach { band ->
                    val frequency = if (band.centerHz >= 1000) "${band.centerHz / 1000} kHz" else "${band.centerHz} Hz"
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(frequency, color = SoftText, style = MaterialTheme.typography.labelMedium)
                            Text("${band.levelMb / 100} dB", color = Cyan, style = MaterialTheme.typography.labelMedium)
                        }
                        Slider(value = band.levelMb.toFloat(), onValueChange = { onBandChange(band.index, it.toInt().toShort()) }, valueRange = state.levelMinMb.toFloat()..state.levelMaxMb.toFloat(), colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Cyan, activeTrackColor = Cyan, inactiveTrackColor = Color(0xFF2A405A)))
                    }
                }
            } else {
                Text("Reproduce una pista para crear una sesión de efectos y mostrar las bandas compatibles.", color = SoftText)
            }
        }
    }
}

@Composable
private fun NoiseReductionPanel(state: AudioFxState, onToggle: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Text("Reducción de ruido", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("El efecto se activa solo si el dispositivo ofrece una implementación compatible. En Android está orientado principalmente a captura de voz, por lo que en música se muestra como experimental.", color = SoftText)
            EffectSwitch("Activar reducción experimental", "Puede variar según fabricante y sesión", state.noiseReductionEnabled, state.noiseReductionAvailable, onToggle)
        }
    }
}

@Composable
private fun EnhancementsPanel(state: AudioFxState, onBassBoost: (Boolean) -> Unit, onLoudness: (Boolean) -> Unit, onSpatial: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EffectSwitch("Realce de bajos", "Refuerza las frecuencias graves", state.bassBoostEnabled, state.equalizerAvailable, onBassBoost)
            EffectSwitch("Volumen percibido", "Ajusta el loudness de la sesión", state.loudnessEnabled, state.equalizerAvailable, onLoudness)
            EffectSwitch("Audio espacial", "Virtualizador cuando el hardware lo permite", state.spatialEnabled, state.equalizerAvailable, onSpatial)
        }
    }
}

@Composable
private fun StorageSettingsPanel() {
    Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Usb, null, tint = Cyan); Spacer(Modifier.width(12.dp)); Text("Teléfono y USB OTG", color = Color.White, fontWeight = FontWeight.SemiBold) }
            Text("Añade archivos o carpetas desde el almacenamiento del teléfono o una memoria USB conectada. La biblioteca se conserva localmente.", color = SoftText)
        }
    }
}

@Composable
private fun AboutSettingsPanel() {
    Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Novera Audio", color = Cyan, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Reproductor local privado, sin cuenta ni servidor.", color = SoftText)
            HorizontalDivider(color = Color(0xFF26364D))
            Text("Kotlin 100 % · Jetpack Compose · Media3 · Java 17", color = SoftText)
            Text("Algunos efectos dependen del fabricante, del dispositivo y de la sesión activa.", color = MutedText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NoveraStudioPanel(
    advancedState: AdvancedState,
    currentTrack: Track?,
    positionMs: Long,
    onCreateSmartPlaylist: (String, SmartRuleMode, String) -> Unit,
    onPlaySmartPlaylist: (SmartPlaylist) -> Unit,
    onDeleteSmartPlaylist: (String) -> Unit,
    onAddBookmark: (String, Long) -> Unit,
    onRemoveBookmark: (String, String) -> Unit,
    onJumpBookmark: (Long) -> Unit,
    onSetLoopA: (Long) -> Unit,
    onSetLoopB: (Long) -> Unit,
    onToggleLoop: (Boolean) -> Unit,
    onClearLoop: () -> Unit,
    onImportLyrics: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onSaveDeviceProfile: (String) -> Unit,
    onApplyDeviceProfile: (DeviceProfile) -> Unit,
    voiceEnabled: Boolean,
    onVoiceToggle: (Boolean) -> Unit
) {
    var smartDialog by remember { mutableStateOf(false) }
    var bookmarkDialog by remember { mutableStateOf(false) }
    var profileDialog by remember { mutableStateOf(false) }
    val trackBookmarks = currentTrack?.let { advancedState.bookmarks[it.id].orEmpty() }.orEmpty()
    val lyrics = currentTrack?.let { advancedState.lrcByTrack[it.id].orEmpty() }.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        StudioSectionCard("Playlists inteligentes", "Reglas locales que generan colas automáticamente") {
            advancedState.smartPlaylists.forEach { smart ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(smart.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(smart.mode.name.lowercase().replace('_', ' '), color = SoftText, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onPlaySmartPlaylist(smart) }) { Icon(Icons.Default.PlayArrow, "Reproducir", tint = Cyan) }
                    IconButton(onClick = { onDeleteSmartPlaylist(smart.id) }) { Icon(Icons.Default.Delete, "Eliminar", tint = Violet) }
                }
            }
            OutlinedButton(onClick = { smartDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Add, "Crear regla"); Spacer(Modifier.width(7.dp)); Text("Crear playlist inteligente")
            }
        }
        StudioSectionCard("Marcadores y modo A-B", "Guarda momentos y repite fragmentos de una canción") {
            if (currentTrack == null) {
                Text("Reproduce una pista para crear marcadores y configurar un bucle.", color = SoftText)
            } else {
                Text(currentTrack.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { bookmarkDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight), shape = RoundedCornerShape(13.dp)) { Text("Añadir marcador") }
                    OutlinedButton(onClick = { onSetLoopA(positionMs) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan), shape = RoundedCornerShape(13.dp)) { Text("A = ${formatTime(positionMs)}") }
                    OutlinedButton(onClick = { onSetLoopB(positionMs) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Violet), shape = RoundedCornerShape(13.dp)) { Text("B = ${formatTime(positionMs)}") }
                }
                Text(if (advancedState.loopA != null && advancedState.loopB != null) "A: ${formatTime(advancedState.loopA)} · B: ${formatTime(advancedState.loopB)}" else "Define A y B para activar el bucle", color = SoftText, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Bucle A-B", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(checked = advancedState.loopEnabled && advancedState.loopA != null && advancedState.loopB != null, onCheckedChange = onToggleLoop, enabled = advancedState.loopA != null && advancedState.loopB != null)
                    TextButton(onClick = onClearLoop) { Text("Limpiar", color = MutedText) }
                }
                trackBookmarks.forEach { mark ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onJumpBookmark(mark.positionMs) }) {
                        Icon(Icons.Default.Bookmark, "Marcador", tint = Cyan)
                        Spacer(Modifier.width(8.dp))
                        Text(mark.title, color = Color.White, modifier = Modifier.weight(1f))
                        Text(formatTime(mark.positionMs), color = SoftText)
                        IconButton(onClick = { onRemoveBookmark(currentTrack.id, mark.id) }) { Icon(Icons.Default.Delete, "Eliminar marcador", tint = MutedText) }
                    }
                }
            }
        }
        StudioSectionCard("Letras y visualizador", "Archivos LRC locales y animación reactiva") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onImportLyrics, enabled = currentTrack != null, colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight), shape = RoundedCornerShape(13.dp)) { Text("Importar .lrc") }
                Text(if (lyrics.isEmpty()) "Sin letras para esta pista" else "${lyrics.size} líneas cargadas", color = SoftText, modifier = Modifier.align(Alignment.CenterVertically))
            }
            if (lyrics.isNotEmpty()) LyricsPanel(lyrics, positionMs)
            VisualizerPanel()
        }
        StudioSectionCard("Perfiles por dispositivo", "Guarda una configuración de audio para reaplicarla cuando quieras") {
            advancedState.deviceProfiles.forEach { profile ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(profile.name, color = Color.White, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onApplyDeviceProfile(profile) }) { Text("Aplicar", color = Cyan) }
                }
            }
            OutlinedButton(onClick = { profileDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Violet), shape = RoundedCornerShape(14.dp)) { Text("Guardar configuración actual") }
        }
        StudioSectionCard("Asistente de voz local", "Controla Novera con la pantalla bloqueada mediante una palabra de activación") {
            Text("Actívalo solo cuando quieras usar comandos como: Novera, pausa; Novera, reproduce mi canción; Novera, siguiente; Novera, escanea mi música; Novera, añade esto a Carretera.", color = SoftText, style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (voiceEnabled) "Escucha local activa" else "Escucha local desactivada", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(if (voiceEnabled) "Puedes apagar la pantalla; verás una notificación persistente" else "Requiere permiso de micrófono; reconocimiento offline incluido", color = MutedText, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = voiceEnabled, onCheckedChange = onVoiceToggle)
            }
        }
        StudioSectionCard("Copia de seguridad local", "Guarda playlists, reglas, marcadores, letras, perfiles, favoritos y tema") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExportBackup, colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight), shape = RoundedCornerShape(13.dp)) { Text("Exportar") }
                OutlinedButton(onClick = onImportBackup, colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan), shape = RoundedCornerShape(13.dp)) { Text("Restaurar") }
            }
        }
    }
    if (smartDialog) SmartPlaylistDialog(onDismiss = { smartDialog = false }, onCreate = { name, mode, artist -> smartDialog = false; onCreateSmartPlaylist(name, mode, artist) })
    if (bookmarkDialog) BookmarkDialog(onDismiss = { bookmarkDialog = false }, onCreate = { title -> bookmarkDialog = false; onAddBookmark(title, positionMs) })
    if (profileDialog) DeviceProfileDialog(onDismiss = { profileDialog = false }, onSave = { name -> profileDialog = false; onSaveDeviceProfile(name) })
}

@Composable
private fun StudioSectionCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = SoftText, style = MaterialTheme.typography.bodySmall)
            content()
        })
    }
}

@Composable
private fun LyricsPanel(lines: List<LrcLine>, positionMs: Long) {
    val activeIndex = lines.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
        lines.takeLast(5).forEachIndexed { index, line ->
            val actualIndex = lines.indexOf(line)
            Text(line.text.ifBlank { "…" }, color = if (actualIndex == activeIndex) Cyan else SoftText, fontWeight = if (actualIndex == activeIndex) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
        }
    }
}

@Composable
private fun VisualizerPanel() {
    val transition = rememberInfiniteTransition(label = "noveraVisualizer")
    val phase by transition.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "visualizerPhase")
    val accent = Cyan
    Canvas(modifier = Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFF0A1220))) {
        val bars = 28
        val gap = size.width / (bars * 1.35f)
        repeat(bars) { index ->
            val wave = (kotlin.math.sin(index * 0.72 + phase * 4.0) * 0.5 + 0.5).toFloat()
            val height = size.height * (0.25f + wave * 0.65f)
            drawRoundRect(accent.copy(alpha = 0.35f + wave * 0.55f), topLeft = androidx.compose.ui.geometry.Offset(index * gap, (size.height - height) / 2), size = androidx.compose.ui.geometry.Size(gap * 0.62f, height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))
        }
    }
}

@Composable
private fun SmartPlaylistDialog(onDismiss: () -> Unit, onCreate: (String, SmartRuleMode, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(SmartRuleMode.FAVORITES) }
    var artist by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, containerColor = DeepPanel, title = { Text("Playlist inteligente", color = Color.White) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true)
            SmartRuleMode.values().forEach { option -> FilterChip(selected = option == mode, onClick = { mode = option }, label = { Text(option.name.lowercase().replace('_', ' ')) }, modifier = Modifier.fillMaxWidth()) }
            if (mode == SmartRuleMode.ARTIST) OutlinedTextField(artist, { artist = it }, label = { Text("Artista contiene") }, singleLine = true)
        }
    }, confirmButton = { Button(onClick = { onCreate(name, mode, artist) }, enabled = name.isNotBlank() && (mode != SmartRuleMode.ARTIST || artist.isNotBlank()), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight)) { Text("Crear") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = SoftText) } })
}

@Composable
private fun BookmarkDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, containerColor = DeepPanel, title = { Text("Nuevo marcador", color = Color.White) }, text = { OutlinedTextField(title, { title = it }, label = { Text("Nombre del momento") }, singleLine = true) }, confirmButton = { Button(onClick = { onCreate(title) }, enabled = title.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight)) { Text("Guardar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = SoftText) } })
}

@Composable
private fun DeviceProfileDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, containerColor = DeepPanel, title = { Text("Guardar perfil", color = Color.White) }, text = { OutlinedTextField(name, { name = it }, label = { Text("Nombre del dispositivo") }, singleLine = true) }, confirmButton = { Button(onClick = { onSave(name) }, enabled = name.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Midnight)) { Text("Guardar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = SoftText) } })
}

@Composable
private fun SettingsSectionTitle(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
    Text(subtitle, color = SoftText, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun EffectSwitch(title: String, description: String, checked: Boolean, available: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (available) Color.White else MutedText, fontWeight = FontWeight.SemiBold)
            Text(if (available) description else "No disponible en esta sesión", color = MutedText, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = available)
    }
}

@Composable
private fun BluetoothAudioPanel(context: Context) {
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val outputs = remember(audioManager) {
        runCatching {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .filter { device ->
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                        device.type == AudioDeviceInfo.TYPE_BLE_BROADCAST
                }
                .map { it.productName?.toString()?.ifBlank { "Auricular Bluetooth" } ?: "Auricular Bluetooth" }
        }.getOrDefault(emptyList())
    }
    Card(colors = CardDefaults.cardColors(containerColor = DeepPanel), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (outputs.isEmpty()) "No se detectó una salida Bluetooth activa" else "Salidas detectadas", color = Color.White, fontWeight = FontWeight.SemiBold)
            if (outputs.isNotEmpty()) outputs.distinct().forEach { Text("• $it", color = Cyan) }
            Text("La conexión y el cambio de salida se gestionan con el panel de audio de Android para respetar el control del sistema y de los auriculares.", color = SoftText, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan), shape = RoundedCornerShape(14.dp)) {
                Text("Conectar auriculares Bluetooth")
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(option: NoveraTheme, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) option.palette.cyan else Color(0xFF26364D)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = option.palette.deepPanel),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Brush.linearGradient(listOf(option.palette.cyan, option.palette.violet))))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(option.label, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(option.description, color = SoftText, style = MaterialTheme.typography.bodySmall)
            }
            if (selected) Icon(Icons.Default.Favorite, "Tema seleccionado", tint = option.palette.cyan)
        }
    }
}

@Composable
private fun NoticeBar(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(bottom = 76.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(color = RaisedPanel, shape = RoundedCornerShape(16.dp), shadowElevation = 8.dp, modifier = Modifier.padding(16.dp)) {
            Text(text, color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
        }
    }
}

@Composable
private fun noveraColors() = androidx.compose.material3.darkColorScheme(
    primary = Cyan,
    onPrimary = Midnight,
    secondary = Violet,
    onSecondary = Color.White,
    background = Midnight,
    surface = DeepPanel,
    onSurface = Color.White,
    onSurfaceVariant = SoftText
)

private fun formatTime(ms: Long): String {
    val totalSeconds = max(0L, ms) / 1000
    return String.format(Locale.getDefault(), "%d:%02d", totalSeconds / 60, totalSeconds % 60)
}
