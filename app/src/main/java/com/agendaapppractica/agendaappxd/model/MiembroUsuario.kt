

package com.agendaapppractica.agendaappxd.model

data class MiembroUsuario(
    val uid: String = "",
    val nombre: String = "Usuario",
    val correo: String = "Sin correo",
    val fotoUrl: String? = null
)