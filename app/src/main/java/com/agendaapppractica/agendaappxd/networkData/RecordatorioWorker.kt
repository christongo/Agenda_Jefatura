package com.agendaapppractica.agendaappxd.networkData

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class RecordatorioWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val titulo = inputData.getString("titulo") ?: "Evento"
        val mensaje = inputData.getString("mensaje") ?: ""

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            // SOLUCIÓN: Cambiado a un recurso drawable compatible con las directrices de Android
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }
}