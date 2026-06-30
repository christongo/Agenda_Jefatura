package com.agendaapppractica.agendaappxd.model

data class Tarea(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val fechaFin: String = "",
    val horaFin: String = "",
    val usuarioId: String = "",
    val nombreUsuario: String = "",
    val correoUsuario: String = "",
    val tipoEvento: String = "normal",
    val visibilidad: String = "personal",
    val grupoId: String = "",
    val esGlobal: Boolean = false,
    val avisosMinutosAntes: List<Long> = listOf(10L, 60L, 1440L)
)