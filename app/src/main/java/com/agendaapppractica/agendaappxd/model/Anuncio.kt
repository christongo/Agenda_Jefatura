package com.agendaapppractica.agendaappxd.model


data class Anuncio(

    val id: String = "",

    val grupoId: String = "",

    val titulo: String = "",

    val contenido: String = "",

    val autorId: String = "",

    val autorNombre: String = "",

    val fecha: Long = System.currentTimeMillis()
)