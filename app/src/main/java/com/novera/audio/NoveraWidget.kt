package com.novera.audio

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.media3.common.Player

object WidgetActions {
    const val PLAY_PAUSE = "com.novera.audio.widget.PLAY_PAUSE"
    const val PREVIOUS = "com.novera.audio.widget.PREVIOUS"
    const val NEXT = "com.novera.audio.widget.NEXT"
    const val REFRESH = "com.novera.audio.widget.REFRESH"
}

private const val PREFS = "novera_widget"
private const val TITLE = "title"
private const val ARTIST = "artist"
private const val IS_PLAYING = "is_playing"

object WidgetStateStore {
    fun save(context: Context, title: String, artist: String, isPlaying: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TITLE, title)
            .putString(ARTIST, artist)
            .putBoolean(IS_PLAYING, isPlaying)
            .apply()
        NoveraWidget.refresh(context)
    }

    fun title(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(TITLE, "Novera Audio") ?: "Novera Audio"
    fun artist(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ARTIST, "Tu música, sin límites") ?: "Tu música, sin límites"
    fun isPlaying(context: Context): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(IS_PLAYING, false)
}

class NoveraWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateOne(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action !in setOf(WidgetActions.PLAY_PAUSE, WidgetActions.PREVIOUS, WidgetActions.NEXT)) return
        val player = PlaybackEngine.player(context)
        PlaybackEngine.startService(context)
        when (intent.action) {
            WidgetActions.PLAY_PAUSE -> if (player.isPlaying) player.pause() else player.play()
            WidgetActions.PREVIOUS -> if (player.currentPosition > 3_000L) player.seekTo(0L) else player.seekToPreviousMediaItem()
            WidgetActions.NEXT -> player.seekToNextMediaItem()
        }
        val title = player.currentMediaItem?.mediaMetadata?.title?.toString() ?: WidgetStateStore.title(context)
        val artist = player.currentMediaItem?.mediaMetadata?.artist?.toString() ?: WidgetStateStore.artist(context)
        WidgetStateStore.save(context, title, artist, player.isPlaying)
    }

    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, NoveraWidget::class.java)
            manager.getAppWidgetIds(component).forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_novera)
            views.setTextViewText(R.id.widget_title, WidgetStateStore.title(context))
            views.setTextViewText(R.id.widget_artist, WidgetStateStore.artist(context))
            views.setTextViewText(R.id.widget_status, if (WidgetStateStore.isPlaying(context)) "REPRODUCIENDO" else "EN PAUSA")
            views.setImageViewResource(R.id.widget_play, if (WidgetStateStore.isPlaying(context)) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
            views.setOnClickPendingIntent(R.id.widget_play, action(context, WidgetActions.PLAY_PAUSE, 101))
            views.setOnClickPendingIntent(R.id.widget_previous, action(context, WidgetActions.PREVIOUS, 102))
            views.setOnClickPendingIntent(R.id.widget_next, action(context, WidgetActions.NEXT, 103))
            val open = Intent(context, MainActivity::class.java)
            views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 104, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            manager.updateAppWidget(id, views)
        }

        private fun action(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, NoveraWidget::class.java).setAction(action)
            return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }
}
