package com.example.voxtask.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.voxtask.MainActivity
import com.example.voxtask.R
import kotlinx.coroutines.*

class ContadorService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var contadorJob: Job? = null

    companion object {
        const val CHANNEL_ID              = "contador_channel"
        const val CHANNEL_ID_FINALIZADO   = "contador_channel_finalizado"
        const val NOTIF_ID                = 1

        const val EXTRA_SEGUNDOS  = "segundos"

        const val ACCION_INICIAR  = "INICIAR"
        const val ACCION_PARAR    = "PARAR"
        const val ACCION_REANUDAR = "REANUDAR"
        const val ACCION_CANCELAR = "CANCELAR"

        var estaActivo: Boolean    = false
        var estaPausado: Boolean   = false
        var segundosRestantes: Int = 0
    }

    // ─────────────────────────────────────────────────────────────────────────
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanales()

        when (intent?.action) {

            ACCION_INICIAR -> {
                val totalSegundos = intent.getIntExtra(EXTRA_SEGUNDOS, 0)
                estaPausado = false

                startForeground(
                    NOTIF_ID,
                    crearNotificacion(
                        titulo    = getString(R.string.txt_titulo_contador_iniciado),
                        contenido = formatearTiempo(totalSegundos),
                        pausado   = false
                    ).build()
                )
                iniciarContador(totalSegundos)
            }

            ACCION_PARAR -> {
                contadorJob?.cancel()
                estaActivo  = false
                estaPausado = true
                actualizarNotificacion(
                    titulo    = getString(R.string.txt_titulo_contador_pausado),
                    contenido = formatearTiempo(segundosRestantes),
                    pausado   = true
                )
            }

            ACCION_REANUDAR -> {
                estaActivo  = true
                estaPausado = false
                startForeground(
                    NOTIF_ID,
                    crearNotificacion(
                        titulo    = getString(R.string.txt_titulo_contador_iniciado),
                        contenido = formatearTiempo(segundosRestantes),
                        pausado   = false
                    ).build()
                )
                iniciarContador(segundosRestantes)
            }

            ACCION_CANCELAR -> {
                contadorJob?.cancel()
                estaActivo        = false
                estaPausado       = false
                segundosRestantes = 0
                getSystemService(NotificationManager::class.java).cancel(NOTIF_ID)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cuenta atrás del servicio
    // ─────────────────────────────────────────────────────────────────────────
    private fun iniciarContador(totalSegundos: Int) {
        estaActivo = true
        contadorJob?.cancel()

        contadorJob = scope.launch {
            var restantes = totalSegundos

            while (restantes >= 0) {
                segundosRestantes = restantes
                val finalizado = restantes == 0

                if (finalizado) {
                    // ── Actualizamos la MISMA notificación en lugar de crear una nueva
                    actualizarNotificacion(
                        titulo     = getString(R.string.txt_titulo_contador_terminado),
                        contenido  = getString(R.string.txt_titulo_contador_finalizado),
                        pausado    = false,
                        finalizado = true
                    )
                    break
                }

                actualizarNotificacion(
                    titulo     = getString(R.string.txt_titulo_contador_iniciado),
                    contenido  = formatearTiempo(restantes),
                    pausado    = false,
                    finalizado = false
                )

                delay(1000L)
                restantes--
            }

            estaActivo        = false
            segundosRestantes = 0
        }
    }
    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun formatearTiempo(segundos: Int): String {
        val h = segundos / 3600
        val m = (segundos % 3600) / 60
        val s = segundos % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    private fun crearPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "ABRIR_CONTADOR"
            flags  = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun pendingIntentAccion(action: String): PendingIntent {
        val intent = Intent(this, ContadorService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Canales de notificación
    // ─────────────────────────────────────────────────────────────────────────
    private fun crearCanales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            // Canal silencioso → usado durante la cuenta atrás
            val canalSilencioso = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.txt_name_canal),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.txt_descripcion)
                setSound(null, null)
                enableVibration(false)
            }

            // Canal con sonido + vibración → usado solo al llegar a 0
            val canalFinalizado = NotificationChannel(
                CHANNEL_ID_FINALIZADO,
                "${getString(R.string.txt_name_canal)} – fin",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.txt_descripcion)
                setSound(
                    Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }

            nm.createNotificationChannel(canalSilencioso)
            nm.createNotificationChannel(canalFinalizado)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Notificaciones
    // ─────────────────────────────────────────────────────────────────────────
    private fun crearNotificacion(
        titulo: String,
        contenido: String,
        pausado: Boolean,
        finalizado: Boolean = false
    ): NotificationCompat.Builder {

        val canalId = if (finalizado) CHANNEL_ID_FINALIZADO else CHANNEL_ID

        val builder = NotificationCompat.Builder(this, canalId)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(!finalizado)
            .setSilent(!finalizado)
            .setContentIntent(crearPendingIntent())

        // Sin botones cuando ha terminado
        if (!finalizado) {
            if (pausado) {
                builder.addAction(
                    android.R.drawable.ic_media_play,
                    getString(R.string.txt_title_reanudar),
                    pendingIntentAccion(ACCION_REANUDAR)
                )
            } else {
                builder.addAction(
                    android.R.drawable.ic_media_pause,
                    getString(R.string.txt_title_pausar),
                    pendingIntentAccion(ACCION_PARAR)
                )
            }
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.txt_title_cancelar),
                pendingIntentAccion(ACCION_CANCELAR)
            )
        }

        return builder
    }
    private fun actualizarNotificacion(
        titulo: String,
        contenido: String,
        pausado: Boolean,
        finalizado: Boolean = false
    ) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, crearNotificacion(titulo, contenido, pausado, finalizado).build())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}