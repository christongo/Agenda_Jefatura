

package com.agendaapppractica.agendaappxd.model

import android.R

data class MiembroUsuario(
    val uid: String = "",
    val nombre: String = "Usuario",
    val correo: String = "Sin correo",
    val fotoUrl: String? = null,
    val telefono: String? = null
)