package com.agendaapppractica.agendaappxd.datos.modelos

import java.util.UUID

data class Nota(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String = "",
    val contenido: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)