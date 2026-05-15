package com.example.voxtask.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.voxtask.MainActivity
import com.example.voxtask.R
import kotlinx.coroutines.*

class ContadorService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var contadorJob: Job? = null

    companion object {
        const val CHANNEL_ID = "contador_channel"
        const val NOTIF_ID = 1

        const val EXTRA_SEGUNDOS = "segundos"

        const val ACCION_INICIAR = "INICIAR"
        const val ACCION_PARAR = "PARAR"
        const val ACCION_REANUDAR = "REANUDAR"
        const val ACCION_CANCELAR = "CANCELAR"

        var estaActivo: Boolean = false
        var estaPausado: Boolean = false
        var segundosRestantes: Int = 0
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanal()

        when (intent?.action) {

            ACCION_INICIAR -> {
                val totalSegundos = intent.getIntExtra(EXTRA_SEGUNDOS, 0)
                estaPausado = false

                startForeground(
                    NOTIF_ID,
                    crearNotificacion(
                        this.getString(R.string.txt_titulo_contador_iniciado),
                        formatearTiempo(totalSegundos),
                        pausado = false
                    ).build()
                )

                iniciarContador(totalSegundos)
            }

            ACCION_PARAR -> {
                contadorJob?.cancel()

                estaActivo = false
                estaPausado = true

                actualizarNotificacion(
                    this.getString(R.string.txt_titulo_contador_pausado),
                    formatearTiempo(segundosRestantes),
                    pausado = true
                )
            }

            ACCION_REANUDAR -> {
                estaActivo = true
                estaPausado = false

                startForeground(
                    NOTIF_ID,
                    crearNotificacion(
                        this.getString(R.string.txt_titulo_contador_pausado),
                        formatearTiempo(segundosRestantes),
                        pausado = false
                    ).build()
                )

                iniciarContador(segundosRestantes)
            }

            ACCION_CANCELAR -> {
                contadorJob?.cancel()

                estaActivo = false
                estaPausado = false
                segundosRestantes = 0

                stopSelf()
            }        }

        return START_NOT_STICKY
    }

    private fun iniciarContador(totalSegundos: Int) {
        estaActivo = true
        contadorJob?.cancel()

        contadorJob = scope.launch {
            var restantes = totalSegundos

            while (restantes >= 0) {
                segundosRestantes = restantes

                actualizarNotificacion(
                    titulo = if (restantes > 0)
                        getString(R.string.txt_titulo_contador_iniciado)  // antes: txt_titulo_contador_pausado
                    else
                        getString(R.string.txt_titulo_contador_terminado),
                    contenido = if (restantes > 0)
                        formatearTiempo(restantes)
                    else
                        getString(R.string.txt_titulo_contador_finalizado),
                    pausado = false
                )

                if (restantes == 0) break

                delay(1000L)
                restantes--
            }

            estaActivo = false
            segundosRestantes = 0
            stopSelf()
        }
    }

    private fun formatearTiempo(segundos: Int): String {
        val h = segundos / 3600
        val m = (segundos % 3600) / 60
        val s = segundos % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    private fun crearPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "ABRIR_CONTADOR"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun pendingIntentAccion(action: String): PendingIntent {
        val intent = Intent(this, ContadorService::class.java).apply {
            this.action = action
        }

        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.txt_name_canal),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.txt_descripcion)
            }

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }
    }

    private fun crearNotificacion(
        titulo: String,
        contenido: String,
        pausado: Boolean
    ): NotificationCompat.Builder {

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(crearPendingIntent())

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

        return builder
    }

    private fun actualizarNotificacion(
        titulo: String,
        contenido: String,
        pausado: Boolean
    ) {
        getSystemService(NotificationManager::class.java)
            .notify(
                NOTIF_ID,
                crearNotificacion(titulo, contenido, pausado).build()
            )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}