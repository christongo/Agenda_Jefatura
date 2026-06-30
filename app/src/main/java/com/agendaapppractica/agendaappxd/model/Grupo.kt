package com.agendaapppractica.agendaappxd.model

data class Grupo(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val codigo: String = "",
    val creadorId: String = "",
    val fotoGrupo: String = "",
    val miembros: List<String> = emptyList(),
    val solicitudes: List<String> = emptyList(),
    val administradores: List<String> = emptyList()
)