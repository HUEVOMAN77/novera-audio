package com.novera.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

/**
 * Servicio foreground de reconocimiento de voz completamente local.
 * Vosk se ejecuta con el modelo español incluido en assets, por lo que no
 * depende de Google Speech Services ni de una conexión a Internet.
 */
class VoiceAssistantService : Service() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val modelExecutor = Executors.newSingleThreadExecutor()
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var mediaController: MediaController? = null
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var listening = false
    private var loadingModel = false
    private var transcriptWindow = ""
    private var lastHandledAt = 0L
    private var lastTranscriptAt = 0L
    private var foregroundStarted = false

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply()
            stopListening()
            if (foregroundStarted) stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply()
            stopSelf()
            return START_NOT_STICKY
        }
        if (!promoteToForeground()) return START_NOT_STICKY

        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, true).apply()
        ensureMediaController()
        return START_NOT_STICKY
    }

    private fun ensureMediaController() {
        if (mediaController != null) {
            ensureModelAndListen()
            return
        }
        if (mediaControllerFuture != null) return
        runCatching {
            MediaController.Builder(
                this,
                SessionToken(this, ComponentName(this, PlaybackService::class.java))
            ).buildAsync().also { future ->
                mediaControllerFuture = future
                future.addListener({
                    runCatching {
                        mediaController = future.get()
                        ensureModelAndListen()
                    }.onFailure { error ->
                        android.util.Log.e(TAG, "No se pudo conectar con MediaSession", error)
                        updateNotification("Reproductor no disponible; voz detenida")
                        stopListening()
                    }
                }, MoreExecutors.directExecutor())
            }
        }.onFailure { error ->
            android.util.Log.e(TAG, "No se pudo solicitar MediaSession", error)
            updateNotification("No se pudo conectar con el reproductor")
        }
    }

    private fun promoteToForeground(): Boolean {
        if (foregroundStarted) return true
        return runCatching {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                0
            }
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification("Preparando reconocimiento offline…"),
                type
            )
            foregroundStarted = true
            true
        }.getOrElse { error ->
            android.util.Log.e(TAG, "No se pudo iniciar el foreground service de micrófono", error)
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply()
            stopSelf()
            false
        }
    }

    private fun ensureModelAndListen() {
        if (model != null) {
            startListening()
            return
        }
        if (loadingModel) return

        loadingModel = true
        updateNotification("Cargando reconocimiento español offline…")
        modelExecutor.execute {
            runCatching {
                val modelDirectory = File(filesDir, MODEL_DIRECTORY)
                if (!File(modelDirectory, "am/final.mdl").exists()) {
                    modelDirectory.deleteRecursively()
                    copyAssetTree(MODEL_ASSET_PATH, modelDirectory)
                }
                Model(modelDirectory.absolutePath)
            }.onSuccess { loadedModel ->
                mainHandler.post {
                    loadingModel = false
                    model = loadedModel
                    if (isEnabled(this)) startListening()
                }
            }.onFailure { error ->
                mainHandler.post {
                    loadingModel = false
                    updateNotification("No se pudo cargar el reconocimiento offline")
                    android.util.Log.e(TAG, "No se pudo cargar el modelo Vosk", error)
                }
            }
        }
    }

    private fun copyAssetTree(assetPath: String, destination: File) {
        val children = assets.list(assetPath).orEmpty()
        if (children.isEmpty()) {
            destination.parentFile?.mkdirs()
            assets.open(assetPath).use { input ->
                FileOutputStream(destination).use { output -> input.copyTo(output) }
            }
            return
        }

        destination.mkdirs()
        children.forEach { child ->
            copyAssetTree("$assetPath/$child", File(destination, child))
        }
    }

    private fun startListening() {
        if (listening || model == null || !isEnabled(this)) return
        runCatching {
            val recognizer = Recognizer(model, SAMPLE_RATE)
            recognizer.setMaxAlternatives(1)
            SpeechService(recognizer, SAMPLE_RATE).also { service ->
                speechService = service
                listening = true
                transcriptWindow = ""
                updateNotification("Escuchando offline · di: Novera y tu comando")
                service.startListening(object : RecognitionListener {
                    override fun onPartialResult(hypothesis: String) {
                        val text = extractText(hypothesis)
                        if (text.isNotBlank()) updateNotification("Escuchando: $text")
                    }

                    override fun onResult(hypothesis: String) {
                        processHypothesis(hypothesis)
                    }

                    override fun onFinalResult(hypothesis: String) {
                        processHypothesis(hypothesis)
                        listening = false
                        scheduleRestart()
                    }

                    override fun onError(exception: Exception) {
                        android.util.Log.e(TAG, "Error de reconocimiento Vosk", exception)
                        listening = false
                        updateNotification("Reconocimiento offline activo; reintentando…")
                        scheduleRestart()
                    }

                    override fun onTimeout() {
                        listening = false
                        scheduleRestart()
                    }
                }
                )
            }
        }.onFailure { error ->
            listening = false
            updateNotification("No se pudo iniciar el micrófono offline")
            android.util.Log.e(TAG, "No se pudo iniciar Vosk", error)
            scheduleRestart()
        }
    }

    private fun processHypothesis(hypothesis: String) {
        val text = extractText(hypothesis)
        if (text.isBlank()) return

        val now = System.currentTimeMillis()
        if (now - lastTranscriptAt > WAKE_WINDOW_MS) transcriptWindow = ""
        lastTranscriptAt = now
        transcriptWindow = "$transcriptWindow $text".trim().takeLast(MAX_TRANSCRIPT_LENGTH)
        if (now - lastHandledAt < COMMAND_COOLDOWN_MS) return

        val response = if (mediaController == null) {
            if (VoiceAssistant.isWakeWordOnly(transcriptWindow)) "Te escucho · di tu comando" else return
        } else {
            VoiceAssistant.execute(this, transcriptWindow, mediaController!!) ?: return
        }
        lastHandledAt = now
        if (VoiceAssistant.isWakeWordOnly(transcriptWindow)) {
            updateNotification("Te escucho · di tu comando")
        } else {
            transcriptWindow = ""
            updateNotification(response)
            runCatching { speechService?.reset() }
        }
    }

    private fun extractText(hypothesis: String): String {
        return runCatching {
            val json = JSONObject(hypothesis)
            json.optString("text").ifBlank { json.optString("partial") }
        }.getOrDefault("").trim()
    }

    private fun scheduleRestart() {
        if (!isEnabled(this)) return
        mainHandler.postDelayed({
            if (isEnabled(this)) {
                runCatching {
                    speechService?.cancel()
                    speechService?.shutdown()
                    speechService = null
                }
                startListening()
            }
        }, RESTART_DELAY_MS)
    }

    private fun stopListening() {
        listening = false
        mainHandler.removeCallbacksAndMessages(null)
        runCatching { speechService?.cancel() }
        runCatching { speechService?.shutdown() }
        speechService = null
        transcriptWindow = ""
        lastTranscriptAt = 0L
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Asistente de voz",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Estado del asistente local de Novera Audio" }
            )
        }
    }

    private fun notification(message: String): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            8,
            Intent(this, VoiceAssistantService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = PendingIntent.getActivity(
            this,
            9,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.novera_audio_icon)
            .setContentTitle("Novera Audio · voz offline")
            .setContentText(message)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_media_pause, "Desactivar", stopIntent)
            .build()
    }

    private fun updateNotification(message: String) {
        runCatching {
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(message))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopListening()
        modelExecutor.shutdownNow()
        runCatching { model?.close() }
        model = null
        runCatching { mediaController?.release() }
        mediaController = null
        mediaControllerFuture?.cancel(true)
        mediaControllerFuture = null
        foregroundStarted = false
        super.onDestroy()
    }

    companion object {
        private const val TAG = "NoveraVoice"
        private const val CHANNEL_ID = "novera_voice"
        private const val NOTIFICATION_ID = 1202
        private const val ACTION_STOP = "com.novera.audio.voice.STOP"
        private const val PREFS = "novera_voice"
        private const val KEY_ENABLED = "enabled"
        private const val MODEL_ASSET_PATH = "model-es"
        private const val MODEL_DIRECTORY = "vosk-model-es"
        private const val SAMPLE_RATE = 16000.0f
        private const val RESTART_DELAY_MS = 700L
        private const val COMMAND_COOLDOWN_MS = 1800L
        private const val MAX_TRANSCRIPT_LENGTH = 180
        private const val WAKE_WINDOW_MS = 4500L

        fun isEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, false)

        fun start(context: Context) {
            runCatching {
                val intent = Intent(context, VoiceAssistantService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            }.onFailure { error ->
                android.util.Log.e(TAG, "No se pudo solicitar el servicio de voz", error)
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, false).apply()
            }
        }

        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, VoiceAssistantService::class.java))
            }.onFailure { error ->
                android.util.Log.e(TAG, "No se pudo detener el servicio de voz", error)
            }
        }
    }
}
