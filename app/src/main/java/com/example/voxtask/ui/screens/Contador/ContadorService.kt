package com.example.voxtask.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.voxtask.MainActivity
import com.example.voxtask.R
import kotlinx.coroutines.*

class ContadorService : Service() {

    // Variable
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    companion object {
        const val CHANNEL_ID = "contador_channel"
        const val EXTRA_SEGUNDOS = "segundos"
        const val NOTIF_ID = 1
    }

    //Funciones
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val totalSegundos = intent?.getIntExtra(EXTRA_SEGUNDOS, 0) ?: 0

        crearCanal()
        startForeground(NOTIF_ID, crearNotificacion("⏱ Contador iniciado", formatearTiempo(totalSegundos)))

        scope.launch {
            var restantes = totalSegundos
            while (restantes >= 0) {
                actualizarNotificacion(
                    titulo = if (restantes > 0) "⏱ Contador en curso" else "✅ ¡Tiempo terminado!",
                    contenido = if (restantes > 0) formatearTiempo(restantes) else "El contador ha finalizado"
                )
                if (restantes == 0) break
                delay(1000L)
                restantes--
            }
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun formatearTiempo(segundos: Int): String {
        val h = segundos / 3600
        val m = (segundos % 3600) / 60
        val s = segundos % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    // Crea el PendingIntent que abre MainActivity y navega al Contador
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

    private fun crearCanal() {
        val canal = NotificationChannel(
            CHANNEL_ID,
            "Contador VoxTask",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Muestra la cuenta atrás del contador"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    private fun crearNotificacion(titulo: String, contenido: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(crearPendingIntent())
            .build()

    private fun actualizarNotificacion(titulo: String, contenido: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, crearNotificacion(titulo, contenido))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}