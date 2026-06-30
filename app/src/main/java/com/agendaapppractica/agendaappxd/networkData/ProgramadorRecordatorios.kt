package com.agendaapppractica.agendaappxd.networkData

import android.content.Context
import androidx.work.*
import com.agendaapppractica.agendaappxd.model.Tarea
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ProgramadorRecordatorios {

    private val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun programarRecordatorios(context: Context, tarea: Tarea) {
        try {
            val fechaInicio = formato.parse("${tarea.fecha} ${tarea.hora}") ?: return
            val fechaFin = if (tarea.fechaFin.isNotBlank() && tarea.horaFin.isNotBlank()) {
                formato.parse("${tarea.fechaFin} ${tarea.horaFin}")
            } else null

            val ahora = Date()

            // 1. Programar avisos personalizados configurados por el usuario
            tarea.avisosMinutosAntes.forEach { minutos ->
                val tiempoAlerta = fechaInicio.time - TimeUnit.MINUTES.toMillis(minutos)
                val delay = tiempoAlerta - ahora.time

                if (delay > 0) {
                    val mensaje = generarMensajeAnticipado(tarea.tipoEvento, minutos)
                    programar(context, tarea.titulo, mensaje, delay)
                }
            }

            // 2. Programar aviso justo al iniciar el evento
            val delayInicio = fechaInicio.time - ahora.time
            if (delayInicio > 0) {
                programar(context, tarea.titulo, obtenerMensajeInicio(tarea.tipoEvento), delayInicio)
            }

            // 3. Programar aviso al finalizar el evento
            fechaFin?.let {
                val delayFin = it.time - ahora.time
                if (delayFin > 0) {
                    programar(context, tarea.titulo, obtenerMensajeFin(tarea.tipoEvento), delayFin)
                }
            }

        } catch (_: Exception) {
            // Manejo silencioso original
        }
    }

    private fun programar(context: Context, titulo: String, mensaje: String, delay: Long) {
        if (delay <= 0) return

        val datos = Data.Builder()
            .putString("titulo", titulo)
            .putString("mensaje", mensaje)
            .build()

        val request = OneTimeWorkRequestBuilder<RecordatorioWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(datos)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    // 🌟 Función matemática para transformar minutos en textos legibles amigables
    private fun generarMensajeAnticipado(tipoEvento: String, minutos: Long): String {
        val textoTiempo = when {
            minutos == 0L -> "ahora"
            minutos < 60L -> "en $minutos minutos"
            minutos == 60L -> "en 1 hora"
            minutos < 1440L -> "en ${minutos / 60} horas"
            minutos == 1440L -> "mañana"
            else -> "en ${minutos / 1440} días"
        }

        val articuloYEvento = when (tipoEvento) {
            "reunion" -> "La reunión comenzará"
            "vacaciones" -> "Tus vacaciones comienzan"
            "recordatorio" -> "Tu recordatorio es"
            else -> "Tu evento comenzará"
        }

        return if (minutos == 1440L) "$articuloYEvento mañana" else "$articuloYEvento $textoTiempo"
    }

    private fun obtenerMensajeInicio(tipoEvento: String): String {
        return when (tipoEvento) {
            "reunion" -> "La reunión acaba de comenzar"
            "vacaciones" -> "Tus vacaciones han comenzado"
            "recordatorio" -> "Es momento de tu recordatorio"
            else -> "Tu evento acaba de comenzar"
        }
    }

    private fun obtenerMensajeFin(tipoEvento: String): String {
        return when (tipoEvento) {
            "reunion" -> "La reunión ha finalizado"
            "vacaciones" -> "Tus vacaciones han finalizado"
            else -> "El evento ha finalizado"
        }
    }
}