package com.novera.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.NoiseSuppressor
import android.media.audiofx.Virtualizer
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

 data class AudioBand(
    val index: Short,
    val centerHz: Int,
    val levelMb: Short
)

data class AudioFxState(
    val sessionId: Int = 0,
    val equalizerAvailable: Boolean = false,
    val bands: List<AudioBand> = emptyList(),
    val levelMinMb: Short = (-1500).toShort(),
    val levelMaxMb: Short = 1500,
    val presets: List<String> = emptyList(),
    val selectedPreset: Short? = null,
    val noiseReductionAvailable: Boolean = false,
    val noiseReductionEnabled: Boolean = false,
    val bassBoostEnabled: Boolean = false,
    val loudnessEnabled: Boolean = false,
    val spatialEnabled: Boolean = false,
    val message: String? = null
)

class AudioEffectsController(
    private val context: Context,
    private val player: ExoPlayer
) {
    private val prefs = context.getSharedPreferences("novera_audio_effects", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(AudioFxState())
    val state: StateFlow<AudioFxState> = _state.asStateFlow()

    private var equalizer: Equalizer? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var virtualizer: Virtualizer? = null

    init {
        onAudioSessionIdChanged(player.audioSessionId)
    }

    fun onAudioSessionIdChanged(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        releaseEffects()
        _state.value = createEffects(audioSessionId)
    }

    fun setBandLevel(index: Short, levelMb: Short) {
        val effect = equalizer ?: return
        runCatching { effect.setBandLevel(index, levelMb) }
            .onSuccess { persistBand(index, levelMb); refreshBands() }
            .onFailure { publishMessage("Este dispositivo no permite cambiar esa banda") }
    }

    fun applyPreset(index: Short) {
        val effect = equalizer ?: return
        runCatching { effect.usePreset(index) }
            .onSuccess {
                prefs.edit().putInt("preset", index.toInt()).apply()
                refreshBands()
                _state.update { it.copy(selectedPreset = index, message = "Preset: ${it.presets.getOrNull(index.toInt()) ?: "Personalizado"}") }
            }
            .onFailure { publishMessage("Preset no disponible en este dispositivo") }
    }

    fun toggleNoiseReduction(enabled: Boolean) {
        val effect = noiseSuppressor
        if (effect == null) {
            publishMessage("La reducción de ruido no está disponible para esta sesión")
            return
        }
        runCatching { effect.enabled = enabled }
            .onSuccess {
                prefs.edit().putBoolean("noise", enabled).apply()
                _state.update { it.copy(noiseReductionEnabled = enabled, message = if (enabled) "Reducción experimental activada" else "Reducción de ruido desactivada") }
            }
            .onFailure { publishMessage("El hardware no permite activar reducción de ruido") }
    }

    fun toggleBassBoost(enabled: Boolean) {
        val effect = bassBoost ?: return publishMessage("Realce de bajos no disponible")
        runCatching { effect.enabled = enabled }
            .onSuccess { prefs.edit().putBoolean("bass", enabled).apply(); _state.update { it.copy(bassBoostEnabled = enabled) } }
            .onFailure { publishMessage("El dispositivo no permite realzar bajos") }
    }

    fun toggleLoudness(enabled: Boolean) {
        val effect = loudnessEnhancer ?: return publishMessage("Realce de volumen no disponible")
        runCatching { effect.enabled = enabled }
            .onSuccess { prefs.edit().putBoolean("loudness", enabled).apply(); _state.update { it.copy(loudnessEnabled = enabled) } }
            .onFailure { publishMessage("El dispositivo no permite este realce") }
    }

    fun toggleSpatial(enabled: Boolean) {
        val effect = virtualizer ?: return publishMessage("Audio espacial no disponible")
        runCatching { effect.enabled = enabled }
            .onSuccess { prefs.edit().putBoolean("spatial", enabled).apply(); _state.update { it.copy(spatialEnabled = enabled) } }
            .onFailure { publishMessage("El dispositivo no permite audio espacial") }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }

    fun release() = releaseEffects()

    private fun createEffects(audioSessionId: Int): AudioFxState {
        var current = AudioFxState(sessionId = audioSessionId)
        equalizer = runCatching { Equalizer(0, audioSessionId) }.getOrNull()
        equalizer?.let { effect ->
            runCatching {
                effect.enabled = true
                val range = effect.bandLevelRange
                val bands = (0 until effect.numberOfBands).map { position ->
                    val band = position.toShort()
                    AudioBand(band, effect.getCenterFreq(band), prefs.getInt("band_$position", effect.getBandLevel(band).toInt()).toShort())
                }
                bands.forEach { band -> effect.setBandLevel(band.index, band.levelMb) }
                val presets = (0 until effect.numberOfPresets).map { effect.getPresetName(it.toShort()) }
                current = current.copy(
                    equalizerAvailable = true,
                    bands = bands,
                    levelMinMb = range[0],
                    levelMaxMb = range[1],
                    presets = presets,
                    selectedPreset = prefs.getInt("preset", -1).takeIf { it >= 0 }?.toShort()
                )
            }.onFailure { equalizer = null }
        }

        noiseSuppressor = runCatching { NoiseSuppressor.create(audioSessionId) }.getOrNull()
        bassBoost = runCatching { BassBoost(0, audioSessionId) }.getOrNull()
        loudnessEnhancer = runCatching { LoudnessEnhancer(audioSessionId) }.getOrNull()
        virtualizer = runCatching { Virtualizer(0, audioSessionId) }.getOrNull()

        runCatching { noiseSuppressor?.enabled = prefs.getBoolean("noise", false) }
        runCatching { bassBoost?.enabled = prefs.getBoolean("bass", false) }
        runCatching { loudnessEnhancer?.enabled = prefs.getBoolean("loudness", false) }
        runCatching { virtualizer?.enabled = prefs.getBoolean("spatial", false) }
        return current.copy(
            noiseReductionAvailable = noiseSuppressor != null,
            noiseReductionEnabled = noiseSuppressor?.enabled == true,
            bassBoostEnabled = bassBoost?.enabled == true,
            loudnessEnabled = loudnessEnhancer?.enabled == true,
            spatialEnabled = virtualizer?.enabled == true
        )
    }

    private fun refreshBands() {
        val effect = equalizer ?: return
        val bands = (0 until effect.numberOfBands).map { position ->
            val band = position.toShort()
            AudioBand(band, effect.getCenterFreq(band), effect.getBandLevel(band))
        }
        _state.update { it.copy(bands = bands) }
    }

    private fun persistBand(index: Short, level: Short) {
        prefs.edit().putInt("band_${index.toInt()}", level.toInt()).apply()
    }

    private fun publishMessage(message: String) = _state.update { it.copy(message = message) }

    private fun releaseEffects() {
        runCatching { equalizer?.release() }
        runCatching { noiseSuppressor?.release() }
        runCatching { bassBoost?.release() }
        runCatching { loudnessEnhancer?.release() }
        runCatching { virtualizer?.release() }
        equalizer = null
        noiseSuppressor = null
        bassBoost = null
        loudnessEnhancer = null
        virtualizer = null
    }
}
