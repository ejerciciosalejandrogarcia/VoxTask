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

    /** Variables */
    private val ambito = CoroutineScope(Dispatchers.Default + Job())
    private var trabajoContador: Job? = null

    /** Configuracion del contador */
    companion object {
        /** Variables */
        const val CHANNEL_ID              = "contador_channel"
        const val CHANNEL_ID_FINALIZADO   = "contador_channel_finalizado"
        const val NOTIF_ID                = 1
        const val ACCION_BROADCAST_CANCELAR = "com.example.voxtask.CONTADOR_CANCELADO"
        const val EXTRA_SEGUNDOS  = "segundos"
        const val ACCION_INICIAR  = "INICIAR"
        const val ACCION_PARAR    = "PARAR"
        const val ACCION_REANUDAR = "REANUDAR"
        const val ACCION_CANCELAR = "CANCELAR"
        var estaActivo: Boolean    = false
        var estaPausado: Boolean   = false
        var estaTerminado: Boolean = false
        var segundosRestantes: Int = 0
    }

    /**
     * Permite gestionar las siguientes peticiones: iniciar, pausar, reanudar o cancelar
     */
    override fun onStartCommand(intento: Intent?, flags: Int, startId: Int): Int {
        crearCanales()

        when (intento?.action) {

            ACCION_INICIAR -> {
                val totalSegundos = intento.getIntExtra(EXTRA_SEGUNDOS, 0)
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
                trabajoContador?.cancel()
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
                trabajoContador?.cancel()
                estaActivo        = false
                estaPausado       = false
                estaTerminado = true
                segundosRestantes = 0
                getSystemService(NotificationManager::class.java).cancel(NOTIF_ID)
                val broadcast = Intent(ACCION_BROADCAST_CANCELAR)
                broadcast.setPackage(packageName)
                sendBroadcast(broadcast)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    /** Inicia la cuenta atrás del contador actualizando el estado y las notificaciones al usuario */
    private fun iniciarContador(totalSegundos: Int) {
        estaActivo = true
        trabajoContador?.cancel()

        trabajoContador = ambito.launch {
            var restantes = totalSegundos

            while (restantes >= 0) {
                segundosRestantes = restantes
                val finalizado = restantes == 0

                if (finalizado) {
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
            estaTerminado = true
            segundosRestantes = 0
        }
    }

    /**  Convierte el tiempo en el siguiente formato:'HH:MM:SS'    */
    private fun formatearTiempo(segundos: Int): String {
        val horas   = segundos / 3600
        val minutos = (segundos % 3600) / 60
        val segs    = segundos % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segs)
    }

    /** Permite el acceso para volver a la app al pulsar la notificación */
    private fun crearIntentoApertura(): PendingIntent {
        val intento = Intent(this, MainActivity::class.java).apply {
            action = "ABRIR_CONTADOR"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        return PendingIntent.getActivity(
            this, 0, intento,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Permite crear un botón de accion para interactuar con el servicio desde la notificación */
    private fun crearIntentoAccion(accion: String): PendingIntent {
        val intento = Intent(this, ContadorService::class.java).apply {
            this.action = accion
        }
        return PendingIntent.getService(
            this, accion.hashCode(), intento,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Permite registrar los canales de notificación en el sistema con distintas prioridades:
     * uno silencioso para la cuenta atras y uno ruidoso para avisar al finalizar
     */
    private fun crearCanales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val gestorNotificaciones = getSystemService(NotificationManager::class.java)

            val canalSilencioso = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.txt_name_canal),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.txt_descripcion)
                setSound(null, null)
                enableVibration(false)
            }

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

            gestorNotificaciones.createNotificationChannel(canalSilencioso)
            gestorNotificaciones.createNotificationChannel(canalFinalizado)
        }
    }

    /**
     * Permite crear la estructura de la notificacion
     */
    private fun crearNotificacion(titulo: String, contenido: String, pausado: Boolean, finalizado: Boolean = false): NotificationCompat.Builder {
        val canalId = if (finalizado) CHANNEL_ID_FINALIZADO else CHANNEL_ID
        val constructor = NotificationCompat.Builder(this, canalId)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(!finalizado)
            .setSilent(!finalizado)
            .setContentIntent(crearIntentoApertura())
        if (!finalizado) {
            if (pausado) {
                constructor.addAction(
                    android.R.drawable.ic_media_play,
                    getString(R.string.txt_title_reanudar),
                    crearIntentoAccion(ACCION_REANUDAR)
                )
            } else {
                constructor.addAction(
                    android.R.drawable.ic_media_pause,
                    getString(R.string.txt_title_pausar),
                    crearIntentoAccion(ACCION_PARAR)
                )
            }
            constructor.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.txt_title_cancelar),
                crearIntentoAccion(ACCION_CANCELAR)
            )
        }
        return constructor
    }

    /**
     * Permite actualizar la notificacion
     */
    private fun actualizarNotificacion(
        titulo: String,
        contenido: String,
        pausado: Boolean,
        finalizado: Boolean = false
    ) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, crearNotificacion(titulo, contenido, pausado, finalizado).build())
    }

    /**
     * Indica que el servicio no permite la vinculación con componentes de la interfaz
     */
    override fun onBind(intento: Intent?): IBinder? = null

    /**
     * Permite destruir la notificacion
     */
    override fun onDestroy() {
        super.onDestroy()
        ambito.cancel()
    }
}