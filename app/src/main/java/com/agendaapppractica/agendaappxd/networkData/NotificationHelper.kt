package com.agendaapppractica.agendaappxd.networkData

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    const val CHANNEL_ID = "agenda_recordatorios"

    fun crearCanal(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val canal = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios Agenda",
                NotificationManager.IMPORTANCE_HIGH
            )

            canal.description =
                "Recordatorios de eventos"

            val manager =
                context.getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(canal)
        }
    }
}