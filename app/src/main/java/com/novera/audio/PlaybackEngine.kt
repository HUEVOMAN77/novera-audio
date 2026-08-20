package com.novera.audio

import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

object PlaybackEngine {
    private var instance: ExoPlayer? = null

    fun player(context: Context): ExoPlayer {
        return instance ?: ExoPlayer.Builder(context.applicationContext).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            setHandleAudioBecomingNoisy(true)
            repeatMode = Player.REPEAT_MODE_OFF
        }.also { instance = it }
    }

    fun startService(context: Context) {
        val intent = Intent(context, PlaybackService::class.java)
        androidx.core.content.ContextCompat.startForegroundService(context, intent)
    }
}
