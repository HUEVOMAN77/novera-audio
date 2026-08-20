package com.novera.audio

import android.media.audiofx.Visualizer
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class AudioVisualizerController {
    private val _bars = MutableStateFlow(emptyList<Float>())
    val bars: StateFlow<List<Float>> = _bars.asStateFlow()
    private var visualizer: Visualizer? = null

    fun attach(audioSessionId: Int) {
        release()
        if (audioSessionId <= 0 || isKnownUnstableManufacturer()) return
        runCatching {
            val effect = Visualizer(audioSessionId)
            val range = Visualizer.getCaptureSizeRange()
            effect.captureSize = range[1]
            effect.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(visualizer: Visualizer, waveform: ByteArray, samplingRate: Int) {
                    runCatching { publishWaveform(waveform) }
                }

                override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {
                    runCatching { publishFft(fft) }
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, true)
            effect.enabled = true
            visualizer = effect
        }
    }

    fun release() {
        runCatching { visualizer?.enabled = false }
        runCatching { visualizer?.release() }
        visualizer = null
        _bars.value = emptyList()
    }

    private fun isKnownUnstableManufacturer(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return manufacturer.contains("huawei") || manufacturer.contains("honor") || brand.contains("huawei") || brand.contains("honor")
    }

    private fun publishWaveform(data: ByteArray) {
        if (data.isEmpty()) return
        val bars = List(36) { index ->
            val start = index * data.size / 36
            val end = ((index + 1) * data.size / 36).coerceAtLeast(start + 1).coerceAtMost(data.size)
            val energy = data.slice(start until end).map { abs(it.toInt() - 128) }.average() / 128f
            energy.coerceIn(0.08, 1.0).toFloat()
        }
        _bars.value = smooth(bars)
    }

    private fun publishFft(data: ByteArray) {
        if (data.size < 4) return
        val bars = List(36) { index ->
            val start = (index * (data.size / 2) / 36).coerceAtLeast(1) * 2
            val end = ((index + 1) * (data.size / 2) / 36).coerceAtLeast(start + 2).coerceAtMost(data.size - 1)
            var energy = 0.0
            var count = 0
            var cursor = start
            while (cursor + 1 < end) {
                val real = data[cursor].toDouble()
                val imaginary = data[cursor + 1].toDouble()
                energy += sqrt(real * real + imaginary * imaginary)
                count++
                cursor += 2
            }
            ((energy / count.coerceAtLeast(1)) / 32.0).coerceIn(0.08, 1.0).toFloat()
        }
        _bars.value = smooth(bars)
    }

    private fun smooth(next: List<Float>): List<Float> {
        val previous = _bars.value
        return next.mapIndexed { index, value -> previous.getOrElse(index) { value } * 0.58f + value * 0.42f }
    }
}
